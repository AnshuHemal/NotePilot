package com.white.notepilot.ui.components.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.ui.res.stringResource
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.white.notepilot.R
import com.white.notepilot.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InterstitialAdManager(private val context: Context) {
    private companion object {
        const val TAG = "InterstitialAdManager"
    }
    
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var lastAdShownTime = 0L
    
    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()
    
    init {
        loadAd()
    }
    
    private fun loadAd() {
        if (isLoading) {
            Log.d(TAG, "Ad is already loading, skipping...")
            return
        }
        
        Log.d(TAG, "Loading interstitial ad...")
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            context.getString(R.string.interstitial_ad_unit_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load ad: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                    _isAdReady.value = false
                }
                
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoading = false
                    _isAdReady.value = true
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            Log.d(TAG, "Ad clicked")
                        }
                        
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed")
                            interstitialAd = null
                            _isAdReady.value = false
                            lastAdShownTime = System.currentTimeMillis()
                            loadAd() // Load next ad
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Failed to show ad: ${error.message}")
                            interstitialAd = null
                            _isAdReady.value = false
                            loadAd() // Try to load another ad
                        }
                        
                        override fun onAdImpression() {
                            Log.d(TAG, "Ad impression recorded")
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad showed full screen content")
                        }
                    }
                }
            }
        )
    }
    
    fun showAd(activity: Activity): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAd = currentTime - lastAdShownTime
        
        Log.d(TAG, "Attempting to show ad. Ad ready: ${interstitialAd != null}, Time since last: ${timeSinceLastAd}ms")
        
        return if (interstitialAd != null && 
                   timeSinceLastAd >= Constants.MIN_TIME_BETWEEN_INTERSTITIAL_MS) {
            Log.d(TAG, "Showing interstitial ad")
            interstitialAd?.show(activity)
            true
        } else {
            Log.d(TAG, "Cannot show ad - either not ready or cooldown active")
            false
        }
    }
    
    fun destroy() {
        Log.d(TAG, "Destroying ad manager")
        interstitialAd = null
        _isAdReady.value = false
    }
}