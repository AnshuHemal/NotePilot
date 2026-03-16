package com.white.notepilot.ui.state

sealed class AiSummaryState {
    data object Idle : AiSummaryState()
    data object Loading : AiSummaryState()
    data class Success(val summary: String) : AiSummaryState()
    data class Error(val message: String) : AiSummaryState()
}
