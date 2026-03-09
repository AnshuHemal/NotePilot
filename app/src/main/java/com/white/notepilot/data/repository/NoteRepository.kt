package com.white.notepilot.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresExtension
import com.google.firebase.FirebaseNetworkException
import com.white.notepilot.data.dao.NoteDao
import com.white.notepilot.data.model.Note
import com.white.notepilot.data.preferences.NotificationPreferences
import com.white.notepilot.enums.NotificationType
import com.white.notepilot.utils.NetworkUtils
import com.white.notepilot.utils.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val firestoreRepository: FirebaseRepository,
    private val networkUtils: NetworkUtils,
    private val notificationPreferences: NotificationPreferences,
    private val notificationRepository: NotificationRepository,
    private val imageRepository: ImageRepository,
    @ApplicationContext private val context: Context
) {
    
    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
    }
    
    suspend fun getNoteById(noteId: Int): Note? {
        return noteDao.getNoteById(noteId)
    }
    
    suspend fun getUnsyncedNotes(): List<Note> {
        return noteDao.getUnsyncedNotes()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun upsertNote(note: Note, userId: String): Result<Note> {
        return try {
            if (note.id > 0) {
                val existingNote = noteDao.getNoteById(note.id)
                if (existingNote != null) {
                    val updatedNote = if (networkUtils.isNetworkAvailable() && userId.isNotBlank()) {
                        val firestoreResult = firestoreRepository.syncNoteToFirestore(
                            note = note,
                            userId = userId,
                            firestoreId = note.noteId
                        )
                        
                        if (firestoreResult.isSuccess) {
                            val firestoreId = firestoreResult.getOrNull()
                            val synced = note.copy(noteId = firestoreId, isSynced = true)
                            synced
                        } else {
                            note.copy(isSynced = false)
                        }
                    } else {
                        note.copy(isSynced = false)
                    }
                    
                    noteDao.upsertNote(updatedNote)
                    return Result.success(updatedNote)
                }
            }
            
            if (note.noteId != null) {
                val existingNote = noteDao.getNoteByNoteId(note.noteId)
                if (existingNote != null) {
                    val updatedNote = existingNote.copy(
                        title = note.title,
                        content = note.content,
                        colorCode = note.colorCode,
                        timestamp = note.timestamp,
                        isSynced = note.isSynced
                    )
                    noteDao.upsertNote(updatedNote)
                    return Result.success(updatedNote)
                }
            }
            
            if (userId.isBlank()) {
                val existingNote = noteDao.getNoteByTitleAndContent(note.title, note.content)
                if (existingNote != null) {
                    return Result.success(existingNote)
                }
                
                val unsyncedNote = note.copy(id = 0, noteId = null, isSynced = false)
                val insertedId = noteDao.insertNote(unsyncedNote)
                val insertedNote = unsyncedNote.copy(id = insertedId.toInt())
                return Result.success(insertedNote)
            }
            
            if (networkUtils.isNetworkAvailable()) {
                val firestoreResult = firestoreRepository.syncNoteToFirestore(
                    note = note,
                    userId = userId,
                    firestoreId = note.noteId
                )
                
                if (firestoreResult.isSuccess) {
                    val firestoreId = firestoreResult.getOrNull()
                    
                    if (firestoreId.isNullOrBlank()) {
                        val unsyncedNote = note.copy(id = 0, noteId = null, isSynced = false)
                        val insertedId = noteDao.insertNote(unsyncedNote)
                        val insertedNote = unsyncedNote.copy(id = insertedId.toInt())
                        return Result.failure(Exception("Firestore did not return a valid document ID"))
                    }
                    
                    val syncedNote = note.copy(
                        id = 0,
                        noteId = firestoreId,
                        isSynced = true
                    )
                    val insertedId = noteDao.insertNote(syncedNote)
                    val insertedNote = syncedNote.copy(id = insertedId.toInt())
                    return Result.success(insertedNote)
                } else {
                    val unsyncedNote = note.copy(id = 0, noteId = null, isSynced = false)
                    val insertedId = noteDao.insertNote(unsyncedNote)
                    val insertedNote = unsyncedNote.copy(id = insertedId.toInt())
                    return Result.failure(firestoreResult.exceptionOrNull() ?: Exception("Firestore sync failed"))
                }
            } else {
                val unsyncedNote = note.copy(id = 0, noteId = null, isSynced = false)
                val insertedId = noteDao.insertNote(unsyncedNote)
                val insertedNote = unsyncedNote.copy(id = insertedId.toInt())
                return Result.success(insertedNote)
            }
        } catch (_: FirebaseNetworkException) {
            val unsyncedNote = note.copy(id = 0, noteId = null, isSynced = false)
            val insertedId = noteDao.insertNote(unsyncedNote)
            val insertedNote = unsyncedNote.copy(id = insertedId.toInt())
            return Result.success(insertedNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(note: Note): Result<Unit> {
        return try {
            noteDao.markNoteAsDeleted(note.id)
            Result.success(Unit)
        } catch (_: Exception) {
            noteDao.markNoteAsDeleted(note.id)
            Result.success(Unit)
        }
    }

    suspend fun permanentlyDeleteNote(note: Note, userId: String): Result<Unit> {
        return try {
            android.util.Log.d("NoteRepository", "Permanently deleting note ${note.id} with images")
            
            imageRepository.deleteImagesForNote(note.id)
            
            if (networkUtils.isNetworkAvailable() && note.noteId != null && userId.isNotBlank()) {
                val deleteResult = firestoreRepository.deleteNoteFromFirestore(
                    firestoreId = note.noteId,
                    userId = userId
                )
                
                if (deleteResult.isSuccess) {
                    noteDao.deleteNote(note)
                    android.util.Log.d("NoteRepository", "Note and images deleted successfully")
                    return Result.success(Unit)
                } else {
                    noteDao.deleteNote(note)
                    android.util.Log.w("NoteRepository", "Firestore delete failed but local delete succeeded")
                    return Result.failure(deleteResult.exceptionOrNull() ?: Exception("Firestore delete failed"))
                }
            } else {
                noteDao.deleteNote(note)
                android.util.Log.d("NoteRepository", "Note and images deleted locally (offline)")
                return Result.success(Unit)
            }
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error permanently deleting note", e)
            noteDao.deleteNote(note)
            Result.failure(e)
        }
    }

    suspend fun deleteAllNotes(userId: String): Result<Unit> {
        return try {
            noteDao.deleteAllNotes()
            
            if (networkUtils.isNetworkAvailable()) {
                firestoreRepository.deleteAllUserNotes(userId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAndSyncNotesFromFirestore(userId: String): Result<Unit> {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val firestoreResult = firestoreRepository.fetchUserNotes(userId)
            
            if (firestoreResult.isSuccess) {
                val firestoreNotes = firestoreResult.getOrNull() ?: emptyList()
                
                firestoreNotes.forEach { noteData ->
                    val firestoreId = noteData["firestoreId"] as? String
                    
                    if (firestoreId != null) {
                        val existingNote = noteDao.getNoteByNoteId(firestoreId)
                        
                        if (existingNote != null && !existingNote.isSynced) {
                            return@forEach
                        }
                        
                        val note = existingNote?.copy(
                            title = noteData["title"] as? String ?: existingNote.title,
                            content = noteData["content"] as? String ?: existingNote.content,
                            colorCode = noteData["colorCode"] as? String ?: existingNote.colorCode,
                            timestamp = noteData["timestamp"] as? Long ?: existingNote.timestamp,
                            noteId = firestoreId,
                            isSynced = true
                        )
                            ?: Note(
                                id = 0,
                                title = noteData["title"] as? String ?: "",
                                content = noteData["content"] as? String ?: "",
                                colorCode = noteData["colorCode"] as? String ?: "",
                                timestamp = noteData["timestamp"] as? Long ?: System.currentTimeMillis(),
                                noteId = firestoreId,
                                isSynced = true
                            )
                        
                        noteDao.upsertNote(note)
                    }
                }
                
                Result.success(Unit)
            } else {
                Result.failure(firestoreResult.exceptionOrNull() ?: Exception("Failed to fetch notes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncUnsyncedNotesToFirestore(userId: String): Result<Int> {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val backgroundSyncEnabled = notificationPreferences.backgroundSyncEnabled.first()
            if (!backgroundSyncEnabled) {
                return Result.failure(Exception("Background sync is disabled"))
            }
            
            val unsyncedNotes = noteDao.getUnsyncedNotes()
            var syncedCount = 0
            
            unsyncedNotes.forEach { note ->
                if (note.noteId != null && note.isSynced) {
                    return@forEach
                }
                
                val firestoreResult = firestoreRepository.syncNoteToFirestore(
                    note = note,
                    userId = userId,
                    firestoreId = note.noteId
                )
                
                if (firestoreResult.isSuccess) {
                    val firestoreId = firestoreResult.getOrNull()
                    
                    val existingNoteWithSameFirestoreId = if (firestoreId != null) {
                        noteDao.getNoteByNoteId(firestoreId)
                    } else {
                        null
                    }
                    
                    if (existingNoteWithSameFirestoreId != null && existingNoteWithSameFirestoreId.id != note.id) {
                        noteDao.deleteNote(note)
                    } else {
                        val syncedNote = note.copy(
                            noteId = firestoreId,
                            isSynced = true
                        )
                        noteDao.upsertNote(syncedNote)
                    }
                    syncedCount++
                }
            }
            
            val unsyncedDeletedNotes = noteDao.getUnsyncedDeletedNotes()
            
            unsyncedDeletedNotes.forEach { note ->
                if (note.noteId != null) {
                    val deleteResult = firestoreRepository.deleteNoteFromFirestore(
                        firestoreId = note.noteId,
                        userId = userId
                    )
                    
                    if (deleteResult.isSuccess) {
                        noteDao.deleteNote(note)
                        syncedCount++
                    }
                } else {
                    noteDao.deleteNote(note)
                }
            }
            
            noteDao.cleanupSyncedDeletedNotes()
            
            noteDao.removeDuplicateNotes()

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cleanupDuplicates() {
        try {
            noteDao.removeDuplicateNotes()
            noteDao.removeDuplicatesByContent()
        } catch (_: Exception) {
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun forceSyncNote(note: Note, userId: String): Result<Note> {
        return try {
            if (userId.isBlank()) {
                return Result.failure(Exception("User ID is required"))
            }
            
            if (!networkUtils.isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val firestoreResult = firestoreRepository.syncNoteToFirestore(
                note = note,
                userId = userId,
                firestoreId = note.noteId
            )
            
            if (firestoreResult.isSuccess) {
                val firestoreId = firestoreResult.getOrNull()
                
                if (firestoreId.isNullOrBlank()) {
                    return Result.failure(Exception("Firestore did not return a valid document ID"))
                }
                
                val syncedNote = note.copy(
                    noteId = firestoreId,
                    isSynced = true
                )
                noteDao.updateNote(syncedNote)
                
                val notificationTitle = "Note Synced"
                val notificationMessage = "\"${note.title}\" has been synced to cloud"
                
                notificationRepository.addNotification(
                    title = notificationTitle,
                    message = notificationMessage,
                    type = NotificationType.NOTE_SYNCED,
                    noteId = note.id,
                    noteTitle = note.title
                )
                
                NotificationHelper.showSyncNotification(
                    context = context,
                    title = notificationTitle,
                    message = notificationMessage,
                    noteId = note.id
                )
                
                Result.success(syncedNote)
            } else {
                Result.failure(firestoreResult.exceptionOrNull() ?: Exception("Firestore sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUnsyncedDeletedNotes(): List<Note> {
        return noteDao.getUnsyncedDeletedNotes()
    }

    suspend fun restoreDeletedNote(note: Note): Result<Unit> {
        return try {
            val restoredNote = note.copy(isDeleted = false)
            noteDao.upsertNote(restoredNote)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
