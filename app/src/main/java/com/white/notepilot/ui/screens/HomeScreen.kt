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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.white.notepilot.R
import com.white.notepilot.data.model.Note
import com.white.notepilot.data.preferences.NotificationPreferences
import com.white.notepilot.ui.components.CustomSnackbar
import com.white.notepilot.ui.components.FilterBottomSheet
import com.white.notepilot.ui.components.ForceUpdateBottomSheet
import com.white.notepilot.ui.components.InfoDialog
import com.white.notepilot.ui.components.OfflineIndicator
import com.white.notepilot.ui.components.RoundedImageCard
import com.white.notepilot.ui.components.SwipeToDeleteNoteItem
import com.white.notepilot.ui.components.ads.AdPositionCalculator
import com.white.notepilot.ui.components.ads.NativeAdView
import com.white.notepilot.ui.components.skeleton.NoteListSkeleton
import com.white.notepilot.ui.events.NotesEvent
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.state.NotesUiState
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.NotesTheme
import com.white.notepilot.utils.BiometricHelper
import com.white.notepilot.utils.HapticFeedbackHelper
import com.white.notepilot.utils.NetworkUtils
import com.white.notepilot.utils.rememberHapticFeedback
import com.white.notepilot.viewmodel.AuthViewModel
import com.white.notepilot.viewmodel.CategoryViewModel
import com.white.notepilot.viewmodel.NotesViewModel
import com.white.notepilot.viewmodel.UpdateViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    notificationPreferences: NotificationPreferences = hiltViewModel<SettingsViewModel>().notificationPreferences,
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val currentUser = authViewModel.getCurrentUser()
    val context = LocalContext.current
    val backgroundSyncEnabled by notificationPreferences.backgroundSyncEnabled.collectAsState(initial = true)
    
    // Force Update System
    val showUpdateDialog by updateViewModel.showUpdateDialog.collectAsState()
    val isForceUpdate by updateViewModel.isForceUpdate.collectAsState()
    val updateInfo by updateViewModel.updateInfo.collectAsState()
    
    // Check for updates when HomeScreen loads
    LaunchedEffect(Unit) {
        updateViewModel.checkForUpdates(context)
    }
    
    // Sync notes and categories when user is logged in
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            // Initial sync from Firestore
            viewModel.syncNotesFromFirestore(userId)
            
            val firebaseRepository = com.white.notepilot.data.repository.FirebaseRepository(
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
            )
            
            // Sync local categories to Firebase
            categoryViewModel.syncCategoriesToFirebase(userId, firebaseRepository)
            
            // Fetch categories from Firebase
            categoryViewModel.fetchCategoriesFromFirebase(userId, firebaseRepository)
        }
    }
    
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
        isRefreshing = isRefreshing,
        isSyncing = isSyncing,
        onEvent = viewModel::onEvent,
        onClearSyncMessage = { viewModel.clearSyncMessage() },
        onGetUnsyncedCount = viewModel::getUnsyncedNotesCount,
        viewModel = viewModel,
        authViewModel = authViewModel,
        categoryViewModel = categoryViewModel,
        navController = navController
    )
    
    // Force Update Bottom Sheet
    if (showUpdateDialog && updateInfo != null) {
        ForceUpdateBottomSheet(
            updateInfo = updateInfo!!,
            isForceUpdate = isForceUpdate,
            onUpdateClick = {
                // User clicked update, keep dialog open until they return
            },
            onExitClick = {
                // Exit the app
                (context as? Activity)?.finishAffinity()
            },
            onDismiss = {
                updateViewModel.dismissUpdateDialog()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: NotesUiState,
    syncMessage: String?,
    isRefreshing: Boolean,
    isSyncing: Boolean,
    onEvent: (NotesEvent) -> Unit,
    onClearSyncMessage: () -> Unit,
    onGetUnsyncedCount: ((Int) -> Unit) -> Unit,
    viewModel: NotesViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel,
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
    val haptic = rememberHapticFeedback()
    val biometricHelper = remember { BiometricHelper(context) }
    
    var showUnlockDialog by remember { mutableStateOf(false) }
    var noteToUnlock by remember { mutableStateOf<Note?>(null) }
    val scope = rememberCoroutineScope()
    
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
            haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
        }
    }
    
    val onNoteClick: (Note) -> Unit = { note ->
        haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
        if (note.isLocked) {
            scope.launch {
                val lockType = viewModel.getLockType(note.id)
                if (lockType == "BIOMETRIC") {
                    biometricHelper.authenticate(
                        activity = context as FragmentActivity,
                        onSuccess = {
                            haptic(HapticFeedbackHelper.HapticType.SUCCESS)
                            navController.navigate(Routes.NoteDetail.createRoute(note.id))
                        },
                        onError = { error ->
                            snackbarMessage = error
                            useStringResource = false
                            showSnackbar = true
                            haptic(HapticFeedbackHelper.HapticType.ERROR)
                        },
                        onFailed = {
                            haptic(HapticFeedbackHelper.HapticType.ERROR)
                        }
                    )
                } else if (lockType == "PASSWORD") {
                    noteToUnlock = note
                    showUnlockDialog = true
                } else {
                    navController.navigate(Routes.NoteDetail.createRoute(note.id))
                }
            }
        } else {
            navController.navigate(Routes.NoteDetail.createRoute(note.id))
        }
    }
    
    val onNoteDelete: (Note) -> Unit = { note ->
        val userId = authViewModel.getCurrentUser()?.uid ?: ""
        onEvent(NotesEvent.DeleteNote(note, userId))
        snackbarMessage = "Note moved to Recycle Bin"
        useStringResource = false
        showSnackbar = true
        haptic(HapticFeedbackHelper.HapticType.MEDIUM_CLICK)
    }

    val onNotePin: (Note) -> Unit = { note ->
        onEvent(NotesEvent.TogglePinStatus(note))
        haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
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
                            onClick = { 
                                haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
                                navController.navigate(Routes.SearchNote.route) 
                            }
                        )
                        RoundedImageCard(
                            imageRes = R.drawable.filter,
                            onClick = { 
                                haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
                                showFilterBottomSheet = true 
                            }
                        )
                        RoundedImageCard(
                            imageRes = R.drawable.unsynced_note,
                            onClick = { 
                                haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
                                navController.navigate(Routes.UnsyncedNotes.route) 
                            }
                        )
                    }
                }
                
                if (isSyncing) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.PaddingLarge),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                val networkUtils = remember { NetworkUtils(context) }
                val isOffline = !networkUtils.isNetworkAvailable()
                var unsyncedCount by remember { mutableIntStateOf(0) }
                
                LaunchedEffect(Unit) {
                    onGetUnsyncedCount { count ->
                        unsyncedCount = count
                    }
                }
                
                OfflineIndicator(
                    isOffline = isOffline,
                    pendingActionsCount = unsyncedCount,
                    modifier = Modifier
                        .padding(horizontal = Dimens.PaddingLarge)
                        .padding(bottom = 8.dp)
                )

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        authViewModel.getCurrentUser()?.uid?.let { userId ->
                            onEvent(NotesEvent.RefreshNotes(userId))
                            haptic(HapticFeedbackHelper.HapticType.MEDIUM_CLICK)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
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
                                onNoteDelete = onNoteDelete,
                                onNotePin = onNotePin
                            )
                        }
                    }
                }
            }

            if (showInfoDialog) {
                InfoDialog(onDismiss = { showInfoDialog = false })
            }

            if (showFilterBottomSheet) {
                val categories by categoryViewModel.categories.collectAsState()
                
                FilterBottomSheet(
                    onDismiss = { 
                        haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
                        showFilterBottomSheet = false 
                    },
                    currentSortOrder = uiState.sortOrder,
                    onSortOrderChange = { newSortOrder ->
                        onEvent(NotesEvent.UpdateSortOrder(newSortOrder))
                        haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
                    },
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { date ->
                        onEvent(NotesEvent.UpdateSelectedDate(date))
                        haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
                    },
                    selectedCategoryIds = uiState.selectedCategoryIds,
                    onCategoriesSelected = { categoryIds ->
                        onEvent(NotesEvent.UpdateSelectedCategories(categoryIds))
                        haptic(HapticFeedbackHelper.HapticType.LIGHT_CLICK)
                    },
                    categories = categories
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
            
            if (showUnlockDialog && noteToUnlock != null) {
                com.white.notepilot.ui.components.lock.UnlockNoteDialog(
                    onDismiss = {
                        showUnlockDialog = false
                        noteToUnlock = null
                    },
                    onUnlock = { password ->
                        scope.launch {
                            val isCorrect = viewModel.verifyPassword(
                                noteId = noteToUnlock!!.id,
                                password = password
                            )
                            if (isCorrect) {
                                haptic(HapticFeedbackHelper.HapticType.SUCCESS)
                                showUnlockDialog = false
                                navController.navigate(Routes.NoteDetail.createRoute(noteToUnlock!!.id))
                                noteToUnlock = null
                            } else {
                                haptic(HapticFeedbackHelper.HapticType.ERROR)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    NoteListSkeleton(
        itemCount = 6,
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = Dimens.PaddingMedium,
                end = Dimens.PaddingMedium,
                top = Dimens.PaddingSmall
            )
    )
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
    categoryViewModel: CategoryViewModel,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit,
    onNotePin: (Note) -> Unit
) {
    val adPositions = remember(notes.size) {
        AdPositionCalculator.calculateAdPositions(notes.size)
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.PaddingMedium,
            end = Dimens.PaddingMedium,
            top = Dimens.PaddingSmall,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
    ) {
        itemsIndexed(
            items = notes,
            key = { _, note -> note.id }
        ) { index, note ->
            NoteItemWithCategories(
                note = note,
                categoryViewModel = categoryViewModel,
                onNoteClick = { onNoteClick(note) },
                onNoteDelete = { onNoteDelete(note) },
                onNotePin = { onNotePin(note) }
            )
            
            if (adPositions.contains(index + 1)) {
                NativeAdView()
            }
        }
    }
}

@Composable
private fun NoteItemWithCategories(
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    NotesTheme {
        HomeScreen(navController = rememberNavController())
    }
}