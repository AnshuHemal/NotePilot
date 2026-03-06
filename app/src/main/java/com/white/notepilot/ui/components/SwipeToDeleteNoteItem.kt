package com.white.notepilot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.white.notepilot.R
import com.white.notepilot.data.model.Note
import com.white.notepilot.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteNoteItem(
    note: Note,
    categories: List<com.white.notepilot.data.model.Category> = emptyList(),
    onNoteClick: () -> Unit,
    onNoteDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
                false
            } else {
                false
            }
        },
        positionalThreshold = { distance -> distance * 0.5f }
    )

    LaunchedEffect(showDeleteDialog) {
        if (!showDeleteDialog) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.PaddingMedium)
                    .background(
                        color = Color.Red,
                        shape = RoundedCornerShape(Dimens.PaddingMedium)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        NoteComponent(
            title = note.title,
            colorCode = note.colorCode,
            categories = categories,
            onClick = onNoteClick
        )
    }

    if (showDeleteDialog) {
        CustomPopupDialog(
            message = stringResource(R.string.delete_note),
            negativeButtonText = stringResource(R.string.cancel),
            positiveButtonText = stringResource(R.string.delete),
            onDismiss = {
                showDeleteDialog = false
            },
            onNegativeClick = {
                showDeleteDialog = false
            },
            onPositiveClick = {
                showDeleteDialog = false
                onNoteDelete()
            }
        )
    }
}
