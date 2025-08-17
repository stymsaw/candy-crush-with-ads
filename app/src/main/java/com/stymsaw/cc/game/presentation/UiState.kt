package com.stymsaw.cc.game.presentation

import com.stymsaw.cc.game.domain.Index
import com.stymsaw.cc.game.domain.TileType

/**
 * Immutable state consumed by the UI.
 * ViewModel should only expose this (via StateFlow).
 */
data class GameUiState(
    val width: Int = 8,
    val height: Int = 8,

    // Flat list of UI tiles in row-major order (size = width * height).
    // Keep it UI-friendly and stable (ids must not change unless the tile changes).
    val tiles: List<TileUi> = emptyList(),

    // Game progression
    val score: Int = 0,
    val isProcessing: Boolean = false,   // true while swapping or resolving cascades

    // Transient animation hints for the UI
    val clearing: Set<Index> = emptySet(),     // cells fading out this frame
    val swapping: Pair<Int, Int>? = null,      // indices involved in the current swap (a -> b)

    // Optional: mode/status flags
    val movesLeft: Int? = null,                // null for endless/time modes
    val timeRemainingMs: Long? = null,         // null for move-based modes
    val message: String? = null                // small status banner if you want
)

/**
 * A UI-facing tile snapshot.
 * Decouples UI from domain Tile; you can render by emoji, color, shape, etc.
 */
data class TileUi(
    val id: Long,              // stable key for animations
    val type: TileType,        // domain type (UI maps to visuals)
    val index: Int             // flattened index (y * width + x)
)