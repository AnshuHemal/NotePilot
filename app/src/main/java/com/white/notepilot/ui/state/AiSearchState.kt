package com.white.notepilot.ui.state

import com.white.notepilot.data.repository.AiSearchRepository

sealed class AiSearchState {
    data object Idle : AiSearchState()
    data object Thinking : AiSearchState()
    data class Results(val matches: List<AiSearchRepository.AiSearchMatch>) : AiSearchState()
    data class Empty(val query: String) : AiSearchState()
    data class Error(val message: String) : AiSearchState()
}
