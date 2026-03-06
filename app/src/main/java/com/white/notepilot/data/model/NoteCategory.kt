package com.white.notepilot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.white.notepilot.utils.DBConstants

/**
 * Junction table for many-to-many relationship between Notes and Categories
 */
@Entity(
    tableName = DBConstants.NOTE_CATEGORY_TABLE,
    primaryKeys = ["note_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("note_id"), Index("category_id")]
)
data class NoteCategory(
    @ColumnInfo("note_id")
    val noteId: Int,
    @ColumnInfo("category_id")
    val categoryId: Int
)
