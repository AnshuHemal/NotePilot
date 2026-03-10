package com.white.notepilot.ads

import kotlin.random.Random

object AdPositionCalculator {
    fun calculateAdPositions(totalItems: Int): Set<Int> {
        val positions = mutableSetOf<Int>()
        
        if (totalItems <= 0) return positions
        
        // First ad always appears after the 1st note
        var currentPosition = 1
        if (currentPosition < totalItems) {
            positions.add(currentPosition)
        }
        
        // Subsequent ads appear after random intervals
        while (currentPosition < totalItems) {
            currentPosition += Random.nextInt(
                AdMobConfig.MIN_NOTES_BEFORE_AD,
                AdMobConfig.MAX_NOTES_BEFORE_AD + 1
            )
            
            if (currentPosition < totalItems) {
                positions.add(currentPosition)
            }
        }
        
        return positions
    }
}
