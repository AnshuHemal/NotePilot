package com.white.notepilot.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.white.notepilot.data.dao.CategoryDao
import com.white.notepilot.data.dao.NoteImageDao
import com.white.notepilot.data.dao.NotificationDao
import com.white.notepilot.data.dao.NoteDao
import com.white.notepilot.data.dao.NotePasswordDao
import com.white.notepilot.data.model.Category
import com.white.notepilot.data.model.Note
import com.white.notepilot.data.model.NoteCategory
import com.white.notepilot.data.model.NoteImage
import com.white.notepilot.data.model.NotePassword
import com.white.notepilot.data.model.Notification

@Database(
    entities = [Note::class, Notification::class, Category::class, NoteCategory::class, NoteImage::class, NotePassword::class],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun notificationDao(): NotificationDao
    abstract fun categoryDao(): CategoryDao
    abstract fun noteImageDao(): NoteImageDao
    abstract fun notePasswordDao(): NotePasswordDao
}
