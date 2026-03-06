package com.white.notepilot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.white.notepilot.data.model.Notification
import com.white.notepilot.utils.DBConstants
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM ${DBConstants.NOTIFICATION_TABLE} ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>
    
    @Query("SELECT * FROM ${DBConstants.NOTIFICATION_TABLE} WHERE is_read = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(): Flow<List<Notification>>
    
    @Query("SELECT COUNT(*) FROM ${DBConstants.NOTIFICATION_TABLE} WHERE is_read = 0")
    fun getUnreadCount(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long
    
    @Update
    suspend fun updateNotification(notification: Notification)
    
    @Query("UPDATE ${DBConstants.NOTIFICATION_TABLE} SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Int)
    
    @Query("UPDATE ${DBConstants.NOTIFICATION_TABLE} SET is_read = 1")
    suspend fun markAllAsRead()
    
    @Query("DELETE FROM ${DBConstants.NOTIFICATION_TABLE} WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: Int)
    
    @Query("DELETE FROM ${DBConstants.NOTIFICATION_TABLE}")
    suspend fun deleteAllNotifications()
    
    @Query("DELETE FROM ${DBConstants.NOTIFICATION_TABLE} WHERE timestamp < :timestamp")
    suspend fun deleteOldNotifications(timestamp: Long)
}
