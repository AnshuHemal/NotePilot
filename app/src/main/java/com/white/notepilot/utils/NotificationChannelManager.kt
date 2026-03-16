package com.white.notepilot.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.white.notepilot.R

object NotificationChannelManager {
    
    const val CHANNEL_SYNC = "sync_channel"
    const val CHANNEL_REMINDERS = "reminders_channel"
    const val CHANNEL_UPDATES = "updates_channel"
    const val CHANNEL_GENERAL = "general_channel"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                context.getString(R.string.notification_channel_sync_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_sync_description)
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.notification_channel_reminders_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_reminders_description)
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lightColor = android.graphics.Color.BLUE
            }
            
            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES,
                context.getString(R.string.notification_channel_updates_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_updates_description)
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                context.getString(R.string.notification_channel_general_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_general_description)
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannels(
                listOf(syncChannel, remindersChannel, updatesChannel, generalChannel)
            )
        }
    }
    
    fun deleteNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(channelId)
        }
    }
    
    fun getChannelImportance(context: Context, channelId: String): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance ?: NotificationManager.IMPORTANCE_DEFAULT
        }
        return NotificationManager.IMPORTANCE_DEFAULT
    }
    
    fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }
    
    fun isChannelEnabled(context: Context, channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return true
    }
}
