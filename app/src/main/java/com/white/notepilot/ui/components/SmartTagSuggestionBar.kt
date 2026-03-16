package com.white.notepilot.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.white.notepilot.R
import com.white.notepilot.data.ml.SmartTagEngine
import com.white.notepilot.ui.state.SmartTagState
import kotlinx.coroutines.delay

// ── palette ──────────────────────────────────────────────────────────────────
private val TagGradientStart = Color(0xFF6366F1)   // indigo
private val TagGradientEnd   = Color(0xFF8B5CF6)   // violet
private val TagChipBg        = Color(0xFFF5F3FF)
private val TagChipBorder    = Color(0xFFDDD6FE)
private val TagChipText      = Color(0xFF4C1D95)
private val TagChipIcon      = Color(0xFF7C3AED)

/**
 * Animated bar that appears below the category chips in CreateNoteScreen.
 * Shows AI-suggested category chips the user can tap to add instantly.
 */
@Composable
fun SmartTagSuggestionBar(
    state: SmartTagState,
    onAccept: (SmartTagEngine.TagSuggestion) -> Unit,
    onDismiss: (SmartTagEngine.TagSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = state is SmartTagState.Analysing || state is SmartTagState.Ready

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(tween(300)),
        exit = shrinkVertically(tween(250)) + fadeOut(tween(200)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            // ── header row ────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                SparkleIcon(isSpinning = state is SmartTagState.Analysing)

                Text(
                    text = when (state) {
                        is SmartTagState.Analysing -> "Analysing note..."
                        is SmartTagState.Ready     -> "Suggested tags"
                        else                       -> ""
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TagGradientStart
                )
            }

            // ── chip row ──────────────────────────────────────────────────────
            if (state is SmartTagState.Ready) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.suggestions.forEachIndexed { index, suggestion ->
                        AnimatedSuggestionChip(
                            suggestion = suggestion,
                            entryDelayMs = index * 80L,
                            onAccept = { onAccept(suggestion) },
                            onDismiss = { onDismiss(suggestion) }
                        )
                    }
                }
            } else {
                // Skeleton shimmer while analysing
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        ShimmerChip(widthFraction = if (index == 1) 0.22f else 0.18f)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// ── individual animated chip ──────────────────────────────────────────────────

@Composable
private fun AnimatedSuggestionChip(
    suggestion: SmartTagEngine.TagSuggestion,
    entryDelayMs: Long,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(entryDelayMs)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "chip_scale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(tween(250)),
        exit = shrinkHorizontally(tween(200)) + fadeOut(tween(150))
    ) {
        Row(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(50.dp))
                .background(TagChipBg)
                .border(1.dp, TagChipBorder, RoundedCornerShape(50.dp))
                .clickable {
                    pressed = true
                    onAccept()
                }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Confidence dot
            ConfidenceDot(confidence = suggestion.confidence)

            Text(
                text = suggestion.categoryName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TagChipText
            )

            // Dismiss ×
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(TagChipBorder)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Dismiss",
                    tint = TagChipIcon,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

// ── confidence dot (green / amber / red) ─────────────────────────────────────

@Composable
private fun ConfidenceDot(confidence: Float) {
    val color = when {
        confidence >= 0.5f -> Color(0xFF10B981)  // green
        confidence >= 0.3f -> Color(0xFFF59E0B)  // amber
        else               -> Color(0xFFEF4444)  // red
    }
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// ── sparkle icon with optional spin ──────────────────────────────────────────

@Composable
private fun SparkleIcon(isSpinning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_rotation"
    )

    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(TagGradientStart, TagGradientEnd))
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_ai_sparkle),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(11.dp)
                .rotate(if (isSpinning) rotation else 0f)
        )
    }
}

// ── shimmer skeleton chip ─────────────────────────────────────────────────────

@Composable
private fun ShimmerChip(widthFraction: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(32.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(TagChipBorder.copy(alpha = alpha))
    )
}
