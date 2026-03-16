package com.white.notepilot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.data.model.Note
import com.white.notepilot.data.repository.AiSearchRepository
import com.white.notepilot.ui.state.AiSearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSearchViewModel @Inject constructor(
    private val repository: AiSearchRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AiSearchState>(AiSearchState.Idle)
    val state: StateFlow<AiSearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String, allNotes: List<Note>) {
        if (query.isBlank()) {
            _state.value = AiSearchState.Idle
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = AiSearchState.Thinking
            val result = repository.search(query, allNotes)
            _state.value = if (result.isSuccess) {
                val matches = result.getOrThrow()
                if (matches.isEmpty()) AiSearchState.Empty(query)
                else AiSearchState.Results(matches)
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Unknown error"
                AiSearchState.Error(
                    when {
                        msg.contains("API_KEY", ignoreCase = true) ||
                        msg.contains("API key", ignoreCase = true) -> "Invalid Gemini API key."
                        msg.contains("quota", ignoreCase = true) -> "API quota exceeded. Try again later."
                        msg.contains("network", ignoreCase = true) ||
                        msg.contains("connect", ignoreCase = true) -> "No internet connection."
                        else -> "AI search failed. Try again."
                    }
                )
            }
        }
    }

    fun reset() {
        searchJob?.cancel()
        _state.value = AiSearchState.Idle
    }
}
