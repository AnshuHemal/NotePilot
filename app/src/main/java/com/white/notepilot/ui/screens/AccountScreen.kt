package com.white.notepilot.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.white.notepilot.data.auth.AuthState
import com.white.notepilot.data.model.Subscription
import com.white.notepilot.data.model.SubscriptionType
import com.white.notepilot.data.model.User
import com.white.notepilot.ui.components.CustomPopupDialog
import com.white.notepilot.ui.components.RewardsSection
import com.white.notepilot.ui.components.skeleton.AccountScreenSkeleton
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.LightGray
import com.white.notepilot.ui.theme.NotesTheme
import com.white.notepilot.viewmodel.AuthViewModel
import com.white.notepilot.viewmodel.RewardsViewModel
import com.white.notepilot.viewmodel.SubscriptionViewModel

@Composable
fun AccountScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    rewardsViewModel: RewardsViewModel = hiltViewModel(),
    subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
) {
    var user by remember { mutableStateOf<User?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    val authState by authViewModel.authState.collectAsState()
    val userRewards by rewardsViewModel.userRewards.collectAsState()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    val subscription by subscriptionViewModel.subscription.collectAsState()
    val subscriptionLoading by subscriptionViewModel.isLoading.collectAsState()
    val rewardsLoading by rewardsViewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        user = authViewModel.getCurrentUser()
        user?.uid?.let { userId ->
            rewardsViewModel.loadUserRewards(userId)
            subscriptionViewModel.loadUserSubscription(userId)
        }
    }
    
    LaunchedEffect(navController.currentBackStackEntry) {
        user?.uid?.let { userId ->
            subscriptionViewModel.forceRefreshFromFirebase(userId)
            rewardsViewModel.loadUserRewards(userId)
        }
    }
    
    LaunchedEffect(subscription, isPremium) {
        println("AccountScreen: Subscription loaded - isPremium: $isPremium, isActive: ${subscription?.isActive}")
    }
    
    // Update loading state based on data availability
    LaunchedEffect(user, userRewards, subscription, subscriptionLoading, rewardsLoading) {
        isLoading = user == null || userRewards == null || subscriptionLoading || rewardsLoading
    }
    
    LaunchedEffect(authState) {
        if (authState is AuthState.Idle) {
            navController.navigate(Routes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Loading State with Skeleton
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                AccountScreenSkeleton()
            }
        }
        
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Content State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.PaddingLarge)
            ) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                user?.let { currentUser ->
                    ProfileSection(
                        user = currentUser,
                        isPremium = isPremium,
                        subscription = subscription
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    RewardsSection(
                        userRewards = userRewards,
                        rewardsViewModel = rewardsViewModel,
                        userId = currentUser.uid
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    SubscriptionSection(
                        isPremium = isPremium,
                        subscription = subscription,
                        onUpgradeClick = {
                            navController.navigate(Routes.Subscription.route)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    AccountInfoSection(user = currentUser)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    LogoutButton(
                        onClick = {
                            showLogoutDialog = true
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        if (showLogoutDialog) {
            CustomPopupDialog(
                message = "Are you sure you want to logout?",
                negativeButtonText = "Cancel",
                positiveButtonText = "Logout",
                onNegativeClick = {
                    showLogoutDialog = false
                },
                onPositiveClick = {
                    showLogoutDialog = false
                    authViewModel.signOut()
                }
            )
        }
    }
}

@Composable
private fun ProfileSection(
    user: User,
    isPremium: Boolean,
    subscription: Subscription?,
    modifier: Modifier = Modifier
) {
    val shouldShowPremium = isPremium && subscription != null && subscription.isActive
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(Dimens.PaddingLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box {
            ProfileImage(
                photoUrl = user.photoUrl,
                initials = user.getInitials(),
                modifier = Modifier.size(56.dp)
            )
            
            if (shouldShowPremium) {
                val (crownColor, crownIcon) = when (subscription.subscriptionType) {
                    SubscriptionType.PREMIUM_MONTHLY -> Color(0xFFFF6B35) to Icons.Default.Star
                    SubscriptionType.PREMIUM_YEARLY -> Color(0xFF3B82F6) to Icons.Default.CheckCircle
                    SubscriptionType.PREMIUM_LIFETIME -> Color(0xFFE6A700) to Icons.Default.Star
                    else -> Color(0xFFE6A700) to Icons.Default.Star
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(24.dp)
                        .background(
                            crownColor,
                            CircleShape
                        )
                        .border(
                            2.dp,
                            Color.White,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = crownIcon,
                        contentDescription = "Premium Member",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = user.displayName ?: "User",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = user.email ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            if (shouldShowPremium) {
                Spacer(modifier = Modifier.height(4.dp))
                
                val (badgeColor, badgeText) = when (subscription.subscriptionType) {
                    SubscriptionType.PREMIUM_MONTHLY -> Color(0xFFFF6B35) to "MONTHLY PREMIUM"
                    SubscriptionType.PREMIUM_YEARLY -> Color(0xFF3B82F6) to "YEARLY PREMIUM"
                    SubscriptionType.PREMIUM_LIFETIME -> Color(0xFFE6A700) to "LIFETIME PREMIUM"
                    else -> Color(0xFFE6A700) to "PREMIUM"
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium Badge",
                        tint = badgeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(
                                badgeColor.copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountInfoSection(
    user: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Account Information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        
        InfoItem(
            label = "Email",
            value = user.email ?: "Not available"
        )
        
        InfoItem(
            label = "Display Name",
            value = user.displayName ?: "Not set"
        )
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 14.sp,
            color = LightGray
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun LogoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Logout",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun ProfileImage(
    photoUrl: String?,
    initials: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    NotesTheme {
        AccountScreen(navController = rememberNavController())
    }
}
@Composable
private fun SubscriptionSection(
    isPremium: Boolean,
    subscription: Subscription?,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowPremium = isPremium && subscription != null && subscription.isActive
    
    val (sectionColor, sectionText, sectionDescription) = if (shouldShowPremium) {
        when (subscription.subscriptionType) {
            SubscriptionType.PREMIUM_MONTHLY -> Triple(
                Color(0xFFFF6B35),
                "Monthly Premium Active",
                "Enjoying premium features for 30 days"
            )
            SubscriptionType.PREMIUM_YEARLY -> Triple(
                Color(0xFF3B82F6),
                "Yearly Premium Active",
                "Enjoying premium features for 365 days"
            )
            SubscriptionType.PREMIUM_LIFETIME -> Triple(
                Color(0xFFE6A700),
                "Lifetime Premium Active",
                "Enjoying premium features forever"
            )
            else -> Triple(
                Color(0xFFE6A700),
                "Premium Active",
                "Enjoying all premium features"
            )
        }
    } else {
        Triple(
            Color(0xFF3B82F6),
            "Upgrade to Premium",
            "Unlock exclusive features with coins"
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { 
                if (!shouldShowPremium) {
                    onUpgradeClick() 
                }
            }
            .border(
                width = if (shouldShowPremium) 2.dp else 1.dp,
                color = if (shouldShowPremium) sectionColor.copy(alpha = 0.3f) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (shouldShowPremium) {
                sectionColor.copy(alpha = 0.05f)
            } else {
                Color.White
            }
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                sectionColor,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (shouldShowPremium && subscription.subscriptionType == SubscriptionType.PREMIUM_YEARLY) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.Star
                            },
                            contentDescription = "Premium",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = sectionText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = sectionColor
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = sectionDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    }
                }
                
                if (!shouldShowPremium) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            if (shouldShowPremium) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = sectionColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (subscription.subscriptionType) {
                            SubscriptionType.PREMIUM_MONTHLY -> "Ad-free experience & unlimited storage"
                            SubscriptionType.PREMIUM_YEARLY -> "All premium features + priority support"
                            SubscriptionType.PREMIUM_LIFETIME -> "All features forever + VIP support"
                            else -> "Ad-free experience & unlimited storage"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF374151),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}