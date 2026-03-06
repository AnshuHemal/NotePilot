package com.white.notepilot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.white.notepilot.enums.NotificationType
import com.white.notepilot.utils.DBConstants

@Entity(tableName = DBConstants.NOTIFICATION_TABLE)
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo("is_read")
    val isRead: Boolean = false,
    @ColumnInfo("note_id")
    val noteId: Int? = null,
    @ColumnInfo("note_title")
    val noteTitle: String? = null
)
