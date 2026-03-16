package com.white.notepilot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.data.ml.SmartTagEngine
import com.white.notepilot.data.model.Category
import com.white.notepilot.data.repository.CategoryRepository
import com.white.notepilot.ui.state.SmartTagState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SmartTagViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _tagState = MutableStateFlow<SmartTagState>(SmartTagState.Idle)
    val tagState: StateFlow<SmartTagState> = _tagState.asStateFlow()

    // Tracks which suggestions the user has already dismissed this session
    private val _dismissedSuggestions = MutableStateFlow<Set<String>>(emptySet())

    private var debounceJob: Job? = null

    /**
     * Called whenever title or content changes.
     * Debounces 800 ms so we don't re-analyse on every keystroke.
     */
    fun onTextChanged(
        title: String,
        content: String,
        selectedCategoryIds: List<Int>
    ) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(800)
            analyse(title, content, selectedCategoryIds)
        }
    }

    /**
     * Immediately re-analyse (e.g. when a category chip is added/removed).
     */
    fun reanalyse(
        title: String,
        content: String,
        selectedCategoryIds: List<Int>
    ) {
        debounceJob?.cancel()
        viewModelScope.launch { analyse(title, content, selectedCategoryIds) }
    }

    private suspend fun analyse(
        title: String,
        content: String,
        selectedCategoryIds: List<Int>
    ) {
        val plainText = title + " " + content
        if (plainText.isBlank()) {
            _tagState.value = SmartTagState.Idle
            return
        }

        _tagState.value = SmartTagState.Analysing

        val suggestions = withContext(Dispatchers.Default) {
            // Resolve selected category names to exclude them from suggestions
            val selectedNames = selectedCategoryIds.mapNotNull { id ->
                categoryRepository.getCategoryById(id)?.name
            }.toSet()

            val dismissed = _dismissedSuggestions.value

            SmartTagEngine.suggest(
                title = title,
                content = content,
                existingCategoryNames = selectedNames + dismissed
            )
        }

        _tagState.value = if (suggestions.isEmpty()) SmartTagState.Idle
        else SmartTagState.Ready(suggestions)
    }

    /**
     * User tapped a suggestion chip — find the matching Category and return it.
     * Returns null if the category doesn't exist in the DB yet.
     */
    suspend fun resolveCategory(suggestionName: String): Category? {
        return withContext(Dispatchers.IO) {
            categoryRepository.getCategoryByName(suggestionName)
        }
    }

    /** Hide a suggestion for the rest of this session without adding it. */
    fun dismiss(categoryName: String) {
        _dismissedSuggestions.value = _dismissedSuggestions.value + categoryName
        val current = _tagState.value
        if (current is SmartTagState.Ready) {
            val remaining = current.suggestions.filter { it.categoryName != categoryName }
            _tagState.value = if (remaining.isEmpty()) SmartTagState.Idle
            else SmartTagState.Ready(remaining)
        }
    }

    fun reset() {
        debounceJob?.cancel()
        _tagState.value = SmartTagState.Idle
        _dismissedSuggestions.value = emptySet()
    }
}
