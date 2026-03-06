package com.white.notepilot.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.white.notepilot.data.model.Note
import com.white.notepilot.utils.DBConstants
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    
    @Query("SELECT * FROM ${DBConstants.TBL_NAME} WHERE is_deleted = 0 ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM ${DBConstants.TBL_NAME} WHERE id = :id AND is_deleted = 0")
    suspend fun getNoteById(id: Int): Note?
    
    @Query("SELECT * FROM ${DBConstants.TBL_NAME} WHERE note_id = :noteId AND is_deleted = 0 LIMIT 1")
    suspend fun getNoteByNoteId(noteId: String): Note?
    
    @Query("SELECT * FROM ${DBConstants.TBL_NAME} WHERE title = :title AND content = :content AND timestamp = :timestamp AND is_deleted = 0 LIMIT 1")
    suspend fun getNoteByContent(title: String, content: String, timestamp: Long): Note?
    
    @Query("SELECT * FROM ${DBConstants.TBL_NAME} WHERE title = :title AND content = :content AND is_deleted = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getNoteByTitleAndContent(title: String, content: String): Note?
    
    @Query("SELECT * FROM ${DBConstants.TBL_NAME} WHERE is_synced = 0 AND is_deleted = 0")
    suspend fun getUnsyncedNotes(): List<Note>
    
    @Query("SELECT * FROM ${DBConstants.TBL_NAME} WHERE is_deleted = 1 AND is_synced = 0")
    suspend fun getUnsyncedDeletedNotes(): List<Note>
    
    @Upsert
    suspend fun upsertNote(note: Note)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Note): Long
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("DELETE FROM ${DBConstants.TBL_NAME}")
    suspend fun deleteAllNotes()
    
    @Query("UPDATE ${DBConstants.TBL_NAME} SET is_deleted = 1, is_synced = 0 WHERE id = :id")
    suspend fun markNoteAsDeleted(id: Int)

    @Query("DELETE FROM ${DBConstants.TBL_NAME} WHERE is_deleted = 1 AND is_synced = 1")
    suspend fun cleanupSyncedDeletedNotes()
    
    @Query("""
        DELETE FROM ${DBConstants.TBL_NAME} 
        WHERE id NOT IN (
            SELECT MIN(id) 
            FROM ${DBConstants.TBL_NAME} 
            WHERE note_id IS NOT NULL 
            GROUP BY note_id
        ) 
        AND note_id IS NOT NULL
    """)
    suspend fun removeDuplicateNotes()
    
    @Query("""
        DELETE FROM ${DBConstants.TBL_NAME} 
        WHERE id NOT IN (
            SELECT MIN(id) 
            FROM ${DBConstants.TBL_NAME} 
            WHERE is_deleted = 0
            GROUP BY title, content
        ) 
        AND is_deleted = 0
    """)
    suspend fun removeDuplicatesByContent()
}
