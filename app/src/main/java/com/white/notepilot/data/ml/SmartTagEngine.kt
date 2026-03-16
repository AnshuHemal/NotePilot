package com.white.notepilot.data.ml

/**
 * On-device Smart Tag Engine.
 *
 * Runs entirely offline — no ML Kit model download required.
 * Uses a keyword-to-category mapping with TF-IDF-style scoring:
 *   - keyword frequency in the text boosts the score
 *   - title matches are weighted 2× vs body matches
 *   - a minimum confidence threshold filters noise
 */
object SmartTagEngine {

    data class TagSuggestion(
        val categoryName: String,
        val confidence: Float,   // 0.0 – 1.0
        val matchedKeywords: List<String>
    )

    // ── keyword → category mapping ────────────────────────────────────────────
    private val CATEGORY_KEYWORDS: Map<String, List<String>> = mapOf(
        "Work" to listOf(
            "meeting", "deadline", "project", "task", "client", "report",
            "presentation", "agenda", "sprint", "standup", "review", "office",
            "email", "colleague", "manager", "team", "budget", "proposal",
            "contract", "invoice", "milestone", "deliverable", "kpi", "okr",
            "quarterly", "annual", "strategy", "roadmap", "stakeholder"
        ),
        "Personal" to listOf(
            "family", "friend", "birthday", "anniversary", "vacation", "trip",
            "home", "house", "personal", "diary", "journal", "memory",
            "relationship", "wedding", "baby", "health", "doctor", "appointment",
            "gym", "workout", "fitness", "diet", "sleep", "mood", "feeling"
        ),
        "Ideas" to listOf(
            "idea", "concept", "brainstorm", "innovation", "creative", "design",
            "startup", "product", "feature", "solution", "hypothesis", "theory",
            "experiment", "prototype", "mvp", "vision", "inspiration", "thought",
            "imagine", "what if", "could", "might", "potential", "explore"
        ),
        "Important" to listOf(
            "urgent", "important", "critical", "priority", "asap", "immediately",
            "must", "required", "essential", "key", "vital", "crucial",
            "deadline", "emergency", "alert", "warning", "reminder", "don't forget",
            "remember", "note to self", "action required", "follow up"
        ),
        "To-Do" to listOf(
            "todo", "to-do", "to do", "task", "checklist", "buy", "get",
            "pick up", "call", "email", "schedule", "book", "order", "pay",
            "fix", "clean", "organize", "prepare", "finish", "complete",
            "submit", "send", "check", "review", "update", "install", "download"
        ),
        "Finance" to listOf(
            "money", "budget", "expense", "income", "salary", "payment",
            "invoice", "bill", "tax", "investment", "savings", "loan",
            "credit", "debit", "bank", "account", "transaction", "cost",
            "price", "fee", "subscription", "insurance", "mortgage", "rent"
        ),
        "Health" to listOf(
            "health", "doctor", "medicine", "medication", "symptom", "pain",
            "exercise", "diet", "nutrition", "calories", "weight", "sleep",
            "mental health", "anxiety", "stress", "therapy", "hospital",
            "prescription", "vitamin", "supplement", "blood", "heart", "fitness"
        ),
        "Learning" to listOf(
            "learn", "study", "course", "book", "read", "research", "notes",
            "lecture", "tutorial", "practice", "skill", "knowledge", "education",
            "university", "school", "exam", "quiz", "assignment", "chapter",
            "concept", "definition", "summary", "review", "understand"
        ),
        "Travel" to listOf(
            "travel", "trip", "flight", "hotel", "booking", "itinerary",
            "passport", "visa", "destination", "city", "country", "airport",
            "train", "bus", "car", "road trip", "backpack", "luggage",
            "tour", "sightseeing", "restaurant", "map", "navigate"
        ),
        "Shopping" to listOf(
            "buy", "purchase", "shop", "store", "price", "discount", "sale",
            "order", "delivery", "amazon", "cart", "wishlist", "product",
            "brand", "review", "compare", "size", "color", "stock", "item"
        ),
        "Recipe" to listOf(
            "recipe", "cook", "bake", "ingredient", "cup", "tablespoon",
            "teaspoon", "oven", "pan", "boil", "fry", "grill", "mix",
            "flour", "sugar", "butter", "egg", "milk", "salt", "pepper",
            "minutes", "preheat", "serve", "portion", "calories"
        ),
        "Technology" to listOf(
            "code", "programming", "software", "app", "api", "database",
            "server", "cloud", "deploy", "bug", "fix", "feature", "git",
            "commit", "branch", "pull request", "kotlin", "java", "python",
            "javascript", "android", "ios", "web", "backend", "frontend",
            "algorithm", "function", "class", "variable", "debug"
        )
    )

    private const val MIN_CONFIDENCE = 0.15f
    private const val MAX_SUGGESTIONS = 3
    private const val TITLE_WEIGHT = 2.0f

    /**
     * Analyse [title] + [content] and return up to [MAX_SUGGESTIONS] category suggestions
     * sorted by confidence descending.
     *
     * @param existingCategoryNames names already assigned — excluded from results
     */
    fun suggest(
        title: String,
        content: String,
        existingCategoryNames: Set<String> = emptySet()
    ): List<TagSuggestion> {
        val titleTokens = tokenize(title)
        val contentTokens = tokenize(content)
        val totalTokens = (titleTokens.size * TITLE_WEIGHT + contentTokens.size).coerceAtLeast(1f)

        val scores = mutableMapOf<String, Pair<Float, MutableList<String>>>()

        CATEGORY_KEYWORDS.forEach { (category, keywords) ->
            if (category in existingCategoryNames) return@forEach

            var score = 0f
            val matched = mutableListOf<String>()

            keywords.forEach { keyword ->
                val titleHits = countOccurrences(titleTokens, keyword) * TITLE_WEIGHT
                val contentHits = countOccurrences(contentTokens, keyword).toFloat()
                val hits = titleHits + contentHits
                if (hits > 0f) {
                    score += hits
                    if (keyword !in matched) matched.add(keyword)
                }
            }

            if (score > 0f) {
                // Normalise: score / totalTokens, then scale by keyword density
                val normalised = (score / totalTokens * 10f).coerceIn(0f, 1f)
                scores[category] = Pair(normalised, matched)
            }
        }

        return scores
            .filter { it.value.first >= MIN_CONFIDENCE }
            .map { (cat, pair) -> TagSuggestion(cat, pair.first, pair.second) }
            .sortedByDescending { it.confidence }
            .take(MAX_SUGGESTIONS)
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("<[^>]+>"), " ")   // strip HTML tags
            .replace(Regex("[^a-z0-9\\s'-]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 1 }
    }

    private fun countOccurrences(tokens: List<String>, keyword: String): Int {
        val kwTokens = keyword.lowercase().split(" ")
        return if (kwTokens.size == 1) {
            tokens.count { it == kwTokens[0] }
        } else {
            // multi-word phrase: sliding window
            var count = 0
            for (i in 0..tokens.size - kwTokens.size) {
                if (tokens.subList(i, i + kwTokens.size) == kwTokens) count++
            }
            count
        }
    }
}
