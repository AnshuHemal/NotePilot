package com.white.notepilot.ui.components.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.white.notepilot.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdManager @Inject constructor() {
    
    private var rewardedAd: RewardedAd? = null
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isAdAvailable = MutableStateFlow(false)
    val isAdAvailable: StateFlow<Boolean> = _isAdAvailable.asStateFlow()
    
    companion object {
        private const val TAG = "RewardedAdManager"
    }
    
    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || _isLoading.value) {
            return
        }
        
        _isLoading.value = true
        
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(
            context,
            context.getString(R.string.rewarded_ad_unit_id),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${adError.message}")
                    rewardedAd = null
                    _isLoading.value = false
                    _isAdAvailable.value = false
                }
                
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    _isLoading.value = false
                    _isAdAvailable.value = true
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            Log.d(TAG, "Rewarded ad was clicked")
                        }
                        
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad dismissed")
                            rewardedAd = null
                            _isAdAvailable.value = false
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                            rewardedAd = null
                            _isAdAvailable.value = false
                        }
                        
                        override fun onAdImpression() {
                            Log.d(TAG, "Rewarded ad recorded an impression")
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad showed fullscreen content")
                        }
                    }
                }
            }
        )
    }
    
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (Int) -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        rewardedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                val rewardAmount = rewardItem.amount
                Log.d(TAG, "User earned reward: $rewardAmount")
                onUserEarnedReward(rewardAmount)
            }
            
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")
                    rewardedAd = null
                    _isAdAvailable.value = false
                    onAdDismissed()
                    loadRewardedAd(activity)
                }
                
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                    _isAdAvailable.value = false
                    onAdDismissed()
                }
            }
        } ?: run {
            Log.w(TAG, "Rewarded ad is not ready yet")
            onAdDismissed()
        }
    }
    
    fun isRewardedAdReady(): Boolean {
        return rewardedAd != null
    }
}