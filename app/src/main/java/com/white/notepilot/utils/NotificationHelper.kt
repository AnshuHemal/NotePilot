package com.white.notepilot.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.white.notepilot.MainActivity
import com.white.notepilot.R
import me.leolin.shortcutbadger.ShortcutBadger

object NotificationHelper {
    
    private const val TAG = "NotificationHelper"
    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    private const val SYNC_CHANNEL_ID = "sync_notifications"
    private const val SYNC_CHANNEL_NAME = "Sync Notifications"

    private var notificationIdCounter = 1000

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }

    fun shouldShowPermissionRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            false
        }
    }

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    fun openNotificationSettings(context: Context) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun getFCMToken(onTokenReceived: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "FCM Token: $token")
                onTokenReceived(token)
            } else {
                Log.e(TAG, "Failed to get FCM token", task.exception)
            }
        }
    }

    fun updateAppBadge(context: Context, count: Int) {
        try {
            val success = ShortcutBadger.applyCount(context, count)
            if (success) {
                Log.d(TAG, "App badge updated successfully with count: $count")
            } else {
                Log.w(TAG, "Failed to update app badge - launcher may not support badges")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating app badge", e)
        }
    }

    fun clearAppBadge(context: Context) {
        try {
            val success = ShortcutBadger.removeCount(context)
            if (success) {
                Log.d(TAG, "App badge cleared successfully")
            } else {
                Log.w(TAG, "Failed to clear app badge - launcher may not support badges")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing app badge", e)
        }
    }

    fun isBadgeSupported(context: Context): Boolean {
        return ShortcutBadger.isBadgeCounterSupported(context)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channel = NotificationChannel(
                SYNC_CHANNEL_ID,
                SYNC_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for note sync status"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $SYNC_CHANNEL_ID")
        }
    }

    fun showSyncNotification(
        context: Context,
        title: String,
        message: String,
        noteId: Int? = null
    ) {
        try {
            if (!hasNotificationPermission(context) || !areNotificationsEnabled(context)) {
                Log.w(TAG, "Notifications not enabled, skipping push notification")
                return
            }
            
            createNotificationChannel(context)
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                noteId?.let { putExtra("noteId", it) }
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationIdCounter++,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .setColor(context.getColor(R.color.purple_500))
                .build()
            
            notificationManager.notify(notificationIdCounter++, notification)
            
            Log.d(TAG, "Push notification shown: $title - $message")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing push notification", e)
        }
    }
}
