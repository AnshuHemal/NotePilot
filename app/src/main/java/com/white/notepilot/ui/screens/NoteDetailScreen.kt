package com.white.notepilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.white.notepilot.R
import com.white.notepilot.data.model.Note
import com.white.notepilot.ui.components.CustomTopBar
import com.white.notepilot.ui.components.ShareBottomSheet
import com.white.notepilot.ui.events.NotesEvent
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.NotesTheme
import com.white.notepilot.utils.ColorUtils
import com.white.notepilot.utils.ShareHelper
import com.white.notepilot.viewmodel.NotesViewModel

@Composable
fun NoteDetailScreen(
    navController: NavHostController,
    noteId: Int,
    viewModel: NotesViewModel = hiltViewModel(),
    authViewModel: com.white.notepilot.viewmodel.AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(noteId) {
        viewModel.onEvent(NotesEvent.GetNoteById(noteId))
    }

    uiState.selectedNote?.let { note ->
        NoteDetailScreenContent(
            note = note,
            navController = navController,
            onSaveNote = { updatedNote ->
                val userId = authViewModel.getCurrentUser()?.uid ?: ""
                viewModel.onEvent(NotesEvent.UpsertNote(updatedNote, userId))
            }
        )
    }
}

@Composable
private fun NoteDetailScreenContent(
    note: Note,
    navController: NavHostController,
    onSaveNote: (Note) -> Unit
) {
    val context = LocalContext.current
    val richTextState = rememberRichTextState()
    val codeBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val codeStrokeColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    var showShareBottomSheet by remember { mutableStateOf(false) }
    
    LaunchedEffect(note.content) {
        richTextState.setHtml(note.content)
    }
    
    // Configure code styling
    LaunchedEffect(codeBackgroundColor, codeStrokeColor) {
        richTextState.config.codeSpanColor = androidx.compose.ui.graphics.Color.Unspecified
        richTextState.config.codeSpanBackgroundColor = codeBackgroundColor
        richTextState.config.codeSpanStrokeColor = codeStrokeColor
    }
    
    Scaffold(
        containerColor = Color(ColorUtils.colorCodeToInt(note.colorCode)),
        topBar = {
            CustomTopBar(
                leftIconRes = R.drawable.back_arrow,
                onLeftIconClick = {
                    navController.popBackStack()
                },
                rightIconRes = R.drawable.edit,
                onRightIconClick = {
                    navController.navigate(Routes.CreateNote.createRoute(note.id))
                },
                customContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Share button
                        IconButton(
                            onClick = { showShareBottomSheet = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.share),
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Edit button
                        IconButton(
                            onClick = {
                                navController.navigate(Routes.CreateNote.createRoute(note.id))
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.edit),
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.PaddingLarge)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.PaddingSmall)
            )

            RichText(
                state = richTextState,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.PaddingSmall)
            )
        }
        
        // Share Bottom Sheet
        if (showShareBottomSheet) {
            ShareBottomSheet(
                onDismiss = { showShareBottomSheet = false },
                onShareAsText = {
                    ShareHelper.shareAsText(context, note)
                },
                onShareAsHtml = {
                    ShareHelper.shareAsHtml(context, note)
                },
                onShareAsPdf = {
                    ShareHelper.shareAsPdf(context, note)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteDetailScreenPreview() {
    NotesTheme {
        NoteDetailScreenContent(
            note = Note(
                id = 1,
                title = "Book Review : The Design of Everyday Things by Don Norman",
                content = "The Design of Everyday Things is required reading for anyone who is interested in the user experience. I personally like to reread it every year or two.\n\nNorman is aware of the durability of his work and the applicability of his principles to multiple disciplines.\n\nIf you know the basics of design better than anyone else, you can apply them flawlessly anywhere.",
                colorCode = "FF5733"
            ),
            navController = rememberNavController(),
            onSaveNote = {}
        )
    }
}
