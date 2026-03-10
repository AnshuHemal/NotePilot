package com.white.notepilot.di

import com.white.notepilot.data.repository.FeedbackRepository
import com.white.notepilot.data.repository.FirebaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideFeedbackRepository(
        firebaseRepository: FirebaseRepository
    ): FeedbackRepository {
        return FeedbackRepository(firebaseRepository)
    }
}