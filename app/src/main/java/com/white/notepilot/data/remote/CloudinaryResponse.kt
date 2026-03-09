package com.white.notepilot.data.remote

import com.google.gson.annotations.SerializedName

data class CloudinaryResponse(
    @SerializedName("public_id")
    val publicId: String,
    @SerializedName("secure_url")
    val secureUrl: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("format")
    val format: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("bytes")
    val bytes: Long,
    @SerializedName("created_at")
    val createdAt: String
)
