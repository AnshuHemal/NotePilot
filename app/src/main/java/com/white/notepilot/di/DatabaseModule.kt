package com.white.notepilot.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.white.notepilot.data.dao.CategoryDao
import com.white.notepilot.data.dao.NoteDao
import com.white.notepilot.data.dao.NoteImageDao
import com.white.notepilot.data.dao.NotePasswordDao
import com.white.notepilot.data.dao.NotificationDao
import com.white.notepilot.data.database.NoteDatabase
import com.white.notepilot.utils.DBConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(
        @ApplicationContext context: Context
    ): NoteDatabase {
        return Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            DBConstants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: NoteDatabase): NoteDao {
        return database.noteDao()
    }
    
    @Provides
    @Singleton
    fun provideNotificationDao(database: NoteDatabase): NotificationDao {
        return database.notificationDao()
    }
    
    @Provides
    @Singleton
    fun provideCategoryDao(database: NoteDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    @Singleton
    fun provideNoteImageDao(database: NoteDatabase): NoteImageDao {
        return database.noteImageDao()
    }
    
    @Provides
    @Singleton
    fun provideNotePasswordDao(database: NoteDatabase): NotePasswordDao {
        return database.notePasswordDao()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
