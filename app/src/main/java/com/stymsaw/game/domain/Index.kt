package com.stymsaw.cc.game.domain

/**
 * Compact 1D index encoding (x,y) into a single Int:
 *  - lower 16 bits: x
 *  - upper 16 bits: y
 *
 * This avoids allocating Pair<Int,Int> during scans and cascades.
 * Works for board dimensions < 65536 (well beyond any match-3 grid).
 */
@JvmInline
value class Index(val value: Int) {
    val x: Int get() = value and 0xFFFF
    val y: Int get() = value ushr 16

    companion object {
        fun of(x: Int, y: Int): Index = Index((y shl 16) or (x and 0xFFFF))
    }
}