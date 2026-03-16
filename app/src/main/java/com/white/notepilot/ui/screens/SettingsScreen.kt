package com.white.notepilot.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.white.notepilot.ui.navigation.Routes
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
            
            Text(
                text = "Tools",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            SettingSwitchItem(
                title = "Scan QR Code",
                description = "Scan a QR code to import a note",
                checked = false,
                onCheckedChange = { },
                isClickable = true,
                onClick = {
                    navController.navigate(Routes.QRScanner.route)
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Support & Information",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            SettingSwitchItem(
                title = "About",
                description = "App version, privacy policy, and more",
                checked = false,
                onCheckedChange = { },
                isClickable = true,
                onClick = {
                    navController.navigate(Routes.About.route)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingSwitchItem(
                title = "Report & Feedback",
                description = "Help us improve the app",
                checked = false,
                onCheckedChange = { },
                isClickable = true,
                onClick = {
                    navController.navigate(Routes.ReportFeedback.route)
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        if (showDisableDialog) {
            CustomPopupDialog(
                message = "Are you sure you want to disable notifications? You won't receive updates about your notes.",
                negativeButtonText = "Cancel",
                positiveButtonText = "Disable",
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
private fun SettingsClickableItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = LightGray,
                    lineHeight = 18.sp
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SettingsNavigationItem(
    title: String,
    description: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

private fun openFeedbackEmail(context: Context) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:")
            putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("hemalkatariya4488@gmail.com"))
            putExtra(android.content.Intent.EXTRA_SUBJECT, "NotePilot - Feedback & Report")
            putExtra(android.content.Intent.EXTRA_TEXT, "Hi,\n\nI would like to provide feedback about NotePilot:\n\n")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error - could show a toast or log
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isClickable && onClick != null) {
                        Modifier.clickable { onClick() }
                    } else Modifier
                ),
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
            
            if (isClickable) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
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
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    NotesTheme {
        SettingsScreen(navController = rememberNavController())
    }
}
