package com.white.notepilot.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.white.notepilot.R
import com.white.notepilot.data.model.Note
import com.white.notepilot.ui.components.CustomSnackbar
import com.white.notepilot.ui.components.FilterBottomSheet
import com.white.notepilot.ui.components.InfoDialog
import com.white.notepilot.ui.components.RoundedImageCard
import com.white.notepilot.ui.components.SwipeToDeleteNoteItem
import com.white.notepilot.ui.events.NotesEvent
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.state.NotesUiState
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.NotesTheme
import com.white.notepilot.viewmodel.NotesViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = hiltViewModel(),
    authViewModel: com.white.notepilot.viewmodel.AuthViewModel = hiltViewModel(),
    categoryViewModel: com.white.notepilot.viewmodel.CategoryViewModel = hiltViewModel(),
    notificationPreferences: com.white.notepilot.data.preferences.NotificationPreferences = hiltViewModel<com.white.notepilot.ui.screens.SettingsViewModel>().notificationPreferences
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val currentUser = authViewModel.getCurrentUser()
    val context = LocalContext.current
    val backgroundSyncEnabled by notificationPreferences.backgroundSyncEnabled.collectAsState(initial = true)
    
    // Monitor network connectivity and sync unsynced notes when online (if background sync is enabled)
    LaunchedEffect(currentUser?.uid, backgroundSyncEnabled) {
        currentUser?.uid?.let { userId ->
            if (backgroundSyncEnabled) {
                val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                
                val networkCallback = object : android.net.ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        viewModel.syncUnsyncedNotes(userId)
                    }
                }
                
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
                
                // Cleanup: Unregister callback when composable leaves composition
                kotlinx.coroutines.coroutineScope {
                    try {
                        kotlinx.coroutines.awaitCancellation()
                    } finally {
                        connectivityManager.unregisterNetworkCallback(networkCallback)
                    }
                }
            }
        }
    }

    HomeScreenContent(
        uiState = uiState,
        syncMessage = syncMessage,
        onEvent = viewModel::onEvent,
        onClearSyncMessage = { viewModel.clearSyncMessage() },
        authViewModel = authViewModel,
        categoryViewModel = categoryViewModel,
        navController = navController
    )
}

@Composable
private fun HomeScreenContent(
    uiState: NotesUiState,
    syncMessage: String?,
    onEvent: (NotesEvent) -> Unit,
    onClearSyncMessage: () -> Unit,
    authViewModel: com.white.notepilot.viewmodel.AuthViewModel,
    categoryViewModel: com.white.notepilot.viewmodel.CategoryViewModel,
    navController: NavHostController
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var showFilterBottomSheet by remember { mutableStateOf(false) }
    var backPressedTime by remember { mutableLongStateOf(0L) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var useStringResource by remember { mutableStateOf(true) }
    var snackbarMessageRes by remember { mutableIntStateOf(R.string.press_back_again_to_exit) }
    val context = LocalContext.current
    val activity = context as? Activity
    
    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarMessage = it
            useStringResource = false
            showSnackbar = true
            onClearSyncMessage()
        }
    }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            activity?.finish()
        } else {
            backPressedTime = currentTime
            snackbarMessageRes = R.string.press_back_again_to_exit
            useStringResource = true
            showSnackbar = true
        }
    }
    
    // Memoize callbacks to prevent NotesListContent recomposition
    val onNoteClick = remember(navController) {
        { note: Note ->
            navController.navigate(Routes.NoteDetail.createRoute(note.id))
        }
    }
    
    val onNoteDelete = remember(authViewModel, onEvent) {
        { note: Note ->
            val userId = authViewModel.getCurrentUser()?.uid ?: ""
            onEvent(NotesEvent.DeleteNote(note, userId))
            snackbarMessage = "Note moved to Recycle Bin"
            useStringResource = false
            showSnackbar = true
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Row(
                    modifier = Modifier
                        .padding(
                            horizontal = Dimens.PaddingLarge,
                            vertical = Dimens.PaddingSmall
                        )
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            Dimens.PaddingMedium
                        )
                    ) {
                        RoundedImageCard(
                            imageRes = R.drawable.search,
                            onClick = { navController.navigate(Routes.SearchNote.route) }
                        )
                        RoundedImageCard(
                            imageRes = R.drawable.filter,
                            onClick = { showFilterBottomSheet = true }
                        )
                        RoundedImageCard(
                            imageRes = R.drawable.unsynced_note,
                            onClick = { navController.navigate(Routes.UnsyncedNotes.route) }
                        )
                    }
                }

                when {
                    uiState.isLoading -> {
                        LoadingContent()
                    }

                    uiState.isEmpty -> {
                        EmptyNotesContent()
                    }

                    else -> {
                        NotesListContent(
                            notes = uiState.notes,
                            categoryViewModel = categoryViewModel,
                            onNoteClick = onNoteClick,
                            onNoteDelete = onNoteDelete
                        )
                    }
                }
            }

            if (showInfoDialog) {
                InfoDialog(onDismiss = { showInfoDialog = false })
            }

            if (showFilterBottomSheet) {
                FilterBottomSheet(
                    onDismiss = { showFilterBottomSheet = false },
                    currentSortOrder = uiState.sortOrder,
                    onSortOrderChange = { newSortOrder ->
                        onEvent(NotesEvent.UpdateSortOrder(newSortOrder))
                    },
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { date ->
                        onEvent(NotesEvent.UpdateSelectedDate(date))
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
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyNotesContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.rafiki),
            contentDescription = stringResource(R.string.empty_notes_illustration),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )

        Spacer(modifier = Modifier.height(Dimens.PaddingSmall))

        Text(
            text = stringResource(R.string.create_your_first_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun NotesListContent(
    notes: List<Note>,
    categoryViewModel: com.white.notepilot.viewmodel.CategoryViewModel,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.PaddingMedium,
            end = Dimens.PaddingMedium,
            top = Dimens.PaddingSmall,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
    ) {
        items(
            items = notes,
            key = { note -> note.id }
        ) { note ->
            val categories by categoryViewModel.getCategoriesForNote(note.id).collectAsState()
            
            SwipeToDeleteNoteItem(
                note = note,
                categories = categories,
                onNoteClick = { onNoteClick(note) },
                onNoteDelete = { onNoteDelete(note) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    NotesTheme {
        HomeScreen(navController = rememberNavController())
    }
}