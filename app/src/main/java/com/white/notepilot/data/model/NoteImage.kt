package com.white.notepilot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.white.notepilot.utils.DBConstants

@Entity(
    tableName = DBConstants.NOTE_IMAGE_TABLE,
    indices = [
        Index("note_id"),
        Index("note_firebase_id"),
        Index("cloudinary_public_id")
    ]
)
data class NoteImage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("note_id")
    val noteId: Int,
    @ColumnInfo("note_firebase_id")
    val noteFirebaseId: String? = null,
    @ColumnInfo("cloudinary_public_id")
    val cloudinaryPublicId: String? = null,
    @ColumnInfo("local_path")
    val localPath: String,
    @ColumnInfo("cloudinary_url")
    val cloudinaryUrl: String? = null,
    @ColumnInfo("file_name")
    val fileName: String,
    @ColumnInfo("file_size")
    val fileSize: Long,
    @ColumnInfo("mime_type")
    val mimeType: String = "image/jpeg",
    @ColumnInfo("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo("is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo("is_deleted")
    val isDeleted: Boolean = false
)
