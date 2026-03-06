package com.white.notepilot.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Data class representing a Note with its associated Categories
 */
data class NoteWithCategories(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteCategory::class,
            parentColumn = "note_id",
            entityColumn = "category_id"
        )
    )
    val categories: List<Category>
)
