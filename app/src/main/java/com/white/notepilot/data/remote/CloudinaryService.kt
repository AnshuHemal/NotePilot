package com.white.notepilot.data.remote

import android.content.Context
import android.net.Uri
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudinaryService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun uploadImage(imageFile: File): Result<CloudinaryResponse> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("CloudinaryService", "Starting upload for: ${imageFile.name}")
            android.util.Log.d("CloudinaryService", "File exists: ${imageFile.exists()}, Size: ${imageFile.length()}")
            android.util.Log.d("CloudinaryService", "Cloud name: ${CloudinaryConfig.CLOUD_NAME}")
            android.util.Log.d("CloudinaryService", "Upload URL: ${CloudinaryConfig.UPLOAD_URL}")
            android.util.Log.d("CloudinaryService", "Upload Preset: ${CloudinaryConfig.UPLOAD_PRESET}")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    imageFile.name,
                    imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                .addFormDataPart("folder", "notepilot")
                .build()

            val request = Request.Builder()
                .url(CloudinaryConfig.UPLOAD_URL)
                .post(requestBody)
                .build()

            android.util.Log.d("CloudinaryService", "Sending request...")
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            android.util.Log.d("CloudinaryService", "Response code: ${response.code}")
            android.util.Log.d("CloudinaryService", "Response body: $responseBody")

            if (response.isSuccessful && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val cloudinaryResponse = CloudinaryResponse(
                    publicId = jsonObject.getString("public_id"),
                    secureUrl = jsonObject.getString("secure_url"),
                    url = jsonObject.getString("url"),
                    format = jsonObject.getString("format"),
                    width = jsonObject.getInt("width"),
                    height = jsonObject.getInt("height"),
                    bytes = jsonObject.getLong("bytes"),
                    createdAt = jsonObject.getString("created_at")
                )
                android.util.Log.d("CloudinaryService", "Upload successful: ${cloudinaryResponse.secureUrl}")
                Result.success(cloudinaryResponse)
            } else {
                val errorMsg = "Upload failed: ${response.code} - $responseBody"
                android.util.Log.e("CloudinaryService", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("CloudinaryService", "Upload exception", e)
            Result.failure(e)
        }
    }

    suspend fun uploadImageFromUri(uri: Uri): Result<CloudinaryResponse> = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val result = uploadImage(tempFile)
            tempFile.delete()
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteImage(publicId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis() / 1000
            val signature = generateDeleteSignature(publicId, timestamp)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("public_id", publicId)
                .addFormDataPart("api_key", CloudinaryConfig.API_KEY)
                .addFormDataPart("timestamp", timestamp.toString())
                .addFormDataPart("signature", signature)
                .build()

            val request = Request.Builder()
                .url("${CloudinaryConfig.BASE_URL}image/destroy")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateSignature(timestamp: Long): String {
        val toSign = "timestamp=$timestamp&upload_preset=${CloudinaryConfig.UPLOAD_PRESET}${CloudinaryConfig.API_SECRET}"
        return sha1(toSign)
    }

    private fun generateDeleteSignature(publicId: String, timestamp: Long): String {
        val toSign = "public_id=$publicId&timestamp=$timestamp${CloudinaryConfig.API_SECRET}"
        return sha1(toSign)
    }

    private fun sha1(input: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
