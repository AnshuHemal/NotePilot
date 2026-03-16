package com.white.notepilot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.data.repository.AiSummaryRepository
import com.white.notepilot.ui.state.AiSummaryState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSummaryViewModel @Inject constructor(
    private val repository: AiSummaryRepository
) : ViewModel() {

    private val _summaryState = MutableStateFlow<AiSummaryState>(AiSummaryState.Idle)
    val summaryState: StateFlow<AiSummaryState> = _summaryState.asStateFlow()

    fun summarize(title: String, plainTextContent: String) {
        if (plainTextContent.isBlank()) {
            _summaryState.value = AiSummaryState.Error("Note has no content to summarize.")
            return
        }
        viewModelScope.launch {
            _summaryState.value = AiSummaryState.Loading
            val result = repository.summarizeNote(title, plainTextContent)
            _summaryState.value = if (result.isSuccess) {
                AiSummaryState.Success(result.getOrThrow())
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Something went wrong"
                AiSummaryState.Error(
                    when {
                        msg.contains("API_KEY", ignoreCase = true) ||
                        msg.contains("API key", ignoreCase = true) -> "Invalid API key. Please check your Gemini API key."
                        msg.contains("quota", ignoreCase = true) -> "API quota exceeded. Try again later."
                        msg.contains("network", ignoreCase = true) ||
                        msg.contains("connect", ignoreCase = true) -> "No internet connection."
                        else -> "Failed to summarize: $msg"
                    }
                )
            }
        }
    }

    fun reset() {
        _summaryState.value = AiSummaryState.Idle
    }
}
