package com.white.notepilot.data.repository

import com.white.notepilot.data.dao.NotificationDao
import com.white.notepilot.data.model.Notification
import com.white.notepilot.enums.NotificationType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    
    fun getAllNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications()
    }

    fun getUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }
    
    suspend fun addNotification(
        title: String,
        message: String,
        type: NotificationType,
        noteId: Int? = null,
        noteTitle: String? = null
    ): Result<Long> {
        return try {
            val notification = Notification(
                title = title,
                message = message,
                type = type,
                noteId = noteId,
                noteTitle = noteTitle
            )
            val id = notificationDao.insertNotification(notification)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markAsRead(notificationId: Int): Result<Unit> {
        return try {
            notificationDao.markAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            notificationDao.markAllAsRead()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteNotification(notificationId: Int): Result<Unit> {
        return try {
            notificationDao.deleteNotification(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAllNotifications(): Result<Unit> {
        return try {
            notificationDao.deleteAllNotifications()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cleanupOldNotifications(daysOld: Int = 30): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
            notificationDao.deleteOldNotifications(timestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
