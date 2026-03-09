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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.white.notepilot.R
import com.white.notepilot.data.model.Note
import com.white.notepilot.data.model.NoteImage
import com.white.notepilot.ui.components.CategoryChip
import com.white.notepilot.ui.components.CustomTopBar
import com.white.notepilot.ui.components.ImageAttachmentRow
import com.white.notepilot.ui.components.ImageViewerDialog
import com.white.notepilot.ui.components.ShareBottomSheet
import com.white.notepilot.ui.events.NotesEvent
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.utils.ColorUtils
import com.white.notepilot.utils.ShareHelper
import com.white.notepilot.viewmodel.AuthViewModel
import com.white.notepilot.viewmodel.CategoryViewModel
import com.white.notepilot.viewmodel.NotesViewModel

@Composable
fun NoteDetailScreen(
    navController: NavHostController,
    noteId: Int,
    viewModel: NotesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(noteId) {
        viewModel.onEvent(NotesEvent.GetNoteById(noteId))
    }

    uiState.selectedNote?.let { note ->
        NoteDetailScreenContent(
            note = note,
            noteId = noteId,
            noteImages = uiState.noteImages,
            categoryViewModel = categoryViewModel,
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
    noteId: Int,
    noteImages: List<NoteImage>,
    categoryViewModel: CategoryViewModel,
    navController: NavHostController,
    onSaveNote: (Note) -> Unit
) {
    val context = LocalContext.current
    val richTextState = rememberRichTextState()
    val codeBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val codeStrokeColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var imageToView by remember { mutableStateOf<NoteImage?>(null) }
    
    val categories by remember(noteId) {
        categoryViewModel.getCategoriesForNote(noteId)
    }.collectAsState(initial = emptyList())
    
    val backgroundColor = remember(note.colorCode) {
        Color(ColorUtils.colorCodeToInt(note.colorCode))
    }
    
    val textColor = remember(backgroundColor) {
        if (backgroundColor.luminance() > 0.5f) {
            Color.Black
        } else {
            Color.White
        }
    }
    
    LaunchedEffect(note.content) {
        richTextState.setHtml(note.content)
    }
    
    LaunchedEffect(codeBackgroundColor, codeStrokeColor) {
        richTextState.config.codeSpanColor = Color.Unspecified
        richTextState.config.codeSpanBackgroundColor = codeBackgroundColor
        richTextState.config.codeSpanStrokeColor = codeStrokeColor
    }
    
    Scaffold(
        containerColor = backgroundColor,
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
                color = textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.PaddingSmall)
            )
            
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.take(5).forEach { category ->
                        CategoryChip(
                            category = category,
                            modifier = Modifier,
                            isSelected = false,
                            showRemoveIcon = false
                        )
                    }
                    
                    if (categories.size > 5) {
                        Text(
                            text = "+${categories.size - 5}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            if (noteImages.isNotEmpty()) {
                ImageAttachmentRow(
                    images = noteImages,
                    onImageClick = { image -> imageToView = image }
                )
            }

            RichText(
                state = richTextState,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.PaddingSmall)
            )
        }
        
        imageToView?.let { image ->
            ImageViewerDialog(
                image = image,
                onDismiss = { imageToView = null }
            )
        }
        
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
