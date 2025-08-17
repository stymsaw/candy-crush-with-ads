package com.stymsaw.cc.game.engine

/**
 * Centralized constants for pacing, scoring, board size, etc.
 * Adjust here to change the "feel" of the game.
 */
object GameRules {
    // Timing (ms)
    const val CLEAR_DELAY_MS = 150L      // fade/shrink before clear
    const val POST_CLEAR_PAUSE_MS = 60L  // pause after clear, before drop
    const val DROP_PAUSE_MS = 80L        // falling animation delay
    const val BETWEEN_CASCADE_MS = 60L   // pause between cascades

    // Gameplay parameters
    const val TILE_TYPE_COUNT = 3
    const val MATCH_MIN = 2

    // Default board dimensions
    const val DEFAULT_WIDTH = 5
    const val DEFAULT_HEIGHT = 8

    // Scoring
    fun scoreFor(cleared: Int, cascadeIndex: Int): Int {
        // Example: give more points on deeper cascades
        return cleared * (1 + cascadeIndex)
    }
}