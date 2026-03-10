package com.white.notepilot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.white.notepilot.ui.theme.Dimens
import kotlinx.coroutines.delay

@Composable
fun CustomSnackbar(
    message: Int,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    duration: Long = 2000L,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    var showSnackbar by remember { mutableStateOf(isVisible) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            showSnackbar = true
            delay(duration)
            showSnackbar = false
            delay(300)
            onDismiss()
        }
    }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = showSnackbar,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(durationMillis = 300)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.PaddingMedium)
                    .imePadding()
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(Dimens.PaddingSmall)
                    )
                    .padding(
                        horizontal = Dimens.PaddingLarge,
                        vertical = Dimens.PaddingMedium
                    )
                    ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CustomSnackbar(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    duration: Long = 2000L,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    var showSnackbar by remember { mutableStateOf(isVisible) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            showSnackbar = true
            delay(duration)
            showSnackbar = false
            delay(300)
            onDismiss()
        }
    }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = showSnackbar,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(durationMillis = 300)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.PaddingMedium)
                    .imePadding()
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(Dimens.PaddingSmall)
                    )
                    .padding(
                        horizontal = Dimens.PaddingLarge,
                        vertical = Dimens.PaddingMedium
                    )
                    ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 24.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}