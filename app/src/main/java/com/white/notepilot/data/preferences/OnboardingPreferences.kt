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

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_preferences")

@Singleton
class OnboardingPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.onboardingDataStore.data
        .map { it[ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted() {
        context.onboardingDataStore.edit { it[ONBOARDING_COMPLETED] = true }
    }
}
