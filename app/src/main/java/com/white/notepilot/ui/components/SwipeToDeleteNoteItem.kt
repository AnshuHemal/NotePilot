package com.white.notepilot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.white.notepilot.R
import com.white.notepilot.data.model.Note
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.utils.HapticFeedbackHelper
import com.white.notepilot.utils.rememberHapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteNoteItem(
    note: Note,
    categories: List<com.white.notepilot.data.model.Category> = emptyList(),
    onNoteClick: () -> Unit,
    onNoteDelete: () -> Unit,
    onNotePin: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic(HapticFeedbackHelper.HapticType.WARNING)
                    showDeleteDialog = true
                    false
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
                    onNotePin()
                    false
                }
                else -> false
            }
        },
        positionalThreshold = { distance -> distance * 0.4f }
    )

    LaunchedEffect(showDeleteDialog) {
        if (!showDeleteDialog) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFFC107)
                SwipeToDismissBoxValue.EndToStart -> Color.Red
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.PaddingMedium)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(Dimens.PaddingMedium)
                    ),
                contentAlignment = alignment
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Icon(
                            painter = painterResource(id = R.drawable.pin),
                            contentDescription = "Pin",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(horizontal = Dimens.PaddingLarge)
                                .size(28.dp)
                        )
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(horizontal = Dimens.PaddingLarge)
                                .size(28.dp)
                        )
                    }
                    else -> {}
                }
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    ) {
        NoteComponent(
            title = note.title,
            colorCode = note.colorCode,
            isPinned = note.isPinned,
            isLocked = note.isLocked,
            categories = categories,
            onClick = onNoteClick
        )
    }

    if (showDeleteDialog) {
        CustomPopupDialog(
            message = stringResource(R.string.delete_note),
            negativeButtonText = stringResource(R.string.cancel),
            positiveButtonText = stringResource(R.string.delete),
            onNegativeClick = {
                showDeleteDialog = false
            },
            onPositiveClick = {
                haptic(HapticFeedbackHelper.HapticType.ERROR)
                showDeleteDialog = false
                onNoteDelete()
            }
        )
    }
}
