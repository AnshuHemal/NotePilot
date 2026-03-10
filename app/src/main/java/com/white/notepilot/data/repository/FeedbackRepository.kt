package com.white.notepilot.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.white.notepilot.data.model.Feedback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepository @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
    
    suspend fun submitFeedback(
        feedback: Feedback,
        context: Context
    ): Result<String> {
        return try {
            if (!isNetworkAvailable(context)) {
                return Result.failure(Exception("No internet connection available"))
            }
            
            firebaseRepository.submitFeedback(feedback)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected == true
        }
    }
    
    fun getDeviceInfo(): String {
        return buildString {
            append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
            append("App Version: 1.0.0\n")
        }
    }
}