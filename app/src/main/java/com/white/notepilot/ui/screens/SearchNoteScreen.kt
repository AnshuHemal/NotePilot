package com.white.notepilot.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.white.notepilot.R
import com.white.notepilot.data.model.Note
import com.white.notepilot.ui.components.AiSearchModeToggle
import com.white.notepilot.ui.components.AiSearchPanel
import com.white.notepilot.ui.components.CustomSnackbar
import com.white.notepilot.ui.components.RoundedImageCard
import com.white.notepilot.ui.components.SwipeToDeleteNoteItem
import com.white.notepilot.ui.components.ads.BannerAdView
import com.white.notepilot.ui.components.skeleton.NoteListSkeleton
import com.white.notepilot.ui.events.NotesEvent
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.state.AiSearchState
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.LightGray
import com.white.notepilot.viewmodel.AiSearchViewModel
import com.white.notepilot.viewmodel.AuthViewModel
import com.white.notepilot.viewmodel.CategoryViewModel
import com.white.notepilot.viewmodel.NotesViewModel

@Composable
fun SearchNoteScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    aiSearchViewModel: AiSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val aiSearchState by aiSearchViewModel.state.collectAsState()

    var localSearchQuery by remember { mutableStateOf("") }
    var isAiMode by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var isKeywordSearching by remember { mutableStateOf(false) }

    // Keyword search debounce
    LaunchedEffect(localSearchQuery, isAiMode) {
        if (!isAiMode) {
            if (localSearchQuery.length >= 3) {
                isKeywordSearching = true
                kotlinx.coroutines.delay(500)
                viewModel.onEvent(NotesEvent.UpdateSearchQuery(localSearchQuery))
                isKeywordSearching = false
            } else {
                viewModel.onEvent(NotesEvent.UpdateSearchQuery(""))
            }
        }
    }

    // Reset AI state when switching modes or clearing query
    LaunchedEffect(isAiMode) {
        if (!isAiMode) aiSearchViewModel.reset()
        localSearchQuery = ""
        viewModel.onEvent(NotesEvent.UpdateSearchQuery(""))
    }

    SearchNoteScreenContent(
        searchQuery = localSearchQuery,
        isAiMode = isAiMode,
        aiSearchState = aiSearchState,
        keywordNotes = if (!isAiMode && localSearchQuery.length >= 3) uiState.notes else emptyList(),
        allNotes = uiState.notes,
        isKeywordSearching = isKeywordSearching,
        categoryViewModel = categoryViewModel,
        showSnackbar = showSnackbar,
        snackbarMessage = snackbarMessage,
        onSearchQueryChange = { query ->
            localSearchQuery = query
            if (isAiMode) aiSearchViewModel.reset()
        },
        onAiSearch = { query ->
            aiSearchViewModel.search(query, uiState.notes)
        },
        onToggleMode = { isAiMode = !isAiMode },
        onNoteClick = { note ->
            navController.navigate(Routes.NoteDetail.createRoute(note.id))
        },
        onNoteDelete = { note ->
            val userId = authViewModel.getCurrentUser()?.uid ?: ""
            viewModel.onEvent(NotesEvent.DeleteNote(note, userId))
            snackbarMessage = "Note moved to Recycle Bin"
            showSnackbar = true
        },
        onNotePin = { note ->
            viewModel.onEvent(NotesEvent.TogglePinStatus(note))
        },
        onBackClick = {
            viewModel.onEvent(NotesEvent.UpdateSearchQuery(""))
            aiSearchViewModel.reset()
            navController.popBackStack()
        },
        onDismissSnackbar = { showSnackbar = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchNoteScreenContent(
    searchQuery: String,
    isAiMode: Boolean,
    aiSearchState: AiSearchState,
    keywordNotes: List<Note>,
    allNotes: List<Note>,
    isKeywordSearching: Boolean,
    categoryViewModel: CategoryViewModel,
    showSnackbar: Boolean,
    snackbarMessage: String,
    onSearchQueryChange: (String) -> Unit,
    onAiSearch: (String) -> Unit,
    onToggleMode: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit,
    onNotePin: (Note) -> Unit,
    onBackClick: () -> Unit,
    onDismissSnackbar: () -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                SearchBar(
                    searchQuery = searchQuery,
                    isAiMode = isAiMode,
                    onSearchQueryChange = onSearchQueryChange,
                    onAiSearch = onAiSearch,
                    onBackClick = onBackClick
                )
                AiSearchModeToggle(
                    isAiMode = isAiMode,
                    onToggle = onToggleMode
                )
            }
        },
        bottomBar = {
            BannerAdView(showSpacerBelow = false)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // ── AI mode ───────────────────────────────────────────────────
                isAiMode -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (aiSearchState is AiSearchState.Idle && searchQuery.isBlank()) {
                            AiSearchHint()
                        } else {
                            AiSearchPanel(
                                state = aiSearchState,
                                onNoteClick = onNoteClick,
                                onNoteDelete = onNoteDelete,
                                onNotePin = onNotePin,
                                categoryViewModel = categoryViewModel
                            )
                        }
                    }
                }

                // ── Keyword mode: loading ─────────────────────────────────────
                isKeywordSearching && searchQuery.length >= 3 -> {
                    NoteListSkeleton(
                        itemCount = 5,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = Dimens.PaddingMedium,
                                end = Dimens.PaddingMedium,
                                top = Dimens.PaddingMedium
                            )
                    )
                }

                // ── Keyword mode: empty / short query ─────────────────────────
                searchQuery.length < 3 || keywordNotes.isEmpty() -> {
                    EmptySearchContent(searchQuery = searchQuery, isAiMode = false)
                }

                // ── Keyword mode: results ─────────────────────────────────────
                else -> {
                    KeywordResultsContent(
                        notes = keywordNotes,
                        categoryViewModel = categoryViewModel,
                        onNoteClick = onNoteClick,
                        onNoteDelete = onNoteDelete,
                        onNotePin = onNotePin
                    )
                }
            }

            if (showSnackbar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    CustomSnackbar(
                        message = snackbarMessage,
                        isVisible = true,
                        onDismiss = onDismissSnackbar
                    )
                }
            }
        }
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    searchQuery: String,
    isAiMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onAiSearch: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
    ) {
        RoundedImageCard(
            imageRes = R.drawable.back_arrow,
            onClick = onBackClick
        )

        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = if (isAiMode)
                        "Ask anything, e.g. \"meeting with client\""
                    else
                        stringResource(R.string.search_notes),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedPlaceholderColor = LightGray,
                focusedPlaceholderColor = LightGray,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(Dimens.PaddingMedium),
            modifier = Modifier.weight(1f),
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = if (isAiMode) ImeAction.Search else ImeAction.Default
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (isAiMode && searchQuery.isNotBlank()) {
                        onAiSearch(searchQuery)
                    }
                }
            )
        )

        // AI search trigger button (only in AI mode)
        AnimatedVisibility(
            visible = isAiMode && searchQuery.isNotBlank(),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {
            RoundedImageCard(
                imageRes = R.drawable.ic_ai_sparkle,
                onClick = { onAiSearch(searchQuery) }
            )
        }
    }
}

// ── AI search hint ────────────────────────────────────────────────────────────

@Composable
private fun AiSearchHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE))
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Try asking:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF7C3AED)
                )
                listOf(
                    "\"Meeting with the client last month\"",
                    "\"My grocery shopping list\"",
                    "\"Ideas about the new product feature\"",
                    "\"Notes about my health appointments\""
                ).forEach { example ->
                    Text(
                        text = example,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4C1D95)
                    )
                }
            }
        }

        Text(
            text = "Type your query above and tap the search button or press Enter",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ── Keyword results ───────────────────────────────────────────────────────────

@Composable
private fun KeywordResultsContent(
    notes: List<Note>,
    categoryViewModel: CategoryViewModel,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit,
    onNotePin: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.PaddingMedium,
            top = Dimens.PaddingMedium,
            end = Dimens.PaddingMedium,
            bottom = Dimens.PaddingMedium
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
    ) {
        items(items = notes, key = { it.id }) { note ->
            KeywordNoteItem(
                note = note,
                categoryViewModel = categoryViewModel,
                onNoteClick = { onNoteClick(note) },
                onNoteDelete = { onNoteDelete(note) },
                onNotePin = { onNotePin(note) }
            )
        }
    }
}

@Composable
private fun KeywordNoteItem(
    note: Note,
    categoryViewModel: CategoryViewModel,
    onNoteClick: () -> Unit,
    onNoteDelete: () -> Unit,
    onNotePin: () -> Unit
) {
    val categories by remember(note.id) {
        categoryViewModel.getCategoriesForNote(note.id)
    }.collectAsState(initial = emptyList())

    SwipeToDeleteNoteItem(
        note = note,
        categories = categories,
        onNoteClick = onNoteClick,
        onNoteDelete = onNoteDelete,
        onNotePin = onNotePin
    )
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptySearchContent(searchQuery: String, isAiMode: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (searchQuery.length >= 3) {
            Image(
                painter = painterResource(id = R.drawable.cuate),
                contentDescription = stringResource(R.string.file_not_found_try_searching_again),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Text(
                text = stringResource(R.string.file_not_found_try_searching_again),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
