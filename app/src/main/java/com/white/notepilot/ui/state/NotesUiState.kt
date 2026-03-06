package com.white.notepilot.ui.state

import com.white.notepilot.data.model.Note
import com.white.notepilot.enums.SortOrder

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val selectedNote: Note? = null,
    val selectedDate: Long? = null
)
