package com.white.notepilot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_passwords")
data class NotePassword(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("note_id")
    val noteId: Int,
    @ColumnInfo("note_firebase_id")
    val noteFirebaseId: String?,
    @ColumnInfo("password_hash")
    val passwordHash: String,
    @ColumnInfo("lock_type")
    val lockType: String = "PASSWORD", // PASSWORD or BIOMETRIC
    @ColumnInfo("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo("is_synced")
    val isSynced: Boolean = false
)
