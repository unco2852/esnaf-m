package com.example.esnafm.ui.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationPreferences(
    val allNotifications: Boolean = true,
    val appointments: Boolean = true,
    val orders: Boolean = true,
    val messages: Boolean = true,
    val campaigns: Boolean = true
)

class NotificationPreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("esnafm_notification_prefs", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<NotificationPreferences> = _preferences.asStateFlow()

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
}
