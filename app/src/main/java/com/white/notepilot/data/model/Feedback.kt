package com.white.notepilot.data.model

import com.white.notepilot.enums.FeedbackStatus
import com.white.notepilot.enums.FeedbackType

data class Feedback(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val feedbackType: FeedbackType = FeedbackType.GENERAL,
    val subject: String = "",
    val description: String = "",
    val deviceInfo: String = "",
    val appVersion: String = "",
    val attachmentUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val status: FeedbackStatus = FeedbackStatus.SUBMITTED
)