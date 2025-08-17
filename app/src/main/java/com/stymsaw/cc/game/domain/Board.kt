package com.stymsaw.cc.game.domain

import kotlin.math.abs
import kotlin.random.Random

/**
 * Mutable board storage + primitive operations (get/set/swap/gravity/refill).
 * Higher-level logic (match finding, reshuffle, rules) lives outside.
 */
class Board(
    val width: Int,
    val height: Int,
    private val rand: Random
) {
    private val tiles: Array<Tile?> = arrayOfNulls(width * height)

    // ----- Basic utilities -----
    private inline fun idx(x: Int, y: Int) = y * width + x
    fun inBounds(x: Int, y: Int) = x in 0 until width && y in 0 until height

    fun get(x: Int, y: Int): Tile? = if (inBounds(x, y)) tiles[idx(x, y)] else null
    fun set(x: Int, y: Int, t: Tile?) { if (inBounds(x, y)) tiles[idx(x, y)] = t }

    fun areAdjacent(ax: Int, ay: Int, bx: Int, by: Int): Boolean {
        val dx = abs(ax - bx); val dy = abs(ay - by)
        return dx + dy == 1
    }

    fun swap(ax: Int, ay: Int, bx: Int, by: Int) {
        val a = get(ax, ay)
        val b = get(bx, by)
        set(ax, ay, b)
        set(bx, by, a)
    }

    fun tilesSnapshot(): List<Tile?> = tiles.toList()

    // ----- Generation / Refilling -----
    /**
     * Fills the board. If [noImmediateMatches] = true, avoids creating
     * instant 3-in-a-row in either axis during generation.
     */
    fun fillNewBoard(noImmediateMatches: Boolean = true) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                do {
                    set(x, y, Tile(TileType.random(rand)))
                } while (noImmediateMatches && createsImmediateMatchAt(x, y))
            }
        }
    }

    /**
     * Checks only the new cell at (x,y) for an immediate 3+ run with its left/up neighbors.
     */
    fun createsImmediateMatchAt(x: Int, y: Int): Boolean {
        val t = get(x, y)?.type ?: return false
        // horizontal check
        if (x >= 2) {
            val a = get(x - 1, y)?.type
            val b = get(x - 2, y)?.type
            if (a == t && b == t) return true
        }
        // vertical check
        if (y >= 2) {
            val a = get(x, y - 1)?.type
            val b = get(x, y - 2)?.type
            if (a == t && b == t) return true
        }
        return false
    }

    // ----- Clear / Gravity / Refill cycle -----
    fun clear(indices: Set<Index>) {
        indices.forEach { set(it.x, it.y, null) }
    }

    fun applyGravity() {
        for (x in 0 until width) {
            var writeY = height - 1
            for (y in (height - 1) downTo 0) {
                val t = get(x, y)
                if (t != null) {
                    if (writeY != y) {
                        set(x, writeY, t)
                        set(x, y, null)
                    }
                    writeY--
                }
            }
        }
    }

    fun refill() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (get(x, y) == null) {
                    set(x, y, Tile(TileType.random(rand)))
                }
            }
        }
    }
}