package com.white.notepilot.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_preferences")

@Singleton
class NotificationPreferences @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val BACKGROUND_SYNC_ENABLED = booleanPreferencesKey("background_sync_enabled")
    }
    
    val notificationsEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: false
        }
    
    val backgroundSyncEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BACKGROUND_SYNC_ENABLED] ?: true
        }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[PreferencesKeys.BACKGROUND_SYNC_ENABLED] = enabled
        }
    }
}
