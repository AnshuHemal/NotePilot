package com.white.notepilot.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.ui.components.ads.RewardedAdManager
import com.white.notepilot.data.model.UserRewards
import com.white.notepilot.data.repository.RewardsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val rewardsRepository: RewardsRepository,
    private val rewardedAdManager: RewardedAdManager
) : ViewModel() {
    
    private val _userRewards = MutableStateFlow<UserRewards?>(null)
    val userRewards: StateFlow<UserRewards?> = _userRewards.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _canWatchAd = MutableStateFlow(false)
    val canWatchAd: StateFlow<Boolean> = _canWatchAd.asStateFlow()
    
    private val _adsWatchedInHour = MutableStateFlow(0)
    val adsWatchedInHour: StateFlow<Int> = _adsWatchedInHour.asStateFlow()
    
    private val _nextAdReward = MutableStateFlow(0)
    val nextAdReward: StateFlow<Int> = _nextAdReward.asStateFlow()
    
    private val _timeUntilNextAd = MutableStateFlow(0L)
    val timeUntilNextAd: StateFlow<Long> = _timeUntilNextAd.asStateFlow()
    
    private val _totalCoinsForHour = MutableStateFlow(0)
    val totalCoinsForHour: StateFlow<Int> = _totalCoinsForHour.asStateFlow()
    
    private val _remainingCoinsForHour = MutableStateFlow(0)
    val remainingCoinsForHour: StateFlow<Int> = _remainingCoinsForHour.asStateFlow()
    
    private val _showRewardDialog = MutableStateFlow(false)
    val showRewardDialog: StateFlow<Boolean> = _showRewardDialog.asStateFlow()
    
    private val _lastEarnedCoins = MutableStateFlow(0)
    val lastEarnedCoins: StateFlow<Int> = _lastEarnedCoins.asStateFlow()
    
    val isAdAvailable = rewardedAdManager.isAdAvailable
    val isAdLoading = rewardedAdManager.isLoading
    
    fun loadUserRewards(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val rewards = rewardsRepository.getUserRewards(userId)
            _userRewards.value = rewards
            
            updateAdStatus(userId)
            
            _isLoading.value = false
        }
    }
    
    private suspend fun updateAdStatus(userId: String) {
        val canWatch = rewardsRepository.canWatchAd(userId)
        val adsWatched = rewardsRepository.getAdsWatchedInLastHour(userId)
        val nextReward = rewardsRepository.getNextAdReward(userId)
        val timeUntil = rewardsRepository.getTimeUntilNextAd(userId)
        val totalCoins = rewardsRepository.getTotalCoinsForCurrentHour(userId)
        val remainingCoins = rewardsRepository.getRemainingCoinsForHour(userId)
        
        _canWatchAd.value = canWatch
        _adsWatchedInHour.value = adsWatched
        _nextAdReward.value = nextReward
        _timeUntilNextAd.value = timeUntil
        _totalCoinsForHour.value = totalCoins
        _remainingCoinsForHour.value = remainingCoins
    }
    
    fun loadRewardedAd(context: Context) {
        rewardedAdManager.loadRewardedAd(context)
    }
    
    fun watchRewardedAd(activity: Activity, userId: String) {
        if (!_canWatchAd.value || !rewardedAdManager.isRewardedAdReady()) {
            return
        }
        
        rewardedAdManager.showRewardedAd(
            activity = activity,
            onUserEarnedReward = { _ ->
                viewModelScope.launch {
                    val coinsEarned = rewardsRepository.addCoinsForWatchingAd(userId)
                    if (coinsEarned != null) {
                        _lastEarnedCoins.value = coinsEarned
                        _showRewardDialog.value = true
                        loadUserRewards(userId)
                    }
                }
            },
            onAdDismissed = {
                viewModelScope.launch {
                    updateAdStatus(userId)
                }
            }
        )
    }
    
    fun dismissRewardDialog() {
        _showRewardDialog.value = false
    }
    
    fun refreshRewardsData(userId: String) {
        loadUserRewards(userId)
    }
}