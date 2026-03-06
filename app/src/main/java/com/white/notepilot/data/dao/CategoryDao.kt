package com.white.notepilot.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.white.notepilot.data.model.Category
import com.white.notepilot.data.model.NoteCategory
import com.white.notepilot.data.model.NoteWithCategories
import com.white.notepilot.utils.DBConstants
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    // Category CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("SELECT * FROM ${DBConstants.CATEGORY_TABLE} ORDER BY created_at DESC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM ${DBConstants.CATEGORY_TABLE} WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): Category?
    
    @Query("SELECT * FROM ${DBConstants.CATEGORY_TABLE} WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?
    
    @Query("DELETE FROM ${DBConstants.CATEGORY_TABLE}")
    suspend fun deleteAllCategories()
    
    // Note-Category relationship operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteCategory(noteCategory: NoteCategory)
    
    @Delete
    suspend fun deleteNoteCategory(noteCategory: NoteCategory)
    
    @Query("DELETE FROM ${DBConstants.NOTE_CATEGORY_TABLE} WHERE note_id = :noteId")
    suspend fun deleteAllCategoriesForNote(noteId: Int)
    
    @Query("DELETE FROM ${DBConstants.NOTE_CATEGORY_TABLE} WHERE category_id = :categoryId")
    suspend fun deleteAllNotesForCategory(categoryId: Int)
    
    // Get notes with categories
    @Transaction
    @Query("SELECT * FROM ${DBConstants.TBL_NAME} WHERE is_deleted = 0 ORDER BY timestamp DESC")
    fun getNotesWithCategories(): Flow<List<NoteWithCategories>>
    
    @Transaction
    @Query("SELECT * FROM ${DBConstants.TBL_NAME} WHERE id = :noteId")
    suspend fun getNoteWithCategories(noteId: Int): NoteWithCategories?
    
    // Get categories for a specific note
    @Query("""
        SELECT c.* FROM ${DBConstants.CATEGORY_TABLE} c
        INNER JOIN ${DBConstants.NOTE_CATEGORY_TABLE} nc ON c.id = nc.category_id
        WHERE nc.note_id = :noteId
        ORDER BY c.name ASC
    """)
    fun getCategoriesForNote(noteId: Int): Flow<List<Category>>
    
    @Query("""
        SELECT c.* FROM ${DBConstants.CATEGORY_TABLE} c
        INNER JOIN ${DBConstants.NOTE_CATEGORY_TABLE} nc ON c.id = nc.category_id
        WHERE nc.note_id = :noteId
        ORDER BY c.name ASC
    """)
    suspend fun getCategoriesForNoteSync(noteId: Int): List<Category>
    
    // Get notes for a specific category
    @Query("""
        SELECT n.* FROM ${DBConstants.TBL_NAME} n
        INNER JOIN ${DBConstants.NOTE_CATEGORY_TABLE} nc ON n.id = nc.note_id
        WHERE nc.category_id = :categoryId AND n.is_deleted = 0
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForCategory(categoryId: Int): Flow<List<com.white.notepilot.data.model.Note>>
    
    // Count notes in category
    @Query("""
        SELECT COUNT(*) FROM ${DBConstants.NOTE_CATEGORY_TABLE} nc
        INNER JOIN ${DBConstants.TBL_NAME} n ON nc.note_id = n.id
        WHERE nc.category_id = :categoryId AND n.is_deleted = 0
    """)
    suspend fun getNotesCountForCategory(categoryId: Int): Int
    
    // Check if note has category
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM ${DBConstants.NOTE_CATEGORY_TABLE}
            WHERE note_id = :noteId AND category_id = :categoryId
        )
    """)
    suspend fun noteHasCategory(noteId: Int, categoryId: Int): Boolean
}
