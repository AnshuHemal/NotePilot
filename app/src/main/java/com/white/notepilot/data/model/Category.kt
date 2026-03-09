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
    val icon: String = "category_label",
    @ColumnInfo("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo("is_synced")
    val isSynced: Boolean = false
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category(name = "Personal", color = "#4CAF50", icon = "category_label"),
            Category(name = "Work", color = "#2196F3", icon = "category_label"),
            Category(name = "Ideas", color = "#FF9800", icon = "category_label"),
            Category(name = "Important", color = "#F44336", icon = "category_label"),
            Category(name = "To-Do", color = "#9C27B0", icon = "category_label")
        )
    }
}
