package com.white.notepilot.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.white.notepilot.R
import com.white.notepilot.ui.state.AiSummaryState

private val AiPurple = Color(0xFF7C3AED)
private val AiPurpleLight = Color(0xFFEDE9FE)
private val AiPurpleMid = Color(0xFFA78BFA)
private val AiBlue = Color(0xFF3B82F6)
private val AiGradientStart = Color(0xFF7C3AED)
private val AiGradientEnd = Color(0xFF3B82F6)

@Composable
fun AiSummaryCard(
    summaryState: AiSummaryState,
    noteBackgroundColor: Color,
    onSummarizeClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkBackground = noteBackgroundColor.luminance() < 0.5f
    val cardBackground = if (isDarkBackground)
        Color.White.copy(alpha = 0.08f)
    else
        Color.White.copy(alpha = 0.85f)

    val borderColor = if (isDarkBackground)
        AiPurpleMid.copy(alpha = 0.5f)
    else
        AiPurple.copy(alpha = 0.3f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Trigger button — always visible
        AiSummarizeButton(
            summaryState = summaryState,
            isDarkBackground = isDarkBackground,
            onClick = onSummarizeClick
        )

        // Expandable result card
        AnimatedVisibility(
            visible = summaryState !is AiSummaryState.Idle,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(300)),
            exit = shrinkVertically(tween(250)) + fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBackground)
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = summaryState,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                    },
                    label = "summary_content"
                ) { state ->
                    when (state) {
                        is AiSummaryState.Loading -> SummaryLoadingContent(isDarkBackground)
                        is AiSummaryState.Success -> SummarySuccessContent(
                            summary = state.summary,
                            isDarkBackground = isDarkBackground,
                            onDismiss = onDismiss
                        )
                        is AiSummaryState.Error -> SummaryErrorContent(
                            message = state.message,
                            isDarkBackground = isDarkBackground,
                            onDismiss = onDismiss
                        )
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun AiSummarizeButton(
    summaryState: AiSummaryState,
    isDarkBackground: Boolean,
    onClick: () -> Unit
) {
    val isLoading = summaryState is AiSummaryState.Loading
    val hasResult = summaryState is AiSummaryState.Success

    val infiniteTransition = rememberInfiniteTransition(label = "ai_shimmer")
    val shimmerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_rotate"
    )

    val buttonAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0.7f else 1f,
        animationSpec = tween(300),
        label = "button_alpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(AiGradientStart, AiGradientEnd)
                ),
                alpha = buttonAlpha
            )
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Animated AI sparkle icon
        Box(
            modifier = Modifier
                .size(20.dp)
                .rotate(if (isLoading) shimmerAngle else 0f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ai_sparkle),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = when {
                isLoading -> "Summarizing..."
                hasResult -> "Re-summarize"
                else -> "AI Summary"
            },
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SummaryLoadingContent(isDarkBackground: Boolean) {
    val textColor = if (isDarkBackground) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280)
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AiPurple.copy(alpha = alpha))
            )
            Text(
                text = "Generating summary...",
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // Skeleton lines
        repeat(3) { index ->
            val lineAlpha by rememberInfiniteTransition(label = "line_$index").animateFloat(
                initialValue = 0.2f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900 + index * 150, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "line_alpha_$index"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (index == 2) 0.6f else 1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AiPurple.copy(alpha = lineAlpha))
            )
        }
    }
}

@Composable
private fun SummarySuccessContent(
    summary: String,
    isDarkBackground: Boolean,
    onDismiss: () -> Unit
) {
    val textColor = if (isDarkBackground) Color.White else Color(0xFF1F2937)
    val labelColor = AiPurple

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AiGradientStart, AiGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ai_sparkle),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "AI Summary",
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "Dismiss",
                tint = if (isDarkBackground) Color.White.copy(alpha = 0.5f) else Color(0xFF9CA3AF),
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onDismiss)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary bullet points
        val lines = summary.split("\n").filter { it.isNotBlank() }
        lines.forEachIndexed { index, line ->
            var visible by remember { mutableStateOf(false) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(index * 80L)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + expandVertically(tween(300))
            ) {
                Text(
                    text = line,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = if (index < lines.lastIndex) 6.dp else 0.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Powered by label
        Text(
            text = "Powered by Gemini",
            color = if (isDarkBackground) Color.White.copy(alpha = 0.35f) else Color(0xFFD1D5DB),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SummaryErrorContent(
    message: String,
    isDarkBackground: Boolean,
    onDismiss: () -> Unit
) {
    val textColor = if (isDarkBackground) Color.White.copy(alpha = 0.8f) else Color(0xFF374151)
    val errorColor = Color(0xFFEF4444)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_error),
                contentDescription = null,
                tint = errorColor,
                modifier = Modifier.size(16.dp).padding(top = 2.dp)
            )
            Text(
                text = message,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = "Dismiss",
            tint = if (isDarkBackground) Color.White.copy(alpha = 0.5f) else Color(0xFF9CA3AF),
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onDismiss)
        )
    }
}
