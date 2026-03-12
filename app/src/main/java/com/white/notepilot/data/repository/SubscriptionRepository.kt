package com.white.notepilot.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.white.notepilot.data.model.Subscription
import com.white.notepilot.data.model.SubscriptionPlan
import com.white.notepilot.data.model.SubscriptionType
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val rewardsRepository: RewardsRepository
) {
    
    companion object {
        private const val TAG = "SubscriptionRepository"
        private const val SUBSCRIPTIONS_COLLECTION = "user_subscriptions"
        private const val PREMIUM_COIN_COST = 2500
    }
    
    suspend fun getSubscriptionWithoutExpiryCheck(userId: String): Subscription? {
        return try {
            Log.d(TAG, "Getting subscription without expiry check for user: $userId")
            val document = firestore.collection(SUBSCRIPTIONS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val subscription = document.toObject(Subscription::class.java)
                Log.d(TAG, "Retrieved raw subscription: $subscription")
                subscription
            } else {
                Log.d(TAG, "No subscription found")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subscription without expiry check", e)
            null
        }
    }
    
    suspend fun getUserSubscription(userId: String): Subscription? {
        return try {
            Log.d(TAG, "Getting subscription for user: $userId")
            val document = firestore.collection(SUBSCRIPTIONS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val subscription = document.toObject(Subscription::class.java)
                subscription?.let { 
                    Log.d(TAG, "Retrieved subscription: $it")
                    Log.d(TAG, "isPremium: ${it.isPremium}, isActive: ${it.isActive}, type: ${it.subscriptionType}")
                    // Only check expiry for existing premium subscriptions
                    if (it.isPremium) {
                        checkAndUpdateExpiry(it)
                    } else {
                        it
                    }
                }
            } else {
                Log.d(TAG, "No subscription found, creating new one")
                val newSubscription = Subscription(userId = userId)
                createUserSubscription(newSubscription)
                newSubscription
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user subscription", e)
            null
        }
    }
    
    private suspend fun createUserSubscription(subscription: Subscription) {
        try {
            firestore.collection(SUBSCRIPTIONS_COLLECTION)
                .document(subscription.userId)
                .set(subscription)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user subscription", e)
        }
    }
    
    private suspend fun checkAndUpdateExpiry(subscription: Subscription): Subscription {
        Log.d(TAG, "Checking expiry for subscription: $subscription")
        
        // Only check expiry for premium subscriptions
        if (subscription.isPremium) {
            if (subscription.expiryDate != null) {
                // Time-based subscription (Monthly/Yearly)
                val currentTime = System.currentTimeMillis()
                val expiryTime = subscription.expiryDate.toDate().time
                
                Log.d(TAG, "Current time: $currentTime, Expiry time: $expiryTime")
                Log.d(TAG, "Is expired: ${currentTime > expiryTime}")
                
                if (currentTime > expiryTime) {
                    Log.d(TAG, "Subscription expired, updating to free")
                    val expiredSubscription = subscription.copy(
                        isPremium = false,
                        subscriptionType = SubscriptionType.FREE,
                        isActive = false
                    )
                    updateUserSubscription(expiredSubscription)
                    return expiredSubscription
                } else {
                    Log.d(TAG, "Subscription is still active and not expired")
                    // Subscription is valid and not expired - ensure it's active
                    if (!subscription.isActive) {
                        Log.d(TAG, "Marking valid subscription as active")
                        val activeSubscription = subscription.copy(isActive = true)
                        updateUserSubscription(activeSubscription)
                        return activeSubscription
                    }
                }
            } else {
                // Lifetime subscription - no expiry date
                Log.d(TAG, "Lifetime subscription detected - ensuring it's active")
                if (!subscription.isActive) {
                    Log.d(TAG, "Marking lifetime subscription as active")
                    val activeSubscription = subscription.copy(isActive = true)
                    updateUserSubscription(activeSubscription)
                    return activeSubscription
                }
            }
        } else {
            // Free subscription - ensure it's not marked as active
            if (subscription.isActive) {
                Log.d(TAG, "Free subscription should not be active - updating")
                val freeSubscription = subscription.copy(isActive = false)
                updateUserSubscription(freeSubscription)
                return freeSubscription
            }
        }
        
        return subscription
    }
    
    suspend fun purchaseSubscription(userId: String, plan: SubscriptionPlan): Result<Subscription> {
        return try {
            Log.d(TAG, "Starting purchase for user: $userId, plan: ${plan.name}")
            
            // Check if user has enough coins
            val userRewards = rewardsRepository.getUserRewards(userId)
            Log.d(TAG, "User rewards: $userRewards")
            if (userRewards == null || userRewards.totalCoins < plan.coinPrice) {
                Log.e(TAG, "Insufficient coins: ${userRewards?.totalCoins} < ${plan.coinPrice}")
                return Result.failure(Exception("Insufficient coins"))
            }
            
            // Calculate expiry date
            val expiryDate = if (plan.durationDays != null) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, plan.durationDays)
                Timestamp(calendar.time)
            } else {
                null // Lifetime subscription
            }
            
            // Create new subscription
            val newSubscription = Subscription(
                userId = userId,
                isPremium = true,
                subscriptionType = plan.type,
                purchaseDate = Timestamp.now(),
                expiryDate = expiryDate,
                coinsSpent = plan.coinPrice,
                isActive = true
            )
            
            Log.d(TAG, "Created subscription: $newSubscription")
            Log.d(TAG, "Subscription isPremium: ${newSubscription.isPremium}")
            Log.d(TAG, "Subscription isActive: ${newSubscription.isActive}")
            Log.d(TAG, "Subscription type: ${newSubscription.subscriptionType}")
            Log.d(TAG, "Subscription expiry: ${newSubscription.expiryDate}")
            
            // Deduct coins from user rewards
            val success = rewardsRepository.deductCoins(userId, plan.coinPrice)
            if (!success) {
                Log.e(TAG, "Failed to deduct coins")
                return Result.failure(Exception("Failed to deduct coins"))
            }
            
            Log.d(TAG, "Coins deducted successfully")
            
            // Update subscription in Firebase
            updateUserSubscription(newSubscription)
            
            // Verify the subscription was saved correctly
            kotlinx.coroutines.delay(1000) // Wait for Firebase write to complete
            val savedSubscription = getUserSubscription(userId)
            Log.d(TAG, "Verified saved subscription: $savedSubscription")
            Log.d(TAG, "Saved subscription isPremium: ${savedSubscription?.isPremium}")
            Log.d(TAG, "Saved subscription isActive: ${savedSubscription?.isActive}")
            
            Result.success(newSubscription)
        } catch (e: Exception) {
            Log.e(TAG, "Error purchasing subscription", e)
            Result.failure(e)
        }
    }
    
    private suspend fun updateUserSubscription(subscription: Subscription) {
        try {
            Log.d(TAG, "Updating subscription: $subscription")
            firestore.collection(SUBSCRIPTIONS_COLLECTION)
                .document(subscription.userId)
                .set(subscription)
                .await()
            Log.d(TAG, "Subscription updated successfully")
            
            // Verify the update was successful
            val verifyDocument = firestore.collection(SUBSCRIPTIONS_COLLECTION)
                .document(subscription.userId)
                .get()
                .await()
            
            if (verifyDocument.exists()) {
                val verifiedSubscription = verifyDocument.toObject(Subscription::class.java)
                Log.d(TAG, "Verified subscription after update: $verifiedSubscription")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user subscription", e)
            throw e
        }
    }
    
    fun getAvailablePlans(): List<SubscriptionPlan> {
        return listOf(
            SubscriptionPlan(
                type = SubscriptionType.PREMIUM_MONTHLY,
                name = "Premium Monthly",
                description = "Unlock all premium features for 30 days",
                coinPrice = 2500,
                durationDays = 30,
                features = listOf(
                    "Ad-free experience",
                    "Unlimited cloud storage",
                    "Advanced note organization",
                    "Priority sync",
                    "Premium themes",
                    "Export to multiple formats"
                )
            ),
            SubscriptionPlan(
                type = SubscriptionType.PREMIUM_YEARLY,
                name = "Premium Yearly",
                description = "Best value! Premium features for 365 days",
                coinPrice = 20000,
                durationDays = 365,
                features = listOf(
                    "All monthly features",
                    "Advanced analytics",
                    "Collaboration tools",
                    "Priority customer support",
                    "Early access to new features"
                ),
                isPopular = true
            ),
            SubscriptionPlan(
                type = SubscriptionType.PREMIUM_LIFETIME,
                name = "Premium Lifetime",
                description = "One-time purchase, lifetime access",
                coinPrice = 50000,
                durationDays = null,
                features = listOf(
                    "All premium features forever",
                    "Lifetime updates",
                    "VIP customer support",
                    "Exclusive premium content",
                    "Beta testing access"
                )
            )
        )
    }
    
    suspend fun isPremiumUser(userId: String): Boolean {
        val subscription = getUserSubscription(userId)
        val result = subscription?.isPremium == true && subscription.isActive
        Log.d(TAG, "isPremiumUser check for $userId: subscription=$subscription, result=$result")
        return result
    }
}