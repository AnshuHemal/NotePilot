package com.white.notepilot.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.white.notepilot.data.model.AdWatchRecord
import com.white.notepilot.data.model.UserRewards
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class RewardsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val TAG = "RewardsRepository"
        private const val REWARDS_COLLECTION = "user_rewards"
        private const val MAX_ADS_PER_HOUR = 5
        private const val MIN_COINS_PER_HOUR = 50
        private const val MAX_COINS_PER_HOUR = 100
        private const val HOUR_IN_MILLIS = 60 * 60 * 1000L
    }
    
    suspend fun getUserRewards(userId: String): UserRewards? {
        return try {
            val document = firestore.collection(REWARDS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(UserRewards::class.java)
            } else {
                val newRewards = UserRewards(userId = userId)
                createUserRewards(newRewards)
                newRewards
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user rewards", e)
            null
        }
    }
    
    private suspend fun createUserRewards(userRewards: UserRewards) {
        try {
            firestore.collection(REWARDS_COLLECTION)
                .document(userRewards.userId)
                .set(userRewards)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user rewards", e)
        }
    }
    
    suspend fun canWatchAd(userId: String): Boolean {
        val userRewards = getUserRewards(userId) ?: return false
        
        val currentTime = System.currentTimeMillis()
        val lastAdTime = userRewards.lastAdWatchTime?.toDate()?.time ?: 0
        
        val oneHourAgo = currentTime - HOUR_IN_MILLIS
        
        val recentAds = userRewards.adWatchHistory.count { record ->
            record.timestamp.toDate().time > oneHourAgo
        }
        
        return recentAds < MAX_ADS_PER_HOUR
    }
    
    suspend fun getAdsWatchedInLastHour(userId: String): Int {
        val userRewards = getUserRewards(userId) ?: return 0
        
        val currentTime = System.currentTimeMillis()
        val oneHourAgo = currentTime - HOUR_IN_MILLIS
        
        return userRewards.adWatchHistory.count { record ->
            record.timestamp.toDate().time > oneHourAgo
        }
    }
    
    suspend fun getNextAdReward(userId: String): Int {
        val adsWatched = getAdsWatchedInLastHour(userId)
        
        if (adsWatched >= MAX_ADS_PER_HOUR) {
            return 0
        }
        
        val hourlyDistribution = getOrCreateHourlyDistribution(userId)
        return hourlyDistribution.getOrNull(adsWatched) ?: 0
    }
    
    private suspend fun getOrCreateHourlyDistribution(userId: String): List<Int> {
        val userRewards = getUserRewards(userId) ?: return emptyList()
        val currentHour = getCurrentHourKey()
        
        val existingDistribution = getStoredHourlyDistribution(userId, currentHour)
        if (existingDistribution.isNotEmpty()) {
            return existingDistribution
        }
        
        val totalCoinsForHour = Random.nextInt(MIN_COINS_PER_HOUR, MAX_COINS_PER_HOUR + 1)
        val distribution = generateCoinDistribution(totalCoinsForHour)
        
        storeHourlyDistribution(userId, currentHour, distribution)
        
        return distribution
    }
    
    private fun generateCoinDistribution(totalCoins: Int): List<Int> {
        val distribution = mutableListOf<Int>()
        var remaining = totalCoins
        
        // Generate random amounts for first 4 ads
        for (i in 0 until MAX_ADS_PER_HOUR - 1) {
            val minAmount = 1
            val maxAmount = maxOf(1, remaining - (MAX_ADS_PER_HOUR - i - 1))
            
            val amount = if (minAmount <= maxAmount) {
                Random.nextInt(minAmount, maxAmount + 1)
            } else {
                minAmount
            }
            
            distribution.add(amount)
            remaining -= amount
        }
        
        // Add remaining coins to the last ad
        distribution.add(maxOf(1, remaining))
        
        // Shuffle the distribution to make it random order
        return distribution.shuffled(Random)
    }
    
    private suspend fun getStoredHourlyDistribution(userId: String, hourKey: String): List<Int> {
        return try {
            val document = firestore.collection(REWARDS_COLLECTION)
                .document(userId)
                .collection("hourly_distributions")
                .document(hourKey)
                .get()
                .await()
            
            if (document.exists()) {
                val distributionData = document.get("distribution") as? List<*>
                distributionData?.mapNotNull { it as? Long }?.map { it.toInt() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting stored hourly distribution", e)
            emptyList()
        }
    }
    
    private suspend fun storeHourlyDistribution(userId: String, hourKey: String, distribution: List<Int>) {
        try {
            val data = mapOf(
                "distribution" to distribution,
                "totalCoins" to distribution.sum(),
                "createdAt" to Timestamp.now()
            )
            
            firestore.collection(REWARDS_COLLECTION)
                .document(userId)
                .collection("hourly_distributions")
                .document(hourKey)
                .set(data)
                .await()
                
            cleanupOldDistributions(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error storing hourly distribution", e)
        }
    }
    
    private suspend fun cleanupOldDistributions(userId: String) {
        try {
            val cutoffTime = System.currentTimeMillis() - (24 * HOUR_IN_MILLIS)
            val cutoffTimestamp = Timestamp(Date(cutoffTime))
            
            val oldDocuments = firestore.collection(REWARDS_COLLECTION)
                .document(userId)
                .collection("hourly_distributions")
                .whereLessThan("createdAt", cutoffTimestamp)
                .get()
                .await()
            
            oldDocuments.documents.forEach { document ->
                document.reference.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old distributions", e)
        }
    }
    
    private fun getCurrentHourKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}-${calendar.get(Calendar.HOUR_OF_DAY)}"
    }
    
    suspend fun addCoinsForWatchingAd(userId: String): Int? {
        return try {
            val userRewards = getUserRewards(userId) ?: return null
            
            if (!canWatchAd(userId)) {
                return null
            }
            
            val coinsEarned = getNextAdReward(userId)
            val currentTime = Timestamp.now()
            
            val newAdRecord = AdWatchRecord(
                timestamp = currentTime,
                coinsEarned = coinsEarned,
                adType = "rewarded"
            )
            
            val updatedHistory = (userRewards.adWatchHistory + newAdRecord)
                .sortedByDescending { it.timestamp.toDate().time }
                .take(50)
            
            val updatedRewards = userRewards.copy(
                totalCoins = userRewards.totalCoins + coinsEarned,
                lastAdWatchTime = currentTime,
                adWatchHistory = updatedHistory
            )
            
            firestore.collection(REWARDS_COLLECTION)
                .document(userId)
                .set(updatedRewards)
                .await()
            
            coinsEarned
        } catch (e: Exception) {
            Log.e(TAG, "Error adding coins for watching ad", e)
            null
        }
    }
    
    suspend fun getTimeUntilNextAd(userId: String): Long {
        val userRewards = getUserRewards(userId) ?: return 0L
        
        if (userRewards.adWatchHistory.isEmpty()) {
            return 0L
        }
        
        val currentTime = System.currentTimeMillis()
        val oneHourAgo = currentTime - HOUR_IN_MILLIS
        
        val recentAds = userRewards.adWatchHistory
            .filter { it.timestamp.toDate().time > oneHourAgo }
            .sortedBy { it.timestamp.toDate().time }
        
        if (recentAds.size < MAX_ADS_PER_HOUR) {
            return 0L
        }
        
        val oldestRecentAd = recentAds.first()
        val timeWhenNextAdAvailable = oldestRecentAd.timestamp.toDate().time + HOUR_IN_MILLIS
        
        return maxOf(0L, timeWhenNextAdAvailable - currentTime)
    }
    
    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    suspend fun getTotalCoinsForCurrentHour(userId: String): Int {
        val hourlyDistribution = getOrCreateHourlyDistribution(userId)
        return hourlyDistribution.sum()
    }
    
    suspend fun getRemainingCoinsForHour(userId: String): Int {
        val totalCoins = getTotalCoinsForCurrentHour(userId)
        val adsWatched = getAdsWatchedInLastHour(userId)
        val hourlyDistribution = getOrCreateHourlyDistribution(userId)
        
        val earnedCoins = hourlyDistribution.take(adsWatched).sum()
        return totalCoins - earnedCoins
    }
}