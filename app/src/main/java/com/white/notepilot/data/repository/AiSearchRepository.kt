package com.white.notepilot.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.white.notepilot.data.model.Note
import com.white.notepilot.data.remote.GeminiConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSearchRepository @Inject constructor() {

    data class AiSearchMatch(
        val note: Note,
        /** Short human-readable reason why this note matched, e.g. "Mentions client meeting on the 5th" */
        val reason: String,
        /** 0.0–1.0 relevance score returned by the model */
        val score: Float
    )

    private val model by lazy {
        GenerativeModel(
            modelName = GeminiConfig.MODEL_NAME,
            apiKey = GeminiConfig.API_KEY
        )
    }

    /**
     * Sends [query] + a compact index of [notes] to Gemini and returns
     * a ranked list of [AiSearchMatch]es.
     *
     * The model receives only note IDs, titles, and a 200-char content
     * snippet — never full content — to stay within token limits.
     */
    suspend fun search(query: String, notes: List<Note>): Result<List<AiSearchMatch>> {
        if (notes.isEmpty()) return Result.success(emptyList())
        if (query.isBlank()) return Result.success(emptyList())

        return try {
            val prompt = buildPrompt(query, notes)
            val response = model.generateContent(prompt)
            val raw = response.text?.trim() ?: return Result.success(emptyList())
            val matches = parseResponse(raw, notes)
            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── prompt builder ────────────────────────────────────────────────────────

    private fun buildPrompt(query: String, notes: List<Note>): String {
        val noteIndex = notes.joinToString("\n") { note ->
            val snippet = note.content
                .replace(Regex("<[^>]+>"), " ")   // strip HTML
                .replace(Regex("\\s+"), " ")
                .trim()
                .take(200)
            "ID:${note.id} | TITLE:${note.title} | CONTENT:$snippet"
        }

        return """
You are a semantic note search engine. Given a user query and a list of notes, return the most relevant notes.

USER QUERY: "$query"

NOTES:
$noteIndex

INSTRUCTIONS:
- Analyse the query semantically — understand intent, not just keywords.
- Return ONLY notes that are genuinely relevant to the query.
- For each match output exactly one line in this format (no extra text):
  MATCH|<id>|<score 0.0-1.0>|<one-sentence reason>
- Score 1.0 = perfect match, 0.0 = irrelevant. Only include notes with score >= 0.3.
- Sort by score descending.
- If no notes match, output: NO_MATCHES
- Do NOT include any other text, headers, or explanation outside the MATCH lines.
        """.trimIndent()
    }

    // ── response parser ───────────────────────────────────────────────────────

    private fun parseResponse(raw: String, notes: List<Note>): List<AiSearchMatch> {
        if (raw.trim() == "NO_MATCHES") return emptyList()

        val noteMap = notes.associateBy { it.id }
        val matches = mutableListOf<AiSearchMatch>()

        raw.lines().forEach { line ->
            val trimmed = line.trim()
            if (!trimmed.startsWith("MATCH|")) return@forEach
            val parts = trimmed.split("|", limit = 4)
            if (parts.size < 4) return@forEach

            val id = parts[1].trim().toIntOrNull() ?: return@forEach
            val score = parts[2].trim().toFloatOrNull()?.coerceIn(0f, 1f) ?: return@forEach
            val reason = parts[3].trim()
            val note = noteMap[id] ?: return@forEach

            matches.add(AiSearchMatch(note = note, reason = reason, score = score))
        }

        return matches.sortedByDescending { it.score }
    }
}
