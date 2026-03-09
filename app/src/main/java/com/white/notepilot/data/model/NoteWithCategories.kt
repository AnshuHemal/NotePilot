package com.white.notepilot.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteWithCategories(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteCategory::class,
            parentColumn = "note_local_id",
            entityColumn = "category_local_id"
        )
    )
    val categories: List<Category>
)
