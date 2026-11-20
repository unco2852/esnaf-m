package com.example.esnafm.ui.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificationPreferences(
    val allNotifications: Boolean = true,
    val appointments: Boolean = true,
    val orders: Boolean = true,
    val messages: Boolean = true,
    val campaigns: Boolean = true
)

class NotificationPreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = application.getSharedPreferences(
        "esnafm_notification_prefs_${auth.currentUser?.uid ?: "anonymous"}",
        Context.MODE_PRIVATE
    )

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<NotificationPreferences> = _preferences.asStateFlow()

    init {
        loadCloudPreferences()
    }

    private fun loadPreferences(): NotificationPreferences {
        return NotificationPreferences(
            allNotifications = prefs.getBoolean("allNotifications", true),
            appointments = prefs.getBoolean("appointments", true),
            orders = prefs.getBoolean("orders", true),
            messages = prefs.getBoolean("messages", true),
            campaigns = prefs.getBoolean("campaigns", true)
        )
    }

    private fun savePreferences(newPrefs: NotificationPreferences) {
        prefs.edit().apply {
            putBoolean("allNotifications", newPrefs.allNotifications)
            putBoolean("appointments", newPrefs.appointments)
            putBoolean("orders", newPrefs.orders)
            putBoolean("messages", newPrefs.messages)
            putBoolean("campaigns", newPrefs.campaigns)
            apply()
        }
        _preferences.value = newPrefs
        persistToCloud(newPrefs)
    }

    fun toggleAll(enabled: Boolean) {
        savePreferences(
            _preferences.value.copy(
                allNotifications = enabled,
                appointments = enabled,
                orders = enabled,
                messages = enabled,
                campaigns = enabled
            )
        )
    }

    fun updatePref(update: (NotificationPreferences) -> NotificationPreferences) {
        val updated = update(_preferences.value)
        savePreferences(updated)
    }

    private fun persistToCloud(preferences: NotificationPreferences) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching {
                firestore.collection("users").document(uid)
                    .collection("settings").document("notifications")
                    .set(mapOf(
                        "allNotifications" to preferences.allNotifications,
                        "appointments" to preferences.appointments,
                        "orders" to preferences.orders,
                        "messages" to preferences.messages,
                        "campaigns" to preferences.campaigns
                    )).await()
            }
        }
    }

    private fun loadCloudPreferences() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val snapshot = runCatching {
                firestore.collection("users").document(uid)
                    .collection("settings").document("notifications").get().await()
            }.getOrNull() ?: return@launch

            if (!snapshot.exists()) {
                persistToCloud(_preferences.value)
                return@launch
            }

            val values = snapshot.data.orEmpty()
            val cloudPreferences = NotificationPreferences(
                allNotifications = values["allNotifications"] as? Boolean ?: true,
                appointments = values["appointments"] as? Boolean ?: true,
                orders = values["orders"] as? Boolean ?: true,
                messages = values["messages"] as? Boolean ?: true,
                campaigns = values["campaigns"] as? Boolean ?: true
            )
            prefs.edit().apply {
                putBoolean("allNotifications", cloudPreferences.allNotifications)
                putBoolean("appointments", cloudPreferences.appointments)
                putBoolean("orders", cloudPreferences.orders)
                putBoolean("messages", cloudPreferences.messages)
                putBoolean("campaigns", cloudPreferences.campaigns)
                apply()
            }
            _preferences.value = cloudPreferences
        }
    }
}
