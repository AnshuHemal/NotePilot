package com.white.notepilot.data.model

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.white.notepilot.utils.DBConstants

@Stable
@Entity(tableName = DBConstants.CATEGORY_TABLE)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("category_id")
    val categoryId: String? = null,
    val name: String,
    val color: String,
    val icon: String = "label",
    @ColumnInfo("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo("is_synced")
    val isSynced: Boolean = false
) {
    companion object {
        // Predefined categories
        val DEFAULT_CATEGORIES = listOf(
            Category(name = "Personal", color = "#4CAF50", icon = "person"),
            Category(name = "Work", color = "#2196F3", icon = "work"),
            Category(name = "Ideas", color = "#FF9800", icon = "lightbulb"),
            Category(name = "Important", color = "#F44336", icon = "star"),
            Category(name = "To-Do", color = "#9C27B0", icon = "checklist")
        )
    }
}
