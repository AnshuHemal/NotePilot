package com.white.notepilot.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.white.notepilot.data.repository.ImageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ImageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageRepository: ImageRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val syncResult = imageRepository.syncAllUnsyncedImages()
            
            if (syncResult.isSuccess) {
                val syncedCount = syncResult.getOrNull() ?: 0
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
