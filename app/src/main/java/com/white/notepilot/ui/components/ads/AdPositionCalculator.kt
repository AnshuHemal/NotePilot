package com.white.notepilot.ui.components.ads

import com.white.notepilot.utils.Constants
import kotlin.random.Random

object AdPositionCalculator {
    fun calculateAdPositions(totalItems: Int): Set<Int> {
        val positions = mutableSetOf<Int>()
        
        if (totalItems <= 0) return positions
        
        var currentPosition = 1
        if (currentPosition < totalItems) {
            positions.add(currentPosition)
        }
        
        while (currentPosition < totalItems) {
            currentPosition += Random.nextInt(
                Constants.MIN_NOTES_BEFORE_AD,
                Constants.MAX_NOTES_BEFORE_AD + 1
            )
            
            if (currentPosition < totalItems) {
                positions.add(currentPosition)
            }
        }
        
        return positions
    }
}
