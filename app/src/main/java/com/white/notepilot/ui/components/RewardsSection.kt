package com.white.notepilot.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.white.notepilot.data.model.UserRewards
import com.white.notepilot.ui.theme.Blue
import com.white.notepilot.ui.theme.LightGray
import com.white.notepilot.ui.theme.White
import com.white.notepilot.viewmodel.RewardsViewModel
import kotlinx.coroutines.delay

@Composable
fun RewardsSection(
    userRewards: UserRewards?,
    rewardsViewModel: RewardsViewModel,
    userId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val canWatchAd by rewardsViewModel.canWatchAd.collectAsState()
    val adsWatchedInHour by rewardsViewModel.adsWatchedInHour.collectAsState()
    val nextAdReward by rewardsViewModel.nextAdReward.collectAsState()
    val timeUntilNextAd by rewardsViewModel.timeUntilNextAd.collectAsState()
    val isAdAvailable by rewardsViewModel.isAdAvailable.collectAsState()
    val isAdLoading by rewardsViewModel.isAdLoading.collectAsState()
    val showRewardDialog by rewardsViewModel.showRewardDialog.collectAsState()
    val lastEarnedCoins by rewardsViewModel.lastEarnedCoins.collectAsState()
    
    var timeRemaining by remember { mutableLongStateOf(timeUntilNextAd) }
    
    LaunchedEffect(timeUntilNextAd) {
        timeRemaining = timeUntilNextAd
        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining -= 1000
            if (timeRemaining <= 0) {
                rewardsViewModel.refreshRewardsData(userId)
            }
        }
    }
    
    LaunchedEffect(Unit) {
        rewardsViewModel.loadRewardedAd(context)
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Earn Rewards",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        
        RewardsCard(
            userRewards = userRewards,
            canWatchAd = canWatchAd,
            adsWatchedInHour = adsWatchedInHour,
            nextAdReward = nextAdReward,
            timeRemaining = timeRemaining,
            isAdAvailable = isAdAvailable,
            isAdLoading = isAdLoading,
            onWatchAd = {
                if (context is Activity) {
                    rewardsViewModel.watchRewardedAd(context, userId)
                }
            }
        )
    }
    
    if (showRewardDialog) {
        RewardEarnedDialog(
            coinsEarned = lastEarnedCoins,
            onDismiss = {
                rewardsViewModel.dismissRewardDialog()
            }
        )
    }
}

@Composable
private fun RewardsCard(
    userRewards: UserRewards?,
    canWatchAd: Boolean,
    adsWatchedInHour: Int,
    nextAdReward: Int,
    timeRemaining: Long,
    isAdAvailable: Boolean,
    isAdLoading: Boolean,
    onWatchAd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CoinsDisplay(totalCoins = userRewards?.totalCoins ?: 0)
            
            AdProgressSection(
                adsWatchedInHour = adsWatchedInHour,
                maxAds = 5
            )
            
            if (canWatchAd && timeRemaining <= 0) {
                WatchAdButton(
                    nextReward = nextAdReward,
                    isAdAvailable = isAdAvailable,
                    isAdLoading = isAdLoading,
                    onClick = onWatchAd
                )
            } else {
                NextAdTimer(timeRemaining = timeRemaining)
            }
        }
    }
}

@Composable
private fun CoinsDisplay(
    totalCoins: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Total Coins",
                style = MaterialTheme.typography.bodySmall,
                color = LightGray,
                fontSize = 14.sp
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Coins",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = totalCoins.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AdProgressSection(
    adsWatchedInHour: Int,
    maxAds: Int,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = adsWatchedInHour.toFloat() / maxAds.toFloat(),
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ads Watched This Hour",
                style = MaterialTheme.typography.bodySmall,
                color = LightGray,
                fontSize = 14.sp
            )
            
            Text(
                text = "$adsWatchedInHour/$maxAds",
                style = MaterialTheme.typography.bodySmall,
                color = Blue,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Blue,
            trackColor = Blue.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun WatchAdButton(
    nextReward: Int,
    isAdAvailable: Boolean,
    isAdLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = isAdAvailable && !isAdLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue,
            disabledContainerColor = Blue.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isAdLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Blue,
                strokeWidth = 2.dp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Loading Ad...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = White
            )
        } else {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Watch Ad",
                tint = White
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Watch Ad (+$nextReward coins)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = White
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun NextAdTimer(
    timeRemaining: Long,
    modifier: Modifier = Modifier
) {
    val minutes = (timeRemaining / 1000 / 60).toInt()
    val seconds = ((timeRemaining / 1000) % 60).toInt()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (timeRemaining > 0) {
            Text(
                text = "Next ad available in ${String.format("%02d:%02d", minutes, seconds)}",
                style = MaterialTheme.typography.bodyMedium,
                color = LightGray,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = "Loading next ad...",
                style = MaterialTheme.typography.bodyMedium,
                color = LightGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}