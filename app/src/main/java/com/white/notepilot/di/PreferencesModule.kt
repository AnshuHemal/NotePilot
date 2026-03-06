package com.white.notepilot.di

import android.content.Context
import com.white.notepilot.data.preferences.NotificationPreferences
import com.white.notepilot.data.preferences.ThemePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    
    @Provides
    @Singleton
    fun provideThemePreferences(
        @ApplicationContext context: Context
    ): ThemePreferences {
        return ThemePreferences(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationPreferences(
        @ApplicationContext context: Context
    ): NotificationPreferences {
        return NotificationPreferences(context)
    }
}
