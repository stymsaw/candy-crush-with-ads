package com.stymsaw.cc.game.model

import kotlin.random.Random
import kotlin.math.abs

data class Tile(
    val type: TileType,
    val id: Long = nextId()
) {
    companion object {
        private var counter = 0L
        private fun nextId() = counter++
    }
}

class Board(
    val width: Int,
    val height: Int,
    private val rand: Random
) {
    private val tiles: Array<Tile?> = arrayOfNulls(width * height)

    fun copyTiles(): List<Tile?> = tiles.toList()
    private fun idx(x: Int, y: Int) = y * width + x
    private fun inBounds(x: Int, y: Int) = x in 0 until width && y in 0 until height

    fun tileAt(x: Int, y: Int): Tile? = if (inBounds(x, y)) tiles[idx(x, y)] else null
    private fun setTile(x: Int, y: Int, t: Tile?) { if (inBounds(x, y)) tiles[idx(x, y)] = t }

    fun fillNewBoard(noImmediateMatches: Boolean = true) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                do {
                    setTile(x, y, Tile(TileType.random(rand)))
                } while (noImmediateMatches && createsImmediateMatch(x, y))
            }
        }
    }

    private fun createsImmediateMatch(x: Int, y: Int): Boolean {
        val t = tileAt(x, y) ?: return false
        // horizontal
        if (x >= 2) {
            val a = tileAt(x - 1, y)?.type
            val b = tileAt(x - 2, y)?.type
            if (a == t.type && b == t.type) return true
        }
        // vertical
        if (y >= 2) {
            val a = tileAt(x, y - 1)?.type
            val b = tileAt(x, y - 2)?.type
            if (a == t.type && b == t.type) return true
        }
        return false
    }

    fun areAdjacent(ax: Int, ay: Int, bx: Int, by: Int): Boolean {
        val dx = abs(ax - bx)
        val dy = abs(ay - by)
        return (dx + dy) == 1
    }

    fun swap(ax: Int, ay: Int, bx: Int, by: Int) {
        val a = tileAt(ax, ay)
        val b = tileAt(bx, by)
        setTile(ax, ay, b)
        setTile(bx, by, a)
    }

    /** Returns set of positions that belong to any 3+ match */
    fun findMatches(): Set<Pair<Int, Int>> {
        val matched = mutableSetOf<Pair<Int, Int>>()
        // Horizontal
        for (y in 0 until height) {
            var runStart = 0
            var lastType: TileType? = null
            for (x in 0 until width) {
                val t = tileAt(x, y)?.type
                if (t != lastType) {
                    if (x - runStart >= 3 && lastType != null) {
                        for (mx in runStart until x) matched += mx to y
                    }
                    runStart = x
                    lastType = t
                }
            }
            if (width - runStart >= 3 && lastType != null) {
                for (mx in runStart until width) matched += mx to y
            }
        }
        // Vertical
        for (x in 0 until width) {
            var runStart = 0
            var lastType: TileType? = null
            for (y in 0 until height) {
                val t = tileAt(x, y)?.type
                if (t != lastType) {
                    if (y - runStart >= 3 && lastType != null) {
                        for (my in runStart until y) matched += x to my
                    }
                    runStart = y
                    lastType = t
                }
            }
            if (height - runStart >= 3 && lastType != null) {
                for (my in runStart until height) matched += x to my
            }
        }
        return matched
    }

    fun clear(matches: Set<Pair<Int, Int>>) {
        matches.forEach { (x, y) -> setTile(x, y, null) }
    }

    fun applyGravity() {
        for (x in 0 until width) {
            var writeY = height - 1
            for (y in (height - 1) downTo 0) {
                val t = tileAt(x, y)
                if (t != null) {
                    if (writeY != y) {
                        setTile(x, writeY, t)
                        setTile(x, y, null)
                    }
                    writeY--
                }
            }
        }
    }

    fun refill() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (tileAt(x, y) == null) {
                    setTile(x, y, Tile(TileType.random(rand)))
                }
            }
        }
    }
}