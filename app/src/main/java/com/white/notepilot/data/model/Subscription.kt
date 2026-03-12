package com.white.notepilot.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Subscription(
    @PropertyName("userId") val userId: String = "",
    @PropertyName("premium") val isPremium: Boolean = false,
    @PropertyName("subscriptionType") val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    @PropertyName("purchaseDate") val purchaseDate: Timestamp? = null,
    @PropertyName("expiryDate") val expiryDate: Timestamp? = null,
    @PropertyName("coinsSpent") val coinsSpent: Int = 0,
    @PropertyName("active") val isActive: Boolean = false
)

enum class SubscriptionType {
    FREE,
    PREMIUM_MONTHLY,
    PREMIUM_YEARLY,
    PREMIUM_LIFETIME
}

data class SubscriptionPlan(
    val type: SubscriptionType,
    val name: String,
    val description: String,
    val coinPrice: Int,
    val durationDays: Int?, // null for lifetime
    val features: List<String>,
    val isPopular: Boolean = false
)