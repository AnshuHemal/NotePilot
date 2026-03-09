package com.white.notepilot.ui.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Login : Routes("login")
    object Home : Routes("home")
    object Notifications : Routes("notifications")
    object Account : Routes("account")
    object Settings : Routes("settings")
    object CreateNote : Routes("create_note?noteId={noteId}") {
        fun createRoute(noteId: Int? = null) = if (noteId != null) "create_note?noteId=$noteId" else "create_note"
    }
    object NoteDetail : Routes("note_detail/{noteId}") {
        fun createRoute(noteId: Int) = "note_detail/$noteId"
    }
    object SearchNote : Routes("search_note")
    object UnsyncedNotes : Routes("unsynced_notes")
    object CategoryManagement : Routes("category_management")
    object ImageSyncTest : Routes("image_sync_test")
}