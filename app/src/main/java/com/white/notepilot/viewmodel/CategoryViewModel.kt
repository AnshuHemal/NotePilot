package com.white.notepilot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.data.model.Category
import com.white.notepilot.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {
    
    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        initializeDefaultCategories()
    }
    
    private fun initializeDefaultCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeDefaultCategories()
        }
    }
    
    fun addCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.insertCategory(category)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun updateCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateCategory(category)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun deleteCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.deleteCategory(category)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun deleteCategoryWithFirebaseSync(
        category: Category,
        userId: String,
        firebaseRepository: com.white.notepilot.data.repository.FirebaseRepository
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.deleteCategoryWithFirebaseSync(category, userId, firebaseRepository)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun getCategoriesForNote(noteId: Int): StateFlow<List<Category>> {
        return repository.getCategoriesForNote(noteId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
    
    suspend fun getCategoriesForNoteSync(noteId: Int): List<Category> {
        return repository.getCategoriesForNoteSync(noteId)
    }
    
    suspend fun getNotesCountForCategory(categoryId: Int): Int {
        return repository.getNotesCountForCategory(categoryId)
    }
    
    fun updateNoteCategories(noteId: Int, categoryIds: List<Int>, noteFirebaseId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateNoteCategories(noteId, categoryIds, noteFirebaseId)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun syncCategoriesToFirebase(
        userId: String,
        firebaseRepository: com.white.notepilot.data.repository.FirebaseRepository
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.syncCategoriesToFirebase(userId, firebaseRepository)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun fetchCategoriesFromFirebase(
        userId: String,
        firebaseRepository: com.white.notepilot.data.repository.FirebaseRepository
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.fetchCategoriesFromFirebase(userId, firebaseRepository)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
