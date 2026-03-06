package com.white.notepilot.utils

import kotlin.random.Random
import androidx.core.graphics.toColorInt

object ColorUtils {
    fun generateRandomColorCode(): String {
        val random = Random.Default
        return String.format(
            "%02X%02X%02X",
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
    }

    fun colorCodeToInt(colorCode: String): Int {
        return "#$colorCode".toColorInt()
    }
}
