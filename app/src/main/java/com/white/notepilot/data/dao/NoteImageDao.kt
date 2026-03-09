package com.white.notepilot.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.white.notepilot.data.model.NoteImage
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteImageDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: NoteImage): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<NoteImage>)
    
    @Update
    suspend fun updateImage(image: NoteImage)
    
    @Delete
    suspend fun deleteImage(image: NoteImage)
    
    @Query("SELECT * FROM note_images WHERE note_id = :noteId AND is_deleted = 0 ORDER BY created_at ASC")
    fun getImagesForNote(noteId: Int): Flow<List<NoteImage>>
    
    @Query("SELECT * FROM note_images WHERE note_id = :noteId AND is_deleted = 0 ORDER BY created_at ASC")
    suspend fun getImagesForNoteSync(noteId: Int): List<NoteImage>
    
    @Query("SELECT * FROM note_images WHERE note_id = :noteId ORDER BY created_at ASC")
    suspend fun getAllImagesForNote(noteId: Int): List<NoteImage>
    
    @Query("SELECT * FROM note_images WHERE note_firebase_id = :noteFirebaseId AND is_deleted = 0 ORDER BY created_at ASC")
    suspend fun getImagesByNoteFirebaseId(noteFirebaseId: String): List<NoteImage>
    
    @Query("SELECT * FROM note_images WHERE id = :imageId")
    suspend fun getImageById(imageId: Int): NoteImage?
    
    @Query("SELECT * FROM note_images WHERE is_synced = 0 AND is_deleted = 0")
    suspend fun getUnsyncedImages(): List<NoteImage>
    
    @Query("SELECT * FROM note_images WHERE is_deleted = 1 AND is_synced = 0")
    suspend fun getUnsyncedDeletedImages(): List<NoteImage>
    
    @Query("UPDATE note_images SET is_deleted = 1 WHERE note_id = :noteId")
    suspend fun markImagesAsDeletedForNote(noteId: Int)
    
    @Query("DELETE FROM note_images WHERE note_id = :noteId")
    suspend fun deleteImagesForNote(noteId: Int): Int
    
    @Query("DELETE FROM note_images WHERE id = :imageId")
    suspend fun deleteImageById(imageId: Int)
    
    @Query("UPDATE note_images SET is_synced = 1, cloudinary_url = :cloudinaryUrl, cloudinary_public_id = :cloudinaryPublicId WHERE id = :imageId")
    suspend fun markImageAsSynced(imageId: Int, cloudinaryUrl: String, cloudinaryPublicId: String)
    
    @Query("UPDATE note_images SET is_synced = 1, cloudinary_url = :cloudinaryUrl, cloudinary_public_id = :cloudinaryPublicId, local_path = '' WHERE id = :imageId")
    suspend fun markImageAsSyncedAndClearLocal(imageId: Int, cloudinaryUrl: String, cloudinaryPublicId: String)
    
    @Query("SELECT COUNT(*) FROM note_images WHERE note_id = :noteId AND is_deleted = 0")
    suspend fun getImageCountForNote(noteId: Int): Int
}
