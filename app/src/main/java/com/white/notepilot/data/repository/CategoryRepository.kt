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
    
    suspend fun addCategoryToNote(
        noteId: Int,
        categoryId: Int,
        noteFirebaseId: String?,
        categoryFirebaseId: String?
    ): Result<Unit> {
        return try {
            val noteFbId = noteFirebaseId ?: "temp_note_$noteId"
            val categoryFbId = categoryFirebaseId ?: "temp_category_$categoryId"
            
            val noteCategory = NoteCategory(
                noteFirebaseId = noteFbId,
                categoryFirebaseId = categoryFbId,
                noteLocalId = noteId,
                categoryLocalId = categoryId
            )
            categoryDao.insertNoteCategory(noteCategory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeCategoryFromNote(noteId: Int, categoryId: Int): Result<Unit> {
        return try {
            categoryDao.deleteAllCategoriesForNote(noteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateNoteCategories(
        noteId: Int,
        categoryIds: List<Int>,
        noteFirebaseId: String? = null
    ): Result<Unit> {
        return try {
            categoryDao.deleteAllCategoriesForNote(noteId)
            
            categoryIds.forEach { categoryId ->
                val category = categoryDao.getCategoryById(categoryId)
                val noteFbId = noteFirebaseId ?: "temp_note_$noteId"
                val categoryFbId = category?.categoryId ?: "temp_category_$categoryId"
                
                val noteCategory = NoteCategory(
                    noteFirebaseId = noteFbId,
                    categoryFirebaseId = categoryFbId,
                    noteLocalId = noteId,
                    categoryLocalId = categoryId
                )
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
    
    suspend fun initializeDefaultCategories(): Result<Unit> {
        return try {
            Category.DEFAULT_CATEGORIES.forEach { category ->
                if (getCategoryByName(category.name) == null) {
                    insertCategory(category)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncCategoriesToFirebase(
        userId: String,
        firebaseRepository: FirebaseRepository
    ): Result<Unit> {
        return try {
            val categories = categoryDao.getAllCategoriesSync()
            
            categories.forEach { category ->
                if (!category.isSynced) {
                    val result = firebaseRepository.syncCategoryToFirestore(
                        category = category,
                        userId = userId,
                        firestoreId = category.categoryId
                    )
                    
                    if (result.isSuccess) {
                        val firestoreId = result.getOrNull()
                        if (firestoreId != null) {
                            val updatedCategory = category.copy(
                                categoryId = firestoreId,
                                isSynced = true
                            )
                            categoryDao.updateCategory(updatedCategory)
                        }
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchCategoriesFromFirebase(
        userId: String,
        firebaseRepository: FirebaseRepository
    ): Result<Unit> {
        return try {
            val result = firebaseRepository.fetchUserCategories(userId)
            
            if (result.isSuccess) {
                val firebaseCategories = result.getOrNull() ?: emptyList()
                
                firebaseCategories.forEach { categoryData ->
                    val name = categoryData["name"] as? String ?: return@forEach
                    val color = categoryData["color"] as? String ?: "#4CAF50"
                    val icon = categoryData["icon"] as? String ?: "label"
                    val createdAt = (categoryData["created_at"] as? Long) ?: System.currentTimeMillis()
                    val firestoreId = categoryData["firestoreId"] as? String
                    
                    val existingCategory = getCategoryByName(name)
                    
                    if (existingCategory == null) {
                        val newCategory = Category(
                            name = name,
                            color = color,
                            icon = icon,
                            createdAt = createdAt,
                            categoryId = firestoreId,
                            isSynced = true
                        )
                        categoryDao.insertCategory(newCategory)
                    } else if (existingCategory.categoryId == null && firestoreId != null) {
                        val updatedCategory = existingCategory.copy(
                            categoryId = firestoreId,
                            isSynced = true
                        )
                        categoryDao.updateCategory(updatedCategory)
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
