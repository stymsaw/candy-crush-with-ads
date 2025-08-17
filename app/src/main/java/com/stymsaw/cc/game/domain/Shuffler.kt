package com.stymsaw.cc.game.domain

import kotlin.random.Random

/**
 * Helpers for "no moves" detection and reshuffling while avoiding immediate matches.
 */
object Shuffler {

    /**
     * Returns true if there exists ANY adjacent swap that would yield a match.
     * This is O(w*h) and fine for typical 8x8 boards.
     */
    fun hasAnyLegalMove(width: Int, height: Int, tiles: List<Tile?>): Boolean {
        fun index(x: Int, y: Int) = y * width + x

        for (y in 0 until height) {
            for (x in 0 until width) {
                val i = index(x, y)

                // Try swap right
                if (x + 1 < width) {
                    val r = index(x + 1, y)
                    if (wouldMatchAfterSwap(width, height, tiles, i, r)) return true
                }
                // Try swap down
                if (y + 1 < height) {
                    val d = index(x, y + 1)
                    if (wouldMatchAfterSwap(width, height, tiles, i, d)) return true
                }
            }
        }
        return false
    }

    /**
     * Shuffle the board until:
     *  - no immediate 3-in-a-row exists, and
     *  - at least one legal move is available.
     * Returns a NEW list of tiles; caller can write it into the Board.
     */
    fun reshuffle(
        width: Int,
        height: Int,
        tiles: List<Tile?>,
        rand: Random,
        maxAttempts: Int = 200
    ): List<Tile?> {
        val pool = tiles.filterNotNull().toMutableList()
        require(pool.size == width * height) { "Reshuffle expects a full board (no nulls)" }

        repeat(maxAttempts) {
            pool.shuffle(rand)
            if (!hasImmediateMatches(width, height, pool) && hasAnyLegalMove(width, height, pool)) {
                return pool.toList()
            }
        }
        // Fallback: return best-effort (no immediate matches), even if no legal move detected.
        // Caller may decide to regenerate.
        var attempt = 0
        while (attempt++ < maxAttempts) {
            pool.shuffle(rand)
            if (!hasImmediateMatches(width, height, pool)) return pool.toList()
        }
        return pool.toList()
    }

    // ----- Internals -----

    private fun wouldMatchAfterSwap(
        width: Int,
        height: Int,
        tiles: List<Tile?>,
        aIdx: Int,
        bIdx: Int
    ): Boolean {
        if (tiles[aIdx] == null || tiles[bIdx] == null) return false
        // small local copy of two indices
        val ta = tiles[aIdx]; val tb = tiles[bIdx]
        // Check only rows/cols that changed
        fun typeAt(i: Int) = when (i) {
            aIdx -> tb?.type
            bIdx -> ta?.type
            else -> tiles[i]?.type
        }

        val ax = aIdx % width; val ay = aIdx / width
        val bx = bIdx % width; val by = bIdx / width

        // Scan affected rows/cols
        return runMatchScanRow(width, ay, ::typeAt) ||
                runMatchScanRow(width, by, ::typeAt) ||
                runMatchScanCol(width, height, ax, ::typeAt) ||
                runMatchScanCol(width, height, bx, ::typeAt)
    }

    private inline fun runMatchScanRow(
        width: Int,
        y: Int,
        typeAt: (Int) -> TileType?
    ): Boolean {
        var run = 1
        var last = typeAt(y * width)
        for (x in 1 until width) {
            val t = typeAt(y * width + x)
            if (t == last && t != null) {
                run++
                if (run >= 3) return true
            } else {
                run = 1
                last = t
            }
        }
        return false
    }

    private inline fun runMatchScanCol(
        width: Int,
        height: Int,
        x: Int,
        typeAt: (Int) -> TileType?
    ): Boolean {
        var run = 1
        var last = typeAt(x)
        for (y in 1 until height) {
            val t = typeAt(y * width + x)
            if (t == last && t != null) {
                run++
                if (run >= 3) return true
            } else {
                run = 1
                last = t
            }
        }
        return false
    }

    private fun hasImmediateMatches(width: Int, height: Int, tiles: List<Tile?>): Boolean {
        val matches = MatchFinder.findMatches(width, height, tiles)
        return matches.isNotEmpty()
    }
}