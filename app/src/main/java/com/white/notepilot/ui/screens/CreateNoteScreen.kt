package com.white.notepilot.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import com.white.notepilot.R
import com.white.notepilot.data.model.Category
import com.white.notepilot.data.model.Note
import com.white.notepilot.data.model.NoteImage
import com.white.notepilot.ui.components.CategoryChip
import com.white.notepilot.ui.components.CategorySelectionBottomSheet
import com.white.notepilot.ui.components.CustomPopupDialog
import com.white.notepilot.ui.components.CustomSnackbar
import com.white.notepilot.ui.components.CustomTopBar
import com.white.notepilot.ui.components.ImageAttachmentRow
import com.white.notepilot.ui.components.ImageViewerDialog
import com.white.notepilot.ui.events.NotesEvent
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.LightGray
import com.white.notepilot.utils.ColorUtils
import com.white.notepilot.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@Composable
fun CreateNoteScreen(
    navController: NavHostController,
    noteId: Int? = null,
    viewModel: NotesViewModel = hiltViewModel(),
    authViewModel: com.white.notepilot.viewmodel.AuthViewModel = hiltViewModel(),
    categoryViewModel: com.white.notepilot.viewmodel.CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    val scope = rememberCoroutineScope()
    var existingCategoryIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    
    LaunchedEffect(noteId) {
        noteId?.let {
            viewModel.onEvent(NotesEvent.GetNoteById(it))
            // Fetch existing categories for this note
            scope.launch {
                val noteCategories = categoryViewModel.getCategoriesForNoteSync(it)
                existingCategoryIds = noteCategories.map { cat -> cat.id }
            }
        }
    }

    val existingNote = if (noteId != null) uiState.selectedNote else null
    
    CreateNoteScreenContent(
        navController = navController,
        existingNote = existingNote,
        existingCategoryIds = existingCategoryIds,
        existingImages = uiState.noteImages,
        syncMessage = syncMessage,
        categories = categories,
        onSaveNote = { note, selectedCategoryIds, selectedImageUris, callback ->
            scope.launch {
                val userId = authViewModel.getCurrentUser()?.uid
                val (success, savedNoteId, noteFirebaseId) = if (!userId.isNullOrBlank()) {
                    viewModel.saveNoteAndWait(note, userId)
                } else {
                    viewModel.saveNoteAndWait(note, "")
                }
                
                if (success && savedNoteId > 0) {
                    categoryViewModel.updateNoteCategories(savedNoteId, selectedCategoryIds, noteFirebaseId)
                    
                    if (selectedImageUris.isNotEmpty()) {
                        android.util.Log.d("CreateNoteScreen", "Saving ${selectedImageUris.size} images...")
                        val syncedCount = viewModel.saveImagesForNote(savedNoteId, noteFirebaseId, selectedImageUris)
                        android.util.Log.d("CreateNoteScreen", "Images saved: $syncedCount/${selectedImageUris.size} synced")
                    }
                }
                
                callback(success)
            }
        },
        onDeleteImage = { image ->
            viewModel.deleteImage(image)
        },
        onClearSyncMessage = {
            viewModel.clearSyncMessage()
        },
        onManageCategories = {
            navController.navigate(com.white.notepilot.ui.navigation.Routes.CategoryManagement.route)
        }
    )
}

@Composable
private fun CreateNoteScreenContent(
    navController: NavHostController,
    existingNote: Note?,
    existingCategoryIds: List<Int>,
    existingImages: List<NoteImage>,
    syncMessage: String?,
    categories: List<Category>,
    onSaveNote: (Note, List<Int>, List<Uri>, (Boolean) -> Unit) -> Unit,
    onDeleteImage: (NoteImage) -> Unit,
    onClearSyncMessage: () -> Unit,
    onManageCategories: () -> Unit
) {
    var title by remember(existingNote) { mutableStateOf(existingNote?.title ?: "") }
    val richTextState = rememberRichTextState()
    var selectedCategoryIds by remember(existingCategoryIds) { mutableStateOf(existingCategoryIds) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var imageToView by remember { mutableStateOf<NoteImage?>(null) }
    val scope = rememberCoroutineScope()

    var showSaveDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarMessageRes by remember { mutableIntStateOf(R.string.please_fill_in_both_title_and_content) }
    var useStringResource by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris = selectedImageUris + uris
        }
    }
    
    LaunchedEffect(existingNote) {
        existingNote?.content?.let { content ->
            richTextState.setHtml(content)
        }
    }
    
    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarMessage = it
            useStringResource = false
            showSnackbar = true
            onClearSyncMessage()
        }
    }

    val isEditMode = existingNote != null
    val contentText = richTextState.annotatedString.text
    val hasChanges = if (isEditMode) {
        title != existingNote.title || contentText != existingNote.content || selectedImageUris.isNotEmpty()
    } else {
        title.isNotBlank() || contentText.isNotBlank() || selectedImageUris.isNotEmpty()
    }

    BackHandler {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                leftIconRes = R.drawable.back_arrow,
                onLeftIconClick = {
                    if (hasChanges) {
                        showDiscardDialog = true
                    } else {
                        navController.popBackStack()
                    }
                },
                customContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add),
                                contentDescription = "Add Images",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = { showCategorySheet = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.label),
                                contentDescription = "Categories",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                if (isSaving) return@IconButton
                                
                                val content = richTextState.toHtml()
                                if (title.isBlank() && contentText.isBlank()) {
                                    snackbarMessageRes = R.string.please_fill_in_both_title_and_content
                                    useStringResource = true
                                    showSnackbar = true
                                } else if (title.isBlank()) {
                                    snackbarMessageRes = R.string.please_enter_a_title
                                    useStringResource = true
                                    showSnackbar = true
                                } else if (contentText.isBlank()) {
                                    snackbarMessageRes = R.string.please_enter_content
                                    useStringResource = true
                                    showSnackbar = true
                                } else {
                                    if (isEditMode) {
                                        if (hasChanges) {
                                            showSaveDialog = true
                                        } else {
                                            navController.popBackStack()
                                        }
                                    } else {
                                        isSaving = true
                                        val note = Note(
                                            title = title,
                                            content = content,
                                            colorCode = ColorUtils.generateRandomColorCode()
                                        )
                                        onSaveNote(note, selectedCategoryIds, selectedImageUris) { success ->
                                            isSaving = false
                                            if (success) {
                                                navController.popBackStack()
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.save),
                                    contentDescription = "Save",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
                    )
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.PaddingLarge)
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.title),
                            style = MaterialTheme.typography.headlineLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        unfocusedPlaceholderColor = LightGray,
                        focusedPlaceholderColor = LightGray,
                        cursorColor = MaterialTheme.colorScheme.onBackground,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                
                // Category chips
                if (selectedCategoryIds.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.filter { it.id in selectedCategoryIds }.chunked(3).forEach { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCategories.forEach { category ->
                                    CategoryChip(
                                        category = category,
                                        showRemoveIcon = true,
                                        onRemove = {
                                            selectedCategoryIds = selectedCategoryIds - category.id
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Image attachments - show existing images
                if (existingImages.isNotEmpty()) {
                    ImageAttachmentRow(
                        images = existingImages,
                        onImageClick = { image -> imageToView = image },
                        onRemoveImage = { image ->
                            onDeleteImage(image)
                        }
                    )
                }
                
                // Image attachments - show newly selected images (preview)
                if (selectedImageUris.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.PaddingLarge, vertical = 8.dp)
                    ) {
                        Text(
                            text = "New Images (${selectedImageUris.size})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            selectedImageUris.forEach { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    coil.compose.AsyncImage(
                                        model = uri,
                                        contentDescription = "Selected image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    
                                    IconButton(
                                        onClick = {
                                            selectedImageUris = selectedImageUris - uri
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                BasicRichTextEditor(
                    state = richTextState,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (richTextState.annotatedString.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.type_something),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = LightGray
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            
            // Rich Text Toolbar overlay at bottom
            RichTextStyleRow(
                state = richTextState,
                onLinkClick = { showLinkDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
            )

            if (showSaveDialog) {
                CustomPopupDialog(
                    message = stringResource(R.string.save_changes),
                    negativeButtonText = stringResource(R.string.discard),
                    positiveButtonText = stringResource(R.string.save),
                    onNegativeClick = {
                        showSaveDialog = false
                    },
                    onPositiveClick = {
                        showSaveDialog = false
                        isSaving = true
                        val updatedNote = existingNote!!.copy(
                            title = title,
                            content = richTextState.toHtml(),
                            timestamp = System.currentTimeMillis()
                        )
                        onSaveNote(updatedNote, selectedCategoryIds, selectedImageUris) { success ->
                            isSaving = false
                            if (success) {
                                navController.popBackStack()
                            }
                        }
                    }
                )
            }
            
            // Category Selection Bottom Sheet
            if (showCategorySheet) {
                CategorySelectionBottomSheet(
                    categories = categories,
                    selectedCategoryIds = selectedCategoryIds,
                    onDismiss = { showCategorySheet = false },
                    onSave = { newSelectedIds ->
                        selectedCategoryIds = newSelectedIds
                    },
                    onManageCategories = {
                        showCategorySheet = false
                        onManageCategories()
                    }
                )
            }

            if (showDiscardDialog) {
                CustomPopupDialog(
                    message = stringResource(R.string.are_your_sure_you_want_discard_your_changes),
                    negativeButtonText = stringResource(R.string.discard),
                    positiveButtonText = stringResource(R.string.keep),
                    onNegativeClick = {
                        showDiscardDialog = false
                        navController.popBackStack()
                    },
                    onPositiveClick = {
                        showDiscardDialog = false
                    }
                )
            }

            if (showLinkDialog) {
                val selectedText = if (richTextState.selection.collapsed) {
                    ""
                } else {
                    richTextState.annotatedString.text.substring(
                        richTextState.selection.min,
                        richTextState.selection.max
                    )
                }
                
                LinkDialog(
                    selectedText = selectedText,
                    onDismiss = { showLinkDialog = false },
                    onConfirm = { text, url ->
                        showLinkDialog = false
                        if (selectedText.isNotEmpty()) {
                            // If text was selected, apply link to the selection
                            richTextState.addLinkToSelection(url = url)
                        } else {
                            // If no text was selected, insert new link
                            richTextState.addLink(text = text, url = url)
                        }
                    }
                )
            }

            if (showSnackbar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    if (useStringResource) {
                        CustomSnackbar(
                            message = snackbarMessageRes,
                            isVisible = true,
                            onDismiss = { showSnackbar = false }
                        )
                    } else {
                        CustomSnackbar(
                            message = snackbarMessage,
                            isVisible = true,
                            onDismiss = { showSnackbar = false }
                        )
                    }
                }
            }
            
            // Image Viewer Dialog
            imageToView?.let { image ->
                ImageViewerDialog(
                    image = image,
                    onDismiss = { imageToView = null }
                )
            }
        }
    }
}

@Composable
private fun RichTextStyleRow(
    state: com.mohamedrejeb.richeditor.model.RichTextState,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bold
        RichTextStyleButton(
            icon = R.drawable.bold,
            contentDescription = "Bold",
            isActive = state.currentSpanStyle.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold,
            onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) }
        )
        
        // Italic
        RichTextStyleButton(
            icon = R.drawable.italic,
            contentDescription = "Italic",
            isActive = state.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic,
            onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) }
        )
        
        // Underline
        RichTextStyleButton(
            icon = R.drawable.underline,
            contentDescription = "Underline",
            isActive = state.currentSpanStyle.textDecoration?.contains(androidx.compose.ui.text.style.TextDecoration.Underline) == true,
            onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) }
        )
        
        // Strikethrough
        RichTextStyleButton(
            icon = R.drawable.strike_through,
            contentDescription = "Strikethrough",
            isActive = state.currentSpanStyle.textDecoration?.contains(androidx.compose.ui.text.style.TextDecoration.LineThrough) == true,
            onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)) }
        )
        
        // Title/Heading
        RichTextStyleButton(
            icon = R.drawable.title,
            contentDescription = "Heading",
            isActive = false,
            onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontSize = 24.dp.value.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) }
        )
        
        // Link
        RichTextStyleButton(
            icon = R.drawable.link,
            contentDescription = "Link",
            isActive = false,
            onClick = onLinkClick
        )
        
        // Bullet List
        RichTextStyleButton(
            icon = R.drawable.format_list_bulleted,
            contentDescription = "Bullet List",
            isActive = state.isUnorderedList,
            onClick = { state.toggleUnorderedList() }
        )
        
        // Numbered List
        RichTextStyleButton(
            icon = R.drawable.format_list_numbered,
            contentDescription = "Numbered List",
            isActive = state.isOrderedList,
            onClick = { state.toggleOrderedList() }
        )
        
        // Code
        RichTextStyleButton(
            icon = R.drawable.code,
            contentDescription = "Code",
            isActive = state.isCodeSpan,
            onClick = { state.toggleCodeSpan() }
        )
    }
}

@Composable
private fun RichTextStyleButton(
    icon: Int,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
            contentColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LinkDialog(
    selectedText: String,
    onDismiss: () -> Unit,
    onConfirm: (text: String, url: String) -> Unit
) {
    val hasSelectedText = selectedText.isNotBlank()
    var linkText by remember { mutableStateOf(selectedText) }
    var linkUrl by remember { mutableStateOf("https://") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(Dimens.PaddingLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(Dimens.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Link",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                
                if (hasSelectedText) {
                    // Show selected text as read-only
                    TextField(
                        value = linkText,
                        onValueChange = { },
                        label = { Text("Link text") },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        singleLine = true
                    )
                } else {
                    // Allow user to enter link text
                    TextField(
                        value = linkText,
                        onValueChange = { linkText = it },
                        placeholder = { Text("Link text") },
                        label = { Text("Link text") },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
                
                TextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    placeholder = { Text("URL") },
                    label = { Text("URL") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(Dimens.PaddingSmall)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Dimens.PaddingSmall)
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (linkText.isNotBlank() && linkUrl.isNotBlank()) {
                                onConfirm(linkText, linkUrl)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(Dimens.PaddingSmall),
                        enabled = linkText.isNotBlank() && linkUrl.isNotBlank()
                    ) {
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = Dimens.PaddingSmall)
                        )
                    }
                }
            }
        }
    }
}
