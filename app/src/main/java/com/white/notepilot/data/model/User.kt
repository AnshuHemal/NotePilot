package com.white.notepilot.data.model

data class User(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
) {
    fun getInitials(): String {
        val name = displayName ?: email ?: "U"
        val parts = name.split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercase() ?: ""}${parts[1].firstOrNull()?.uppercase() ?: ""}"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "U"
        }
    }
}
