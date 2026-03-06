package com.white.notepilot.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.white.notepilot.MainActivity
import com.white.notepilot.R
import com.white.notepilot.data.preferences.NotificationPreferences
import com.white.notepilot.ui.components.CustomPopupDialog
import com.white.notepilot.ui.theme.Blue
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.LightGray
import com.white.notepilot.ui.theme.NotesTheme
import com.white.notepilot.utils.NotificationHelper
import com.white.notepilot.viewmodel.ThemeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun SettingsScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val scope = rememberCoroutineScope()
    
    val notificationsEnabled by settingsViewModel.notificationPreferences.notificationsEnabled.collectAsState(initial = false)
    
    val backgroundSyncEnabled by settingsViewModel.notificationPreferences.backgroundSyncEnabled.collectAsState(initial = true)
    
    var showDisableDialog by remember { mutableStateOf(false) }
    
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        MainActivity.onPermissionResult = { isGranted ->
            scope.launch {
                if (isGranted) {
                    settingsViewModel.notificationPreferences.setNotificationsEnabled(true)
                } else {
                    if (activity != null && !NotificationHelper.shouldShowPermissionRationale(activity)) {
                        showPermissionDeniedDialog = true
                    }
                    settingsViewModel.notificationPreferences.setNotificationsEnabled(false)
                }
            }
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.PaddingLarge)
        ) {
            Text(
                text = stringResource(R.string.account_settings),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            SettingSwitchItem(
                title = "Push Notifications",
                description = if (notificationsEnabled) {
                    "Receive notifications about your notes"
                } else {
                    "Enable notifications to receive updates"
                },
                checked = notificationsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        if (NotificationHelper.hasNotificationPermission(context)) {
                            scope.launch {
                                settingsViewModel.notificationPreferences.setNotificationsEnabled(true)
                            }
                        } else {
                            activity?.let { NotificationHelper.requestNotificationPermission(it) }
                        }
                    } else {
                        showDisableDialog = true
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingSwitchItem(
                title = "Background Sync",
                description = if (backgroundSyncEnabled) {
                    stringResource(R.string.automatically_sync_notes_when_online)
                } else {
                    "Manual sync only"
                },
                checked = backgroundSyncEnabled,
                onCheckedChange = { enabled ->
                    scope.launch {
                        settingsViewModel.notificationPreferences.setBackgroundSyncEnabled(enabled)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            SettingSwitchItem(
                title = "Light Mode",
                description = "Switch between light and dark theme",
                checked = !isDarkMode,
                onCheckedChange = { themeViewModel.toggleTheme(!it) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        if (showDisableDialog) {
            CustomPopupDialog(
                message = "Are you sure you want to disable notifications? You won't receive updates about your notes.",
                negativeButtonText = "Cancel",
                positiveButtonText = "Disable",
                onDismiss = {
                    showDisableDialog = false
                },
                onNegativeClick = {
                    showDisableDialog = false
                },
                onPositiveClick = {
                    showDisableDialog = false
                    scope.launch {
                        settingsViewModel.notificationPreferences.setNotificationsEnabled(false)
                    }
                }
            )
        }
        
        if (showPermissionDeniedDialog) {
            CustomPopupDialog(
                message = "Notification permission is required. Please enable it in app settings to receive notifications.",
                negativeButtonText = "Cancel",
                positiveButtonText = "Open Settings",
                onDismiss = {
                    showPermissionDeniedDialog = false
                },
                onNegativeClick = {
                    showPermissionDeniedDialog = false
                },
                onPositiveClick = {
                    showPermissionDeniedDialog = false
                    // Open system settings
                    NotificationHelper.openNotificationSettings(context)
                }
            )
        }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val notificationPreferences: NotificationPreferences
) : ViewModel()

@Composable
private fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        color = LightGray,
                        lineHeight = 18.sp
                    )
                }
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = Blue,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = LightGray.copy(alpha = 0.3f),
                    checkedBorderColor = Color.Transparent,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    NotesTheme {
        SettingsScreen(navController = rememberNavController())
    }
}
