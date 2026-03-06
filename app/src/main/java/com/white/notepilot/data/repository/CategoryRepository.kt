package com.white.notepilot.data.repository

import com.white.notepilot.data.dao.CategoryDao
import com.white.notepilot.data.model.Category
import com.white.notepilot.data.model.NoteCategory
import com.white.notepilot.data.model.NoteWithCategories
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    
    // Category operations
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }
    
    suspend fun getCategoryById(categoryId: Int): Category? {
        return categoryDao.getCategoryById(categoryId)
    }
    
    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)
    }
    
    suspend fun insertCategory(category: Category): Result<Long> {
        return try {
            val id = categoryDao.insertCategory(category)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            categoryDao.updateCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCategory(category: Category): Result<Unit> {
        return try {
            categoryDao.deleteCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAllCategories(): Result<Unit> {
        return try {
            categoryDao.deleteAllCategories()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Note-Category relationship operations
    suspend fun addCategoryToNote(noteId: Int, categoryId: Int): Result<Unit> {
        return try {
            val noteCategory = NoteCategory(noteId = noteId, categoryId = categoryId)
            categoryDao.insertNoteCategory(noteCategory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeCategoryFromNote(noteId: Int, categoryId: Int): Result<Unit> {
        return try {
            val noteCategory = NoteCategory(noteId = noteId, categoryId = categoryId)
            categoryDao.deleteNoteCategory(noteCategory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateNoteCategories(noteId: Int, categoryIds: List<Int>): Result<Unit> {
        return try {
            // Remove all existing categories for this note
            categoryDao.deleteAllCategoriesForNote(noteId)
            
            // Add new categories
            categoryIds.forEach { categoryId ->
                val noteCategory = NoteCategory(noteId = noteId, categoryId = categoryId)
                categoryDao.insertNoteCategory(noteCategory)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAllCategoriesForNote(noteId: Int): Result<Unit> {
        return try {
            categoryDao.deleteAllCategoriesForNote(noteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Query operations
    fun getNotesWithCategories(): Flow<List<NoteWithCategories>> {
        return categoryDao.getNotesWithCategories()
    }
    
    suspend fun getNoteWithCategories(noteId: Int): NoteWithCategories? {
        return categoryDao.getNoteWithCategories(noteId)
    }
    
    fun getCategoriesForNote(noteId: Int): Flow<List<Category>> {
        return categoryDao.getCategoriesForNote(noteId)
    }
    
    suspend fun getCategoriesForNoteSync(noteId: Int): List<Category> {
        return categoryDao.getCategoriesForNoteSync(noteId)
    }
    
    fun getNotesForCategory(categoryId: Int): Flow<List<com.white.notepilot.data.model.Note>> {
        return categoryDao.getNotesForCategory(categoryId)
    }
    
    suspend fun getNotesCountForCategory(categoryId: Int): Int {
        return categoryDao.getNotesCountForCategory(categoryId)
    }
    
    suspend fun noteHasCategory(noteId: Int, categoryId: Int): Boolean {
        return categoryDao.noteHasCategory(noteId, categoryId)
    }
    
    // Initialize default categories
    suspend fun initializeDefaultCategories(): Result<Unit> {
        return try {
            Category.DEFAULT_CATEGORIES.forEach { category ->
                // Only insert if category doesn't exist
                if (getCategoryByName(category.name) == null) {
                    insertCategory(category)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
