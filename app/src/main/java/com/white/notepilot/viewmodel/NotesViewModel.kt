package com.white.notepilot.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.data.model.Note
import com.white.notepilot.data.model.NoteImage
import com.white.notepilot.data.repository.ImageRepository
import com.white.notepilot.data.repository.NoteRepository
import com.white.notepilot.enums.SortOrder
import com.white.notepilot.ui.events.NotesEvent
import com.white.notepilot.ui.state.NotesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val categoryRepository: com.white.notepilot.data.repository.CategoryRepository,
    val imageRepository: ImageRepository,
    private val passwordRepository: com.white.notepilot.data.repository.NotePasswordRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.DESCENDING)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedDate = MutableStateFlow<Long?>(null)
    private val _selectedCategoryIds = MutableStateFlow<List<Int>>(emptyList())
    private val _selectedNote = MutableStateFlow<Note?>(null)
    private val _noteImages = MutableStateFlow<List<NoteImage>>(emptyList())
    private val _isSyncing = MutableStateFlow(false)
    private val _syncError = MutableStateFlow<String?>(null)
    private val _syncMessage = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val notesFlow = repository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val processedNotes = combine(
        notesFlow,
        _sortOrder,
        _searchQuery,
        _selectedDate,
        _selectedCategoryIds
    ) { notesList, order, query, selectedDate, selectedCategoryIds ->
        val filtered = if (query.isBlank()) {
            notesList
        } else {
            notesList.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true)
            }
        }
        
        val dateFiltered = if (selectedDate != null) {
            filtered.filter { note ->
                isSameDay(note.timestamp, selectedDate)
            }
        } else {
            filtered
        }
        
        val categoryFiltered = if (selectedCategoryIds.isNotEmpty()) {
            dateFiltered.filter { note ->
                val noteCategories = categoryRepository.getCategoriesForNoteSync(note.id)
                val noteCategoryIds = noteCategories.map { it.id }
                selectedCategoryIds.any { it in noteCategoryIds }
            }
        } else {
            dateFiltered
        }

        when (order) {
            SortOrder.ASCENDING -> categoryFiltered.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenBy { it.timestamp }
            )
            SortOrder.DESCENDING -> categoryFiltered.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenByDescending { it.timestamp }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<NotesUiState> = combine(
        processedNotes,
        _sortOrder,
        _searchQuery,
        _selectedNote
    ) { notes, sortOrder, searchQuery, selectedNote ->
        Triple(notes, sortOrder, searchQuery) to selectedNote
    }.combine(_selectedDate) { (triple, selectedNote), selectedDate ->
        Pair(triple, selectedNote) to selectedDate
    }.combine(_selectedCategoryIds) { (pair, selectedDate), selectedCategoryIds ->
        val (triple, selectedNote) = pair
        val (notes, sortOrder, searchQuery) = triple
        NotesUiState(
            notes = notes,
            sortOrder = sortOrder,
            searchQuery = searchQuery,
            isLoading = false,
            isEmpty = notes.isEmpty(),
            selectedNote = selectedNote,
            selectedDate = selectedDate,
            selectedCategoryIds = selectedCategoryIds
        )
    }.combine(_noteImages) { state, images ->
        state.copy(noteImages = images)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState(isLoading = true)
    )

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.UpdateSortOrder -> {
                _sortOrder.value = event.sortOrder
            }

            is NotesEvent.UpdateSearchQuery -> {
                _searchQuery.value = event.query
            }
            
            is NotesEvent.UpdateSelectedDate -> {
                _selectedDate.value = event.date
            }
            
            is NotesEvent.UpdateSelectedCategories -> {
                _selectedCategoryIds.value = event.categoryIds
            }

            is NotesEvent.UpsertNote -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val result = repository.upsertNote(event.note, event.userId)
                    if (result.isFailure) {
                        _syncError.value = "Note saved locally. Will sync when online."
                    } else {
                        val syncedNote = result.getOrNull()
                        if (syncedNote?.isSynced == false) {
                            _syncMessage.value = "Note saved locally. Will sync when online."
                        }
                    }
                }
            }

            is NotesEvent.DeleteNote -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val result = repository.deleteNote(event.note)
                    if (result.isFailure) {
                        _syncError.value = result.exceptionOrNull()?.message
                    }
                }
            }

            is NotesEvent.DeleteAllNotes -> {
                viewModelScope.launch(Dispatchers.IO) {
                    if (event.userId.isNotEmpty()) {
                        val result = repository.deleteAllNotes(event.userId)
                        if (result.isFailure) {
                            _syncError.value = result.exceptionOrNull()?.message
                        }
                    }
                }
            }

            is NotesEvent.GetNoteById -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val note = repository.getNoteById(event.noteId)
                    _selectedNote.value = note
                    if (note != null) {
                        val images = imageRepository.getImagesForNoteSync(note.id)
                        _noteImages.value = images
                    } else {
                        _noteImages.value = emptyList()
                    }
                }
            }

            is NotesEvent.TogglePinStatus -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.toggleNotePinStatus(event.note.id, !event.note.isPinned)
                }
            }

            is NotesEvent.RefreshNotes -> {
                viewModelScope.launch(Dispatchers.IO) {
                    _isRefreshing.value = true
                    repository.fetchAndSyncNotesFromFirestore(event.userId)
                    repository.syncUnsyncedNotesToFirestore(event.userId)
                    delay(1000) // Visual feedback
                    _isRefreshing.value = false
                }
            }
        }
    }

    suspend fun saveNoteAndWait(note: Note, userId: String): Triple<Boolean, Int, String?> {
        return try {
            val result = repository.upsertNote(note, userId)
            if (result.isFailure) {
                _syncError.value = "Note saved locally. Will sync when online."
                Triple(true, note.id, note.noteId)
            } else {
                val syncedNote = result.getOrNull()
                if (syncedNote?.isSynced == false) {
                    _syncMessage.value = "Note saved locally. Will sync when online."
                }
                Triple(true, syncedNote?.id ?: note.id, syncedNote?.noteId)
            }
        } catch (e: Exception) {
            _syncError.value = e.message
            Triple(false, 0, null)
        }
    }
    
    suspend fun saveImagesForNote(noteId: Int, noteFirebaseId: String?, imageUris: List<Uri>): Int {
        return withContext(Dispatchers.IO) {
            var syncedCount = 0
            imageUris.forEach { uri ->
                val result = imageRepository.saveImageForNote(noteId, noteFirebaseId, uri)
                if (result.isSuccess) {
                    val image = result.getOrNull()
                    if (image?.isSynced == true) {
                        syncedCount++
                    }
                }
            }
            val images = imageRepository.getImagesForNoteSync(noteId)
            _noteImages.value = images
            
            if (syncedCount < imageUris.size) {
                _syncMessage.value = "Images saved. ${imageUris.size - syncedCount} pending sync."
            } else {
                _syncMessage.value = "All images synced successfully!"
            }
            
            syncedCount
        }
    }
    
    fun deleteImage(image: NoteImage) {
        viewModelScope.launch(Dispatchers.IO) {
            imageRepository.deleteImage(image)
            val images = imageRepository.getImagesForNoteSync(image.noteId)
            _noteImages.value = images
        }
    }
    
    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    fun syncNotesFromFirestore(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            _syncError.value = null
            val result = repository.fetchAndSyncNotesFromFirestore(userId)
            repository.cleanupDuplicates()
            _isSyncing.value = false
            if (result.isFailure) {
                _syncError.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun syncUnsyncedNotes(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            _syncError.value = null
            val result = repository.syncUnsyncedNotesToFirestore(userId)
            _isSyncing.value = false
            if (result.isSuccess) {
                val syncedCount = result.getOrNull() ?: 0
                if (syncedCount > 0) {
                    _syncMessage.value = "$syncedCount note(s) synced successfully"
                }
            } else {
                _syncError.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun getUnsyncedNotesCount(callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val unsyncedNotes = repository.getUnsyncedNotes()
            callback(unsyncedNotes.size)
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }
    
    fun getUnsyncedNotesList(callback: (List<Note>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val unsyncedNotes = repository.getUnsyncedNotes()
            callback(unsyncedNotes)
        }
    }

    fun forceSyncNote(note: Note, userId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.forceSyncNote(note, userId)
            callback(result.isSuccess && result.getOrNull()?.isSynced == true)
        }
    }
    
    fun getUnsyncedDeletedNotesList(callback: (List<Note>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val deletedNotes = repository.getUnsyncedDeletedNotes()
            callback(deletedNotes)
        }
    }
    
    fun forceSyncDeletedNote(note: Note, userId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.permanentlyDeleteNote(note, userId)
            callback(result.isSuccess)
        }
    }
    
    fun restoreDeletedNote(note: Note, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.restoreDeletedNote(note)
            callback(result.isSuccess)
        }
    }

    suspend fun hasPassword(noteId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            passwordRepository.hasPassword(noteId)
        }
    }

    suspend fun verifyPassword(noteId: Int, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            passwordRepository.verifyPasswordForNote(noteId, password)
        }
    }

    fun setPassword(noteId: Int, noteFirebaseId: String?, password: String, userId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = passwordRepository.setPasswordForNote(noteId, noteFirebaseId, password, userId, "PASSWORD")
            callback(result.isSuccess)
        }
    }
    
    fun setBiometric(noteId: Int, noteFirebaseId: String?, userId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = passwordRepository.setPasswordForNote(noteId, noteFirebaseId, "", userId, "BIOMETRIC")
            callback(result.isSuccess)
        }
    }
    
    suspend fun getLockType(noteId: Int): String? {
        return withContext(Dispatchers.IO) {
            passwordRepository.getLockType(noteId)
        }
    }

    fun removePassword(noteId: Int, noteFirebaseId: String?, userId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = passwordRepository.removePasswordForNote(noteId, noteFirebaseId, userId)
            callback(result.isSuccess)
        }
    }

    suspend fun getPasswordHash(noteId: Int): String? {
        return withContext(Dispatchers.IO) {
            passwordRepository.getPasswordHash(noteId)
        }
    }

    suspend fun addScannedNote(
        title: String,
        htmlContent: String,
        categories: String,
        passwordHash: String,
        isLocked: Boolean
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Create the note - always create a new note, don't check for duplicates
                val note = Note(
                    id = 0,
                    noteId = null,
                    title = title,
                    content = htmlContent,
                    colorCode = generateRandomColorCode(),
                    timestamp = System.currentTimeMillis(),
                    isPinned = false,
                    isLocked = isLocked,
                    isSynced = false,
                    isDeleted = false
                )
                
                // Insert the note directly to avoid duplicate checking
                val insertedId = repository.insertNoteDirectly(note)
                val savedNote = note.copy(id = insertedId.toInt())
                
                if (insertedId > 0) {
                    // Handle categories
                    if (categories.isNotBlank()) {
                        try {
                            val categoryList = categories.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            
                            categoryList.forEach { categoryName ->
                                try {
                                    // Check if category exists, if not create it
                                    val existingCategory = categoryRepository.getCategoryByNameSync(categoryName)
                                    val categoryId = if (existingCategory != null) {
                                        existingCategory.id
                                    } else {
                                        val newCategory = com.white.notepilot.data.model.Category(
                                            name = categoryName,
                                            color = generateRandomColorHex()
                                        )
                                        categoryRepository.insertCategorySync(newCategory).toInt()
                                    }
                                    
                                    // Link category to note
                                    categoryRepository.addCategoryToNoteSync(savedNote.id, categoryId)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    // Continue with other categories even if one fails
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Continue even if category processing fails
                        }
                    }
                    
                    // Handle password if locked
                    if (isLocked && passwordHash.isNotBlank()) {
                        try {
                            passwordRepository.setPasswordHashForNoteAsync(
                                savedNote.id,
                                savedNote.noteId,
                                passwordHash,
                                ""
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Continue even if password setting fails
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        _syncMessage.value = "Note added to workspace successfully!"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _syncError.value = "Failed to save note"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _syncError.value = "Failed to add note: ${e.message}"
                }
            }
        }
    }
    
    private fun generateRandomColorHex(): String {
        val colors = listOf(
            "#EF5350", "#EC407A", "#AB47BC", "#7E57C2",
            "#5C6BC0", "#42A5F5", "#29B6F6", "#26C6DA",
            "#26A69A", "#66BB6A", "#9CCC65", "#D4E157",
            "#FFEE58", "#FFCA28", "#FF7043", "#8D6E63"
        )
        return colors.random()
    }
    
    private fun generateRandomColorCode(): String {
        val random = java.util.Random()
        return String.format(
            "%02X%02X%02X",
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
    }
}
