package com.example.esnafm.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esnafm.R
import com.example.esnafm.ui.theme.DarkInk
import com.example.esnafm.ui.theme.LightBeige

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationPreferencesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val prefs by viewModel.preferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_notification_preferences), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBeige, titleContentColor = DarkInk)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.desc_notification_preferences),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkInk
                )
            }

            item {
                PreferenceSwitch(
                    title = stringResource(R.string.pref_all_notifications),
                    subtitle = stringResource(R.string.pref_all_notifications_desc),
                    checked = prefs.allNotifications,
                    onCheckedChange = { viewModel.toggleAll(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                PreferenceSwitch(
                    title = stringResource(R.string.pref_appointments),
                    subtitle = stringResource(R.string.pref_appointments_desc),
                    checked = prefs.appointments,
                    enabled = prefs.allNotifications,
                    onCheckedChange = { isChecked -> viewModel.updatePref { it.copy(appointments = isChecked) } }
                )
            }

            item {
                PreferenceSwitch(
                    title = stringResource(R.string.pref_orders),
                    subtitle = stringResource(R.string.pref_orders_desc),
                    checked = prefs.orders,
                    enabled = prefs.allNotifications,
                    onCheckedChange = { isChecked -> viewModel.updatePref { it.copy(orders = isChecked) } }
                )
            }

            item {
                PreferenceSwitch(
                    title = stringResource(R.string.pref_messages),
                    subtitle = stringResource(R.string.pref_messages_desc),
                    checked = prefs.messages,
                    enabled = prefs.allNotifications,
                    onCheckedChange = { isChecked -> viewModel.updatePref { it.copy(messages = isChecked) } }
                )
            }

            item {
                PreferenceSwitch(
                    title = stringResource(R.string.pref_campaigns),
                    subtitle = stringResource(R.string.pref_campaigns_desc),
                    checked = prefs.campaigns,
                    enabled = prefs.allNotifications,
                    onCheckedChange = { isChecked -> viewModel.updatePref { it.copy(campaigns = isChecked) } }
                )
            }
        }
    }
}

@Composable
private fun PreferenceSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = if (enabled) DarkInk else DarkInk.copy(alpha = 0.5f))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = if (enabled) DarkInk.copy(alpha = 0.7f) else DarkInk.copy(alpha = 0.4f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
