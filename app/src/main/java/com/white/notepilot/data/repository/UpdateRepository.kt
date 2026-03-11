package com.white.notepilot.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.white.notepilot.data.model.AppUpdate
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val TAG = "UpdateRepository"
        private const val UPDATE_COLLECTION = "app_config"
        private const val UPDATE_DOCUMENT = "update_info"
    }
    
    suspend fun getUpdateInfo(): AppUpdate? {
        return try {
            val document = firestore.collection(UPDATE_COLLECTION)
                .document(UPDATE_DOCUMENT)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(AppUpdate::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting update info", e)
            null
        }
    }
    
    fun getCurrentAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Error getting app version", e)
            "1.0.0"
        }
    }
    
    fun isUpdateRequired(currentVersion: String, minimumVersion: String): Boolean {
        return try {
            compareVersions(currentVersion, minimumVersion) < 0
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing versions", e)
            false
        }
    }
    
    fun isUpdateAvailable(currentVersion: String, latestVersion: String): Boolean {
        return try {
            compareVersions(currentVersion, latestVersion) < 0
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing versions", e)
            false
        }
    }
    
    private fun compareVersions(version1: String, version2: String): Int {
        val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(parts1.size, parts2.size)
        
        for (i in 0 until maxLength) {
            val part1 = parts1.getOrNull(i) ?: 0
            val part2 = parts2.getOrNull(i) ?: 0
            
            when {
                part1 < part2 -> return -1
                part1 > part2 -> return 1
            }
        }
        
        return 0
    }
}