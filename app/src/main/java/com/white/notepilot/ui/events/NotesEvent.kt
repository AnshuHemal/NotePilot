package com.white.notepilot.ui.events

import com.white.notepilot.data.model.Note
import com.white.notepilot.enums.SortOrder

sealed class NotesEvent {
    data class UpdateSortOrder(val sortOrder: SortOrder) : NotesEvent()
    data class UpdateSearchQuery(val query: String) : NotesEvent()
    data class UpdateSelectedDate(val date: Long?) : NotesEvent()
    data class UpdateSelectedCategories(val categoryIds: List<Int>) : NotesEvent()
    data class UpsertNote(val note: Note, val userId: String) : NotesEvent()
    data class DeleteNote(val note: Note, val userId: String) : NotesEvent()
    data class DeleteAllNotes(val userId: String) : NotesEvent()
    data class GetNoteById(val noteId: Int) : NotesEvent()
    data class TogglePinStatus(val note: Note) : NotesEvent()
    data class RefreshNotes(val userId: String) : NotesEvent()
}
