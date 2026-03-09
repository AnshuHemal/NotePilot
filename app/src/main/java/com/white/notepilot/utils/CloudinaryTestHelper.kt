package com.white.notepilot.utils

import android.content.Context
import android.util.Log
import com.white.notepilot.data.repository.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CloudinaryTestHelper {
    
    fun testSyncExistingImages(
        imageRepository: ImageRepository,
        onResult: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val unsyncedImages = withContext(Dispatchers.IO) {
                    imageRepository.getUnsyncedImages()
                }
                
                Log.d("CloudinaryTest", "Found ${unsyncedImages.size} unsynced images")
                
                if (unsyncedImages.isEmpty()) {
                    onResult("No unsynced images found")
                    return@launch
                }
                
                unsyncedImages.forEach { image ->
                    Log.d("CloudinaryTest", "Image ${image.id}: ${image.fileName}")
                    Log.d("CloudinaryTest", "Local path: ${image.localPath}")
                    Log.d("CloudinaryTest", "File exists: ${java.io.File(image.localPath).exists()}")
                }
                
                val result = withContext(Dispatchers.IO) {
                    imageRepository.syncAllUnsyncedImages()
                }
                
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    val message = "Successfully synced $count out of ${unsyncedImages.size} images"
                    Log.d("CloudinaryTest", message)
                    onResult(message)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("CloudinaryTest", "Sync failed: $error")
                    onResult("Sync failed: $error")
                }
            } catch (e: Exception) {
                Log.e("CloudinaryTest", "Exception during sync", e)
                onResult("Exception: ${e.message}")
            }
        }
    }
    
    fun logCloudinaryConfig() {
        Log.d("CloudinaryTest", "=== Cloudinary Configuration ===")
        Log.d("CloudinaryTest", "Cloud Name: ${com.white.notepilot.data.remote.CloudinaryConfig.CLOUD_NAME}")
        Log.d("CloudinaryTest", "API Key: ${com.white.notepilot.data.remote.CloudinaryConfig.API_KEY}")
        Log.d("CloudinaryTest", "Upload Preset: ${com.white.notepilot.data.remote.CloudinaryConfig.UPLOAD_PRESET}")
        Log.d("CloudinaryTest", "Upload URL: ${com.white.notepilot.data.remote.CloudinaryConfig.UPLOAD_URL}")
        Log.d("CloudinaryTest", "================================")
    }
}
