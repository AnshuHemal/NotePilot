package com.white.notepilot.data.model

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.white.notepilot.utils.DBConstants

@Stable
@Entity(tableName = DBConstants.TBL_NAME)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("note_id")
    val noteId: String? = null,
    val title: String,
    val content: String,
    val colorCode: String,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo("is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo("is_deleted")
    val isDeleted : Boolean = false,
    @ColumnInfo("is_pinned")
    val isPinned: Boolean = false,
    @ColumnInfo("is_locked")
    val isLocked: Boolean = false
)
