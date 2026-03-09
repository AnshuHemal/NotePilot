package com.white.notepilot.data.repository

import android.content.Context
import android.net.Uri
import com.white.notepilot.data.dao.NoteImageDao
import com.white.notepilot.data.model.NoteImage
import com.white.notepilot.data.remote.CloudinaryService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteImageDao: NoteImageDao,
    private val cloudinaryService: CloudinaryService
) {
    
    private val imagesDir: File by lazy {
        File(context.filesDir, "note_images").apply {
            if (!exists()) mkdirs()
        }
    }
    
    suspend fun saveImageForNote(
        noteId: Int,
        noteFirebaseId: String?,
        imageUri: Uri
    ): Result<NoteImage> {
        return try {
            android.util.Log.d("ImageRepository", "=== Starting saveImageForNote ===")
            android.util.Log.d("ImageRepository", "Note ID: $noteId, Firebase ID: $noteFirebaseId")
            
            val fileName = "IMG_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            val imageFile = File(imagesDir, fileName)
            
            android.util.Log.d("ImageRepository", "Saving to: ${imageFile.absolutePath}")
            
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(imageFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            val fileSize = imageFile.length()
            android.util.Log.d("ImageRepository", "File saved, size: $fileSize bytes")
            
            val noteImage = NoteImage(
                noteId = noteId,
                noteFirebaseId = noteFirebaseId,
                localPath = imageFile.absolutePath,
                fileName = fileName,
                fileSize = fileSize,
                mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg",
                isSynced = false
            )
            
            val imageId = noteImageDao.insertImage(noteImage)
            val savedImage = noteImage.copy(id = imageId.toInt())
            android.util.Log.d("ImageRepository", "Image saved to DB with ID: $imageId")
            
            android.util.Log.d("ImageRepository", "Attempting immediate sync to Cloudinary...")
            try {
                val syncResult = syncImageToCloudinary(savedImage)
                if (syncResult.isSuccess) {
                    android.util.Log.d("ImageRepository", "Immediate sync successful!")
                    Result.success(syncResult.getOrNull()!!)
                } else {
                    val error = syncResult.exceptionOrNull()?.message ?: "Unknown error"
                    android.util.Log.e("ImageRepository", "Immediate sync failed: $error")
                    android.util.Log.d("ImageRepository", "Image saved locally, will retry sync later")
                    Result.success(savedImage)
                }
            } catch (e: Exception) {
                android.util.Log.e("ImageRepository", "Immediate sync exception: ${e.message}", e)
                android.util.Log.d("ImageRepository", "Image saved locally, will retry sync later")
                Result.success(savedImage)
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageRepository", "Failed to save image: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    fun getImagesForNote(noteId: Int): Flow<List<NoteImage>> {
        return noteImageDao.getImagesForNote(noteId)
    }
    
    suspend fun getImagesForNoteSync(noteId: Int): List<NoteImage> {
        return noteImageDao.getImagesForNoteSync(noteId)
    }
    
    suspend fun deleteImage(image: NoteImage): Result<Unit> {
        return try {
            val updatedImage = image.copy(isDeleted = true, isSynced = false)
            noteImageDao.updateImage(updatedImage)
            
            val file = File(image.localPath)
            if (file.exists()) {
                file.delete()
            }
            
            if (image.cloudinaryPublicId != null) {
                cloudinaryService.deleteImage(image.cloudinaryPublicId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun permanentlyDeleteImage(image: NoteImage): Result<Unit> {
        return try {
            noteImageDao.deleteImage(image)
            
            val file = File(image.localPath)
            if (file.exists()) {
                file.delete()
            }
            
            if (image.cloudinaryPublicId != null) {
                cloudinaryService.deleteImage(image.cloudinaryPublicId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncImageToCloudinary(image: NoteImage): Result<NoteImage> {
        return try {
            android.util.Log.d("ImageRepository", "Starting sync for image ${image.id}")
            
            val imageFile = File(image.localPath)
            if (!imageFile.exists()) {
                val error = "Image file not found: ${image.localPath}"
                android.util.Log.e("ImageRepository", error)
                return Result.failure(Exception(error))
            }
            
            android.util.Log.d("ImageRepository", "File found, size: ${imageFile.length()} bytes")
            
            val uploadResult = cloudinaryService.uploadImage(imageFile)
            
            if (uploadResult.isSuccess) {
                val cloudinaryResponse = uploadResult.getOrNull()!!
                android.util.Log.d("ImageRepository", "Upload successful, updating database and cleaning up local file...")
                
                noteImageDao.markImageAsSyncedAndClearLocal(
                    imageId = image.id,
                    cloudinaryUrl = cloudinaryResponse.secureUrl,
                    cloudinaryPublicId = cloudinaryResponse.publicId
                )
                
                if (imageFile.exists()) {
                    val deleted = imageFile.delete()
                    if (deleted) {
                        android.util.Log.d("ImageRepository", "Local file deleted: ${image.localPath}")
                    } else {
                        android.util.Log.w("ImageRepository", "Failed to delete local file: ${image.localPath}")
                    }
                }
                
                val updatedImage = image.copy(
                    cloudinaryUrl = cloudinaryResponse.secureUrl,
                    cloudinaryPublicId = cloudinaryResponse.publicId,
                    localPath = "",
                    isSynced = true
                )
                
                android.util.Log.d("ImageRepository", "Database updated, local file cleaned up")
                Result.success(updatedImage)
            } else {
                val error = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                android.util.Log.e("ImageRepository", "Upload failed: $error")
                Result.failure(uploadResult.exceptionOrNull() ?: Exception(error))
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageRepository", "Sync exception for image ${image.id}", e)
            Result.failure(e)
        }
    }
    
    suspend fun syncAllUnsyncedImages(): Result<Int> {
        return try {
            val unsyncedImages = noteImageDao.getUnsyncedImages()
            var syncedCount = 0
            val errors = mutableListOf<String>()
            
            unsyncedImages.forEach { image ->
                try {
                    val result = syncImageToCloudinary(image)
                    if (result.isSuccess) {
                        syncedCount++
                    } else {
                        errors.add("Image ${image.id}: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    errors.add("Image ${image.id}: ${e.message}")
                }
            }
            
            if (errors.isNotEmpty()) {
                android.util.Log.e("ImageRepository", "Sync errors: ${errors.joinToString(", ")}")
            }
            
            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncExistingImages(): Result<String> {
        return try {
            val result = syncAllUnsyncedImages()
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                Result.success("Successfully synced $count image(s)")
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUnsyncedImages(): List<NoteImage> {
        return noteImageDao.getUnsyncedImages()
    }
    
    suspend fun getImageCountForNote(noteId: Int): Int {
        return noteImageDao.getImageCountForNote(noteId)
    }
    
    suspend fun deleteImagesForNote(noteId: Int) {
        android.util.Log.d("ImageRepository", "Deleting all images for note $noteId")
        
        val allImages = noteImageDao.getAllImagesForNote(noteId)
        android.util.Log.d("ImageRepository", "Found ${allImages.size} images to delete (including soft-deleted)")
        
        allImages.forEach { image ->
            android.util.Log.d("ImageRepository", "Deleting image ${image.id}: ${image.fileName}")
            
            val file = File(image.localPath)
            if (file.exists()) {
                val deleted = file.delete()
                android.util.Log.d("ImageRepository", "Local file deleted: $deleted")
            } else {
                android.util.Log.d("ImageRepository", "Local file not found: ${image.localPath}")
            }
            
            if (image.cloudinaryPublicId != null) {
                android.util.Log.d("ImageRepository", "Deleting from Cloudinary: ${image.cloudinaryPublicId}")
                val result = cloudinaryService.deleteImage(image.cloudinaryPublicId)
                if (result.isSuccess) {
                    android.util.Log.d("ImageRepository", "Cloudinary delete successful")
                } else {
                    android.util.Log.e("ImageRepository", "Cloudinary delete failed: ${result.exceptionOrNull()?.message}")
                }
            } else {
                android.util.Log.d("ImageRepository", "No Cloudinary ID, skipping cloud delete")
            }
        }
        
        val deletedCount = noteImageDao.deleteImagesForNote(noteId)
        android.util.Log.d("ImageRepository", "Deleted $deletedCount database records for note $noteId")
    }
    
    fun imageFileExists(localPath: String): Boolean {
        return File(localPath).exists()
    }
    
    fun getImageFile(localPath: String): File? {
        val file = File(localPath)
        return if (file.exists()) file else null
    }
}
