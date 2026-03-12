package com.white.notepilot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.data.model.Subscription
import com.white.notepilot.data.model.SubscriptionPlan
import com.white.notepilot.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    
    private val _subscription = MutableStateFlow<Subscription?>(null)
    val subscription: StateFlow<Subscription?> = _subscription.asStateFlow()
    
    private val _availablePlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val availablePlans: StateFlow<List<SubscriptionPlan>> = _availablePlans.asStateFlow()
    
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _purchaseResult = MutableStateFlow<PurchaseResult?>(null)
    val purchaseResult: StateFlow<PurchaseResult?> = _purchaseResult.asStateFlow()
    
    private val _showPurchaseDialog = MutableStateFlow(false)
    val showPurchaseDialog: StateFlow<Boolean> = _showPurchaseDialog.asStateFlow()
    
    private val _showConfirmationDialog = MutableStateFlow(false)
    val showConfirmationDialog: StateFlow<Boolean> = _showConfirmationDialog.asStateFlow()
    
    private val _showSuccessDialog = MutableStateFlow(false)
    val showSuccessDialog: StateFlow<Boolean> = _showSuccessDialog.asStateFlow()
    
    private val _showErrorDialog = MutableStateFlow(false)
    val showErrorDialog: StateFlow<Boolean> = _showErrorDialog.asStateFlow()
    
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()
    
    private val _selectedPlan = MutableStateFlow<SubscriptionPlan?>(null)
    val selectedPlan: StateFlow<SubscriptionPlan?> = _selectedPlan.asStateFlow()
    
    init {
        loadAvailablePlans()
    }
    
    fun loadUserSubscription(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val subscription = subscriptionRepository.getUserSubscription(userId)
                _subscription.value = subscription
                
                // Calculate premium status
                val premiumStatus = subscription?.isPremium == true && subscription.isActive
                _isPremium.value = premiumStatus
                
                println("SubscriptionViewModel: Loaded subscription for user $userId")
                println("SubscriptionViewModel: isPremium = ${subscription?.isPremium}, isActive = ${subscription?.isActive}")
                println("SubscriptionViewModel: calculated premiumStatus = $premiumStatus")
            } catch (e: Exception) {
                println("SubscriptionViewModel: Error loading subscription: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadAvailablePlans() {
        _availablePlans.value = subscriptionRepository.getAvailablePlans()
    }
    
    fun purchaseSubscription(userId: String, plan: SubscriptionPlan) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                println("SubscriptionViewModel: Starting purchase for plan ${plan.name}")
                val result = subscriptionRepository.purchaseSubscription(userId, plan)
                
                if (result.isSuccess) {
                    val newSubscription = result.getOrNull()
                    println("SubscriptionViewModel: Purchase successful, subscription = $newSubscription")
                    
                    // Update local state immediately
                    _subscription.value = newSubscription
                    _isPremium.value = newSubscription?.isPremium == true && newSubscription.isActive
                    _purchaseResult.value = PurchaseResult.Success("Premium subscription activated!")
                    _showPurchaseDialog.value = false
                    _showSuccessDialog.value = true
                    
                    // Force refresh from Firebase to ensure consistency
                    println("SubscriptionViewModel: Force refreshing subscription data")
                    kotlinx.coroutines.delay(1000) // Small delay to ensure Firebase write is complete
                    loadUserSubscription(userId)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Purchase failed"
                    println("SubscriptionViewModel: Purchase failed: $error")
                    _purchaseResult.value = PurchaseResult.Error(error)
                    _errorMessage.value = error
                    _showErrorDialog.value = true
                }
            } catch (e: Exception) {
                println("SubscriptionViewModel: Purchase exception: ${e.message}")
                _purchaseResult.value = PurchaseResult.Error(e.message ?: "Unknown error")
                _errorMessage.value = e.message ?: "Unknown error"
                _showErrorDialog.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun showPurchaseDialog(plan: SubscriptionPlan) {
        _selectedPlan.value = plan
        _showPurchaseDialog.value = true
    }
    
    fun hidePurchaseDialog() {
        _showPurchaseDialog.value = false
        _selectedPlan.value = null
    }
    
    fun showConfirmationDialog(plan: SubscriptionPlan) {
        _selectedPlan.value = plan
        _showConfirmationDialog.value = true
    }
    
    fun hideConfirmationDialog() {
        _showConfirmationDialog.value = false
        _selectedPlan.value = null
    }
    
    fun confirmPurchase(userId: String) {
        _selectedPlan.value?.let { plan ->
            _showConfirmationDialog.value = false
            purchaseSubscription(userId, plan)
        }
    }
    
    fun clearPurchaseResult() {
        _purchaseResult.value = null
    }
    
    fun refreshSubscription(userId: String) {
        println("SubscriptionViewModel: Force refreshing subscription for user $userId")
        loadUserSubscription(userId)
    }
    
    fun forceRefreshFromFirebase(userId: String) {
        viewModelScope.launch {
            try {
                println("SubscriptionViewModel: Force refresh from Firebase")
                // Clear current state first
                _subscription.value = null
                _isPremium.value = false
                
                // Small delay to ensure any pending writes are complete
                kotlinx.coroutines.delay(1000)
                
                // Get subscription without expiry check first to see raw data
                val rawSubscription = subscriptionRepository.getSubscriptionWithoutExpiryCheck(userId)
                println("SubscriptionViewModel: Raw subscription from Firebase: $rawSubscription")
                
                // Then reload with expiry check
                loadUserSubscription(userId)
            } catch (e: Exception) {
                println("SubscriptionViewModel: Error in force refresh: ${e.message}")
            }
        }
    }
    
    fun hideSuccessDialog() {
        _showSuccessDialog.value = false
    }
    
    fun hideErrorDialog() {
        _showErrorDialog.value = false
    }
}

sealed class PurchaseResult {
    data class Success(val message: String) : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}