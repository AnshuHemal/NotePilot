package com.white.notepilot.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.white.notepilot.data.remote.GeminiConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSummaryRepository @Inject constructor() {

    private val model by lazy {
        GenerativeModel(
            modelName = GeminiConfig.MODEL_NAME,
            apiKey = GeminiConfig.API_KEY
        )
    }

    suspend fun summarizeNote(title: String, plainTextContent: String): Result<String> {
        return try {
            val prompt = buildPrompt(title, plainTextContent)
            val response = model.generateContent(prompt)
            val summary = response.text?.trim()
            if (summary.isNullOrBlank()) {
                Result.failure(Exception("Empty response from AI"))
            } else {
                Result.success(summary)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(title: String, content: String): String {
        val truncated = if (content.length > 4000) content.take(4000) + "..." else content
        return """
            You are a concise note summarizer. Summarize the following note in 2-4 clear, informative bullet points.
            Each bullet point should start with "• ". Be direct and capture the key ideas only.
            Do not add any intro or outro text — just the bullet points.
            
            Note Title: $title
            
            Note Content:
            $truncated
        """.trimIndent()
    }
}
