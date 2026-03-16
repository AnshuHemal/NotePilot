package com.white.notepilot.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.white.notepilot.R
import com.white.notepilot.data.model.Category
import com.white.notepilot.data.model.Note
import com.white.notepilot.data.repository.AiSearchRepository
import com.white.notepilot.ui.state.AiSearchState
import com.white.notepilot.utils.ColorUtils
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay

// ── palette ───────────────────────────────────────────────────────────────────
private val AiGradStart  = Color(0xFF6366F1)
private val AiGradEnd    = Color(0xFF8B5CF6)
private val AiChipBg     = Color(0xFFF5F3FF)
private val AiChipBorder = Color(0xFFDDD6FE)
private val AiChipText   = Color(0xFF4C1D95)
private val ReasonBg     = Color(0xFFF0FDF4)
private val ReasonBorder = Color(0xFFBBF7D0)
private val ReasonText   = Color(0xFF166534)

// ── AI Mode Toggle Bar ────────────────────────────────────────────────────────

@Composable
fun AiSearchModeToggle(
    isAiMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Classic mode chip
        ModeChip(
            label = "Keyword",
            icon = R.drawable.search,
            isActive = !isAiMode,
            activeGradient = null,
            onClick = { if (isAiMode) onToggle() }
        )

        // AI mode chip
        ModeChip(
            label = "AI Search",
            icon = R.drawable.ic_ai_sparkle,
            isActive = isAiMode,
            activeGradient = Brush.horizontalGradient(listOf(AiGradStart, AiGradEnd)),
            onClick = { if (!isAiMode) onToggle() }
        )

        if (isAiMode) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(300)) + expandVertically()
            ) {
                Text(
                    text = "Ask anything naturally",
                    fontSize = 11.sp,
                    color = AiGradStart.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    icon: Int,
    isActive: Boolean,
    activeGradient: Brush?,
    onClick: () -> Unit
) {
    val bgModifier = if (isActive && activeGradient != null) {
        Modifier.background(activeGradient, RoundedCornerShape(50.dp))
    } else if (isActive) {
        Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50.dp))
    } else {
        Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50.dp))
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .then(bgModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── AI Search State Panel ─────────────────────────────────────────────────────
@Composable
fun AiSearchPanel(
    state: AiSearchState,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit,
    onNotePin: (Note) -> Unit,
    categoryViewModel: com.white.notepilot.viewmodel.CategoryViewModel,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            (fadeIn(tween(300)) + slideInVertically { it / 4 }) togetherWith fadeOut(tween(200))
        },
        label = "ai_search_panel",
        modifier = modifier
    ) { currentState ->
        when (currentState) {
            is AiSearchState.Idle -> Box(modifier = Modifier.height(1.dp))

            is AiSearchState.Thinking -> AiThinkingAnimation()

            is AiSearchState.Results -> AiResultsList(
                matches = currentState.matches,
                onNoteClick = onNoteClick,
                onNoteDelete = onNoteDelete,
                onNotePin = onNotePin,
                categoryViewModel = categoryViewModel
            )

            is AiSearchState.Empty -> AiEmptyState(query = currentState.query)

            is AiSearchState.Error -> AiErrorState(message = currentState.message)
        }
    }
}

// ── Thinking animation ────────────────────────────────────────────────────────

@Composable
private fun AiThinkingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_spin"
    )
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "d1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, 160, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "d2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, 320, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "d3"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AiGradStart, AiGradEnd))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ai_sparkle),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotation)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(dotAlpha1, dotAlpha2, dotAlpha3).forEach { alpha ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AiGradStart.copy(alpha = alpha))
                )
            }
        }

        Text(
            text = "AI is searching your notes...",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AiGradStart
        )
        Text(
            text = "Understanding the meaning of your query",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Results list ──────────────────────────────────────────────────────────────

@Composable
private fun AiResultsList(
    matches: List<AiSearchRepository.AiSearchMatch>,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit,
    onNotePin: (Note) -> Unit,
    categoryViewModel: com.white.notepilot.viewmodel.CategoryViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AiGradStart, AiGradEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ai_sparkle),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = "${matches.size} AI result${if (matches.size != 1) "s" else ""}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AiGradStart
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = matches,
                key = { _, match -> match.note.id }
            ) { index, match ->
                AiResultCard(
                    match = match,
                    entryDelayMs = index * 60L,
                    categoryViewModel = categoryViewModel,
                    onNoteClick = { onNoteClick(match.note) },
                    onNoteDelete = { onNoteDelete(match.note) },
                    onNotePin = { onNotePin(match.note) }
                )
            }
        }
    }
}

// ── Individual result card ────────────────────────────────────────────────────

@Composable
private fun AiResultCard(
    match: AiSearchRepository.AiSearchMatch,
    entryDelayMs: Long,
    categoryViewModel: com.white.notepilot.viewmodel.CategoryViewModel,
    onNoteClick: () -> Unit,
    onNoteDelete: () -> Unit,
    onNotePin: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(entryDelayMs)
        visible = true
    }

    val categories by remember(match.note.id) {
        categoryViewModel.getCategoriesForNote(match.note.id)
    }.collectAsState(initial = emptyList())

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialOffsetY = { it / 3 }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(AiGradStart.copy(alpha = 0.3f), AiGradEnd.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Relevance score bar at top
            RelevanceBar(score = match.score)

            // Note card (reuse existing SwipeToDeleteNoteItem)
            SwipeToDeleteNoteItem(
                note = match.note,
                categories = categories,
                onNoteClick = onNoteClick,
                onNoteDelete = onNoteDelete,
                onNotePin = onNotePin
            )

            // AI reason chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AiGradStart, AiGradEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ai_sparkle),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                }
                Text(
                    text = match.reason,
                    fontSize = 12.sp,
                    color = ReasonText,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ReasonBg)
                        .border(1.dp, ReasonBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ── Relevance score bar ───────────────────────────────────────────────────────

@Composable
private fun RelevanceBar(score: Float) {
    val color = when {
        score >= 0.7f -> Color(0xFF10B981)
        score >= 0.5f -> Color(0xFFF59E0B)
        else          -> Color(0xFF6366F1)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(score)
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f)))
                )
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun AiEmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = "No matching notes found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "AI couldn't find notes related to \"$query\". Try rephrasing your query.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ── Error state ───────────────────────────────────────────────────────────────

@Composable
private fun AiErrorState(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error),
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = message,
            fontSize = 13.sp,
            color = Color(0xFF991B1B)
        )
    }
}

// end of file
