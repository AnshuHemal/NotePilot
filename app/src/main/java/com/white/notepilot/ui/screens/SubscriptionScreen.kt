package com.white.notepilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.white.notepilot.R
import com.white.notepilot.data.model.Subscription
import com.white.notepilot.data.model.SubscriptionPlan
import com.white.notepilot.ui.components.CustomPopupDialog
import com.white.notepilot.ui.components.SingleButtonDialog
import com.white.notepilot.ui.components.CustomTopBar
import com.white.notepilot.ui.theme.NotesTheme
import com.white.notepilot.viewmodel.AuthViewModel
import com.white.notepilot.viewmodel.RewardsViewModel
import com.white.notepilot.viewmodel.SubscriptionViewModel

@Composable
fun SubscriptionScreen(
    navController: NavHostController,
    subscriptionViewModel: SubscriptionViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    rewardsViewModel: RewardsViewModel = hiltViewModel()
) {
    val availablePlans by subscriptionViewModel.availablePlans.collectAsState()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    val isLoading by subscriptionViewModel.isLoading.collectAsState()
    val userRewards by rewardsViewModel.userRewards.collectAsState()
    val showConfirmationDialog by subscriptionViewModel.showConfirmationDialog.collectAsState()
    val showSuccessDialog by subscriptionViewModel.showSuccessDialog.collectAsState()
    val showErrorDialog by subscriptionViewModel.showErrorDialog.collectAsState()
    val errorMessage by subscriptionViewModel.errorMessage.collectAsState()
    val selectedPlan by subscriptionViewModel.selectedPlan.collectAsState()
    val subscription by subscriptionViewModel.subscription.collectAsState()
    val currentUser = authViewModel.getCurrentUser()
    
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            subscriptionViewModel.loadUserSubscription(userId)
            rewardsViewModel.loadUserRewards(userId)
        }
    }
    
    // Refresh data when screen becomes visible
    LaunchedEffect(navController.currentBackStackEntry) {
        currentUser?.uid?.let { userId ->
            println("SubscriptionScreen: Refreshing data for user $userId")
            subscriptionViewModel.refreshSubscription(userId)
            rewardsViewModel.loadUserRewards(userId)
        }
    }
    
    // Debug subscription state changes
    LaunchedEffect(subscription, isPremium) {
        println("SubscriptionScreen: Subscription state changed")
        println("SubscriptionScreen: subscription = $subscription")
        println("SubscriptionScreen: isPremium = $isPremium")
        println("SubscriptionScreen: subscriptionType = ${subscription?.subscriptionType}")
        println("SubscriptionScreen: isActive = ${subscription?.isActive}")
    }
    
    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                // Inline Header (same as AboutScreen)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.back_arrow),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { navController.popBackStack() },
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "Subscriptions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            HeaderSection(
                                currentCoins = userRewards?.totalCoins ?: 0
                            )
                        }
                        
                        items(availablePlans) { plan ->
                            SubscriptionPlanCard(
                                plan = plan,
                                currentCoins = userRewards?.totalCoins ?: 0,
                                isPremium = isPremium,
                                subscription = subscription,
                                onPurchaseClick = {
                                    // Show confirmation dialog instead of direct purchase
                                    subscriptionViewModel.showConfirmationDialog(plan)
                                }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
    
    // Dialogs
    
    // Confirmation Dialog
    if (showConfirmationDialog && selectedPlan != null) {
        PurchaseConfirmationDialog(
            plan = selectedPlan!!,
            currentCoins = userRewards?.totalCoins ?: 0,
            onConfirm = {
                currentUser?.uid?.let { userId ->
                    subscriptionViewModel.confirmPurchase(userId)
                }
            },
            onCancel = {
                subscriptionViewModel.hideConfirmationDialog()
            }
        )
    }
    
    // Success Dialog (only positive button)
    if (showSuccessDialog) {
        SingleButtonDialog(
            message = "🎉 Premium subscription activated successfully! Enjoy all premium features.",
            buttonText = "Great!",
            onButtonClick = {
                subscriptionViewModel.hideSuccessDialog()
                // Force refresh data before going back
                currentUser?.uid?.let { userId ->
                    subscriptionViewModel.forceRefreshFromFirebase(userId)
                }
                navController.popBackStack()
            }
        )
    }
    
    // Error Dialog
    if (showErrorDialog) {
        CustomPopupDialog(
            message = "❌ $errorMessage",
            negativeButtonText = "Cancel",
            positiveButtonText = "OK",
            onNegativeClick = {
                subscriptionViewModel.hideErrorDialog()
            },
            onPositiveClick = {
                subscriptionViewModel.hideErrorDialog()
            }
        )
    }
}

@Composable
private fun HeaderSection(
    currentCoins: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Choose the perfect plan for your needs.\nUpgrade with your earned coins.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Coins Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    Color.White,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    Color(0xFFE5E7EB),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Coins",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$currentCoins Coins Available",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    currentCoins: Int,
    isPremium: Boolean,
    subscription: Subscription?,
    onPurchaseClick: () -> Unit
) {
    val canAfford = currentCoins >= plan.coinPrice
    val isCurrentPlan = isPremium && subscription?.subscriptionType == plan.type && subscription.isActive
    
    // Debug logging for current plan detection
    println("SubscriptionPlanCard: Plan ${plan.name}")
    println("SubscriptionPlanCard: isPremium = $isPremium")
    println("SubscriptionPlanCard: subscription?.subscriptionType = ${subscription?.subscriptionType}")
    println("SubscriptionPlanCard: plan.type = ${plan.type}")
    println("SubscriptionPlanCard: subscription?.isActive = ${subscription?.isActive}")
    println("SubscriptionPlanCard: isCurrentPlan = $isCurrentPlan")
    
    // Define distinct colors based on plan type with better contrast
    val (planColor, buttonColor, borderColor) = when {
        plan.name.contains("Monthly", ignoreCase = true) -> 
            Triple(Color(0xFFFF6B35), Color(0xFFFF6B35), Color(0xFFFFE5DC))
        plan.name.contains("Yearly", ignoreCase = true) -> 
            Triple(Color(0xFF3B82F6), Color(0xFF3B82F6), Color(0xFFDCEAFF))
        plan.name.contains("Lifetime", ignoreCase = true) -> 
            Triple(Color(0xFFE6A700), Color(0xFFE6A700), Color(0xFFFFF4CC))
        else -> 
            Triple(Color(0xFF6B7280), Color(0xFF6B7280), Color(0xFFE5E7EB))
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (plan.isPopular || isCurrentPlan) planColor.copy(alpha = 0.3f) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (plan.isPopular || isCurrentPlan) borderColor else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (plan.isPopular || isCurrentPlan) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Popular Badge or Current Plan Badge
            if (plan.isPopular || isCurrentPlan) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = if (isCurrentPlan) "CURRENT PLAN" else "MOST POPULAR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = planColor,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(
                                planColor.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Price Section
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${plan.coinPrice}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    fontSize = 42.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "coins",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6B7280),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Plan Name
            Text(
                text = plan.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = planColor,
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Plan Description
            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6B7280),
                lineHeight = 22.sp,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Features List
            plan.features.take(4).forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Check",
                        tint = planColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF374151),
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Purchase Button
            Button(
                onClick = onPurchaseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = canAfford && !isCurrentPlan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrentPlan) Color(0xFF10B981) else buttonColor,
                    contentColor = Color.White,
                    disabledContainerColor = if (isCurrentPlan) Color(0xFF10B981) else Color(0xFFE5E7EB),
                    disabledContentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when {
                        isCurrentPlan -> "YOUR CURRENT PLAN"
                        !canAfford -> "INSUFFICIENT COINS"
                        else -> "UPGRADE TO PREMIUM"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun PurchaseConfirmationDialog(
    plan: SubscriptionPlan,
    currentCoins: Int,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val (planColor, planIcon) = when {
        plan.name.contains("Monthly", ignoreCase = true) -> 
            Color(0xFFFF6B35) to Icons.Default.Star
        plan.name.contains("Yearly", ignoreCase = true) -> 
            Color(0xFF3B82F6) to Icons.Default.Star
        plan.name.contains("Lifetime", ignoreCase = true) -> 
            Color(0xFFE6A700) to Icons.Default.Star
        else -> 
            Color(0xFF6B7280) to Icons.Default.Star
    }
    
    Dialog(onDismissRequest = { /* Non-dismissible */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Plan Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            planColor.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = planIcon,
                        contentDescription = "Plan Icon",
                        tint = planColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Title
                Text(
                    text = "Confirm Purchase",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Plan Name
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = planColor,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cost Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8F9FA)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cost:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${plan.coinPrice}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = " coins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Coins:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$currentCoins",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentCoins >= plan.coinPrice) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                                Text(
                                    text = " coins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                        
                        if (currentCoins >= plan.coinPrice) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "After Purchase:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${currentCoins - plan.coinPrice}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = " coins",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = "Are you sure you want to purchase ${plan.name}?\nThis action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3F4F6),
                            contentColor = Color(0xFF6B7280)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        enabled = currentCoins >= plan.coinPrice,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = planColor,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE5E7EB),
                            disabledContentColor = Color(0xFF9CA3AF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Confirm",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubscriptionScreenPreview() {
    NotesTheme {
        SubscriptionScreen(navController = rememberNavController())
    }
}