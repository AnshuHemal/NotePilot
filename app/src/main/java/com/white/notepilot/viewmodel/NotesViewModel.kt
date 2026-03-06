package com.white.notepilot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.data.model.Note
import com.white.notepilot.data.repository.NoteRepository
import com.white.notepilot.enums.SortOrder
import com.white.notepilot.ui.events.NotesEvent
import com.white.notepilot.ui.state.NotesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.DESCENDING)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedDate = MutableStateFlow<Long?>(null)
    private val _selectedNote = MutableStateFlow<Note?>(null)
    private val _isSyncing = MutableStateFlow(false)
    private val _syncError = MutableStateFlow<String?>(null)
    private val _syncMessage = MutableStateFlow<String?>(null)
    
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
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
        _selectedDate
    ) { notesList, order, query, selectedDate ->
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

        when (order) {
            SortOrder.ASCENDING -> dateFiltered.sortedBy { it.timestamp }
            SortOrder.DESCENDING -> dateFiltered.sortedByDescending { it.timestamp }
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
        _selectedNote,
        _selectedDate
    ) { notes, sortOrder, searchQuery, selectedNote, selectedDate ->
        NotesUiState(
            notes = notes,
            sortOrder = sortOrder,
            searchQuery = searchQuery,
            isLoading = false,
            isEmpty = notes.isEmpty(),
            selectedNote = selectedNote,
            selectedDate = selectedDate
        )
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
                }
            }
        }
    }

    suspend fun saveNoteAndWait(note: Note, userId: String): Boolean {
        return try {
            val result = repository.upsertNote(note, userId)
            if (result.isFailure) {
                _syncError.value = "Note saved locally. Will sync when online."
                true
            } else {
                val syncedNote = result.getOrNull()
                if (syncedNote?.isSynced == false) {
                    _syncMessage.value = "Note saved locally. Will sync when online."
                }
                true
            }
        } catch (e: Exception) {
            _syncError.value = e.message
            false
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
}
