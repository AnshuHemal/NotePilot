package com.white.notepilot.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class UserRewards(
    @PropertyName("userId") val userId: String = "",
    @PropertyName("totalCoins") val totalCoins: Int = 0,
    @PropertyName("adsWatchedToday") val adsWatchedToday: Int = 0,
    @PropertyName("lastAdWatchTime") val lastAdWatchTime: Timestamp? = null,
    @PropertyName("lastResetDate") val lastResetDate: String = "",
    @PropertyName("adWatchHistory") val adWatchHistory: List<AdWatchRecord> = emptyList()
)

data class AdWatchRecord(
    @PropertyName("timestamp") val timestamp: Timestamp = Timestamp.now(),
    @PropertyName("coinsEarned") val coinsEarned: Int = 0,
    @PropertyName("adType") val adType: String = "rewarded"
)