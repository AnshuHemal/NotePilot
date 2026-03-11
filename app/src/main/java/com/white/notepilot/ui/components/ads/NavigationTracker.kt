package com.white.notepilot.ui.components.ads

import android.util.Log
import com.white.notepilot.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NavigationTracker {
    private const val TAG = "NavigationTracker"
    private var navigationCount = 0
    private var lastResetTime = System.currentTimeMillis()
    
    private val _shouldShowInterstitial = MutableStateFlow(false)
    val shouldShowInterstitial: StateFlow<Boolean> = _shouldShowInterstitial.asStateFlow()
    
    fun trackNavigation() {
        navigationCount++
        Log.d(TAG, "Navigation tracked. Count: $navigationCount")
        
        if (navigationCount >= Constants.NAVIGATION_COUNT_THRESHOLD && !_shouldShowInterstitial.value) {
            Log.d(TAG, "Threshold reached. Triggering interstitial ad.")
            _shouldShowInterstitial.value = true
        }
    }
    
    fun onAdShown() {
        Log.d(TAG, "Ad shown. Resetting trigger and adjusting count.")
        _shouldShowInterstitial.value = false
        navigationCount = 0
        lastResetTime = System.currentTimeMillis()
    }
    
    fun resetCounter() {
        Log.d(TAG, "Counter reset.")
        navigationCount = 0
        lastResetTime = System.currentTimeMillis()
        _shouldShowInterstitial.value = false
    }

    fun checkAndResetIfInactive(inactivityThresholdMs: Long = 300000L) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastResetTime > inactivityThresholdMs) {
            Log.d(TAG, "Resetting due to inactivity.")
            resetCounter()
        }
    }
}
