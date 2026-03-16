package com.white.notepilot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.white.notepilot.data.model.NotePassword

@Dao
interface NotePasswordDao {
    
    @Query("SELECT * FROM note_passwords WHERE note_id = :noteId LIMIT 1")
    suspend fun getPasswordForNote(noteId: Int): NotePassword?
    
    @Query("SELECT * FROM note_passwords WHERE note_firebase_id = :noteFirebaseId LIMIT 1")
    suspend fun getPasswordByFirebaseId(noteFirebaseId: String): NotePassword?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: NotePassword): Long
    
    @Update
    suspend fun updatePassword(password: NotePassword)
    
    @Query("DELETE FROM note_passwords WHERE note_id = :noteId")
    suspend fun deletePasswordForNote(noteId: Int)
    
    @Query("SELECT * FROM note_passwords WHERE is_synced = 0")
    suspend fun getUnsyncedPasswords(): List<NotePassword>
    
    @Query("UPDATE note_passwords SET is_synced = 1 WHERE note_id = :noteId")
    suspend fun markPasswordAsSynced(noteId: Int)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPasswordSync(password: NotePassword): Long
    
    @Query("SELECT lock_type FROM note_passwords WHERE note_id = :noteId LIMIT 1")
    suspend fun getLockType(noteId: Int): String?
}
