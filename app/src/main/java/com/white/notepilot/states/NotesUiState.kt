package com.white.notepilot.states

import com.white.notepilot.data.model.Note

sealed class NotesUiState {
    object Loading : NotesUiState()
    object Empty : NotesUiState()
    data class Success(val notes: List<Note>) : NotesUiState()
}