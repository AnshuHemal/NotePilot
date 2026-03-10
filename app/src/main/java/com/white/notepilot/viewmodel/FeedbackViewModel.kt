package com.white.notepilot.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.data.model.Feedback
import com.white.notepilot.data.repository.FeedbackRepository
import com.white.notepilot.enums.FeedbackType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()
    
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()
    
    fun updateFeedbackType(type: FeedbackType) {
        _uiState.value = _uiState.value.copy(feedbackType = type)
    }
    
    fun updateSubject(subject: String) {
        _uiState.value = _uiState.value.copy(subject = subject)
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    fun submitFeedback(
        userId: String,
        userEmail: String,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSubmitting.value = true
            
            try {
                val currentState = _uiState.value
                
                if (!isValidInput(currentState)) {
                    onError("Please fill in all required fields")
                    _isSubmitting.value = false
                    return@launch
                }
                
                val feedback = Feedback(
                    userId = userId,
                    userEmail = userEmail,
                    feedbackType = currentState.feedbackType,
                    subject = currentState.subject,
                    description = currentState.description,
                    deviceInfo = feedbackRepository.getDeviceInfo(),
                    appVersion = "1.0.0",
                    attachmentUrls = currentState.attachmentUrls
                )
                
                val result = feedbackRepository.submitFeedback(feedback, context)
                
                if (result.isSuccess) {
                    onSuccess()
                    clearForm()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                    onError(error)
                }
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred while submitting feedback")
            } finally {
                _isSubmitting.value = false
            }
        }
    }
    
    private fun isValidInput(state: FeedbackUiState): Boolean {
        return state.subject.isNotBlank() && 
               state.description.isNotBlank()
    }
    
    private fun clearForm() {
        _uiState.value = FeedbackUiState()
    }
}

data class FeedbackUiState(
    val feedbackType: FeedbackType = FeedbackType.GENERAL,
    val subject: String = "",
    val description: String = "",
    val attachmentUrls: List<String> = emptyList()
)