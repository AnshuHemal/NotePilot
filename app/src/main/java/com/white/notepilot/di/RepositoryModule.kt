package com.white.notepilot.di

import com.google.firebase.firestore.FirebaseFirestore
import com.white.notepilot.ui.components.ads.RewardedAdManager
import com.white.notepilot.data.repository.FeedbackRepository
import com.white.notepilot.data.repository.FirebaseRepository
import com.white.notepilot.data.repository.RewardsRepository
import com.white.notepilot.data.repository.SubscriptionRepository
import com.white.notepilot.data.repository.UpdateRepository
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
    
    @Provides
    @Singleton
    fun provideRewardsRepository(
        firestore: FirebaseFirestore
    ): RewardsRepository {
        return RewardsRepository(firestore)
    }
    
    @Provides
    @Singleton
    fun provideUpdateRepository(
        firestore: FirebaseFirestore
    ): UpdateRepository {
        return UpdateRepository(firestore)
    }
    
    @Provides
    @Singleton
    fun provideRewardedAdManager(): RewardedAdManager {
        return RewardedAdManager()
    }
    
    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        firestore: FirebaseFirestore,
        rewardsRepository: RewardsRepository
    ): SubscriptionRepository {
        return SubscriptionRepository(firestore, rewardsRepository)
    }
}