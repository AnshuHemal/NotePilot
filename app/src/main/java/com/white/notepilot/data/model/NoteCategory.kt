package com.white.notepilot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.white.notepilot.utils.DBConstants

@Entity(
    tableName = DBConstants.NOTE_CATEGORY_TABLE,
    primaryKeys = ["note_firebase_id", "category_firebase_id"],
    indices = [
        Index("note_firebase_id"),
        Index("category_firebase_id"),
        Index("note_local_id"),
        Index("category_local_id")
    ]
)
data class NoteCategory(
    @ColumnInfo("note_firebase_id")
    val noteFirebaseId: String,
    @ColumnInfo("category_firebase_id")
    val categoryFirebaseId: String,
    @ColumnInfo("note_local_id")
    val noteLocalId: Int,
    @ColumnInfo("category_local_id")
    val categoryLocalId: Int,
    @ColumnInfo("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
