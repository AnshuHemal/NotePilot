package com.white.notepilot.di

import com.google.firebase.auth.FirebaseAuth
import com.white.notepilot.data.auth.PhoneAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun providePhoneAuthRepository(firebaseAuth: FirebaseAuth): PhoneAuthRepository {
        return PhoneAuthRepository(firebaseAuth)
    }
}
