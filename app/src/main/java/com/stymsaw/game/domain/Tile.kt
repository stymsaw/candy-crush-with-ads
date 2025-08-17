package com.stymsaw.cc.game.domain

import kotlin.random.Random

/**
 * Visual/type of a candy. Keep only game-logic here.
 * UI can map types to emojis/colors later.
 */
enum class TileType {
    Red, Green, Blue, Yellow, Purple;

    companion object {
        private val values = entries.toTypedArray()
        fun random(rand: Random) = values[rand.nextInt(values.size)]
    }
}

/**
 * A tile on the board. 'id' is stable so UI can animate moves reliably.
 */
data class Tile(
    val type: TileType,
    val id: Long = nextId()
) {
    companion object {
        private var counter = 0L
        private fun nextId() = counter++
    }
}