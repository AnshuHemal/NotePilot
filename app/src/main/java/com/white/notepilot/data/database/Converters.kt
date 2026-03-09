package com.white.notepilot.data.database

import androidx.room.TypeConverter
import com.white.notepilot.enums.NotificationType

class Converters {
    
    @TypeConverter
    fun fromNotificationType(value: NotificationType): String {
        return value.name
    }
    
    @TypeConverter
    fun toNotificationType(value: String): NotificationType {
        return try {
            NotificationType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            NotificationType.GENERAL
        }
    }
}
