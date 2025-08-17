package com.stymsaw.cc.game.domain

/**
 * Pure functions for detecting matches on a tiles snapshot.
 * Input is a List<Tile?> so we can test independently from Board.
 */
object MatchFinder {

    /**
     * Returns a Set of indices that belong to any 3+ match (horizontal or vertical).
     */
    fun findMatches(width: Int, height: Int, tiles: List<Tile?>): Set<Index> {
        val matched = HashSet<Index>(width * height / 3)

        // Horizontal runs
        for (y in 0 until height) {
            var runStart = 0
            var lastType: TileType? = null
            for (x in 0 until width) {
                val t = tiles[y * width + x]?.type
                if (t != lastType) {
                    if (x - runStart >= 3 && lastType != null) {
                        for (mx in runStart until x) matched += Index.of(mx, y)
                    }
                    runStart = x
                    lastType = t
                }
            }
            if (width - runStart >= 3 && lastType != null) {
                for (mx in runStart until width) matched += Index.of(mx, y)
            }
        }

        // Vertical runs
        for (x in 0 until width) {
            var runStart = 0
            var lastType: TileType? = null
            for (y in 0 until height) {
                val t = tiles[y * width + x]?.type
                if (t != lastType) {
                    if (y - runStart >= 3 && lastType != null) {
                        for (my in runStart until y) matched += Index.of(x, my)
                    }
                    runStart = y
                    lastType = t
                }
            }
            if (height - runStart >= 3 && lastType != null) {
                for (my in runStart until height) matched += Index.of(x, my)
            }
        }

        return matched
    }
}