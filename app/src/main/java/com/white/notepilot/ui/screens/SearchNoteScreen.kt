package com.white.notepilot.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.white.notepilot.R
import com.white.notepilot.data.model.Note
import com.white.notepilot.ui.components.CustomSnackbar
import com.white.notepilot.ui.components.RoundedImageCard
import com.white.notepilot.ui.components.SwipeToDeleteNoteItem
import com.white.notepilot.ui.events.NotesEvent
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.LightGray
import com.white.notepilot.viewmodel.AuthViewModel
import com.white.notepilot.viewmodel.CategoryViewModel
import com.white.notepilot.viewmodel.NotesViewModel

@Composable
fun SearchNoteScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var localSearchQuery by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    LaunchedEffect(localSearchQuery) {
//        delay(500)
        viewModel.onEvent(NotesEvent.UpdateSearchQuery(localSearchQuery))
    }

    SearchNoteScreenContent(
        searchQuery = localSearchQuery,
        notes = if (localSearchQuery.length >= 3) uiState.notes else emptyList(),
        categoryViewModel = categoryViewModel,
        showSnackbar = showSnackbar,
        snackbarMessage = snackbarMessage,
        onSearchQueryChange = { query ->
            localSearchQuery = query
        },
        onNoteClick = { note ->
            navController.navigate(Routes.NoteDetail.createRoute(note.id))
        },
        onNoteDelete = { note ->
            val userId = authViewModel.getCurrentUser()?.uid ?: ""
            viewModel.onEvent(NotesEvent.DeleteNote(note, userId))
            snackbarMessage = "Note moved to Recycle Bin"
            showSnackbar = true
        },
        onBackClick = {
            viewModel.onEvent(NotesEvent.UpdateSearchQuery(""))
            navController.popBackStack()
        },
        onDismissSnackbar = { showSnackbar = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchNoteScreenContent(
    searchQuery: String,
    notes: List<Note>,
    categoryViewModel: CategoryViewModel,
    showSnackbar: Boolean,
    snackbarMessage: String,
    onSearchQueryChange: (String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit,
    onBackClick: () -> Unit,
    onDismissSnackbar: () -> Unit
) {
    Scaffold(
        topBar = {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            com.white.notepilot.ads.BannerAdView(
                showSpacerBelow = false
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (searchQuery.length < 3 || notes.isEmpty()) {
                EmptySearchContent(searchQuery = searchQuery)
            } else {
                SearchResultsContent(
                    notes = notes,
                    categoryViewModel = categoryViewModel,
                    onNoteClick = onNoteClick,
                    onNoteDelete = onNoteDelete
                )
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

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(
                horizontal = Dimens.PaddingLarge,
                vertical = Dimens.PaddingSmall
            ),
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
                    text = stringResource(R.string.search_notes),
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
            }
        )
    }
}

@Composable
private fun EmptySearchContent(searchQuery: String) {
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

@Composable
private fun SearchResultsContent(
    notes: List<Note>,
    categoryViewModel: CategoryViewModel,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit
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
        items(
            items = notes,
            key = { note -> note.id }
        ) { note ->
            SearchNoteItemWithCategories(
                note = note,
                categoryViewModel = categoryViewModel,
                onNoteClick = { onNoteClick(note) },
                onNoteDelete = { onNoteDelete(note) }
            )
        }
    }
}

@Composable
private fun SearchNoteItemWithCategories(
    note: Note,
    categoryViewModel: CategoryViewModel,
    onNoteClick: () -> Unit,
    onNoteDelete: () -> Unit
) {
    val categories by remember(note.id) {
        categoryViewModel.getCategoriesForNote(note.id)
    }.collectAsState(initial = emptyList())
    
    SwipeToDeleteNoteItem(
        note = note,
        categories = categories,
        onNoteClick = onNoteClick,
        onNoteDelete = onNoteDelete
    )
}
//
//@Preview(showBackground = true)
//@Composable
//fun SearchNoteScreenPreview() {
//    NotesTheme {
//        SearchNoteScreenContent(
//            searchQuery = "",
//            notes = emptyList(),
//            onSearchQueryChange = {},
//            onNoteClick = {},
//            onNoteDelete = {},
//            onBackClick = {},
//            showSnackbar = true,
//            snackbarMessage = "",
//            onDismissSnackbar = {},
//            categoryViewModel = CategoryViewModel(repository = CategoryRepository(CategoryDao()))
//        )
//    }
//}
