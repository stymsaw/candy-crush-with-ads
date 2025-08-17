package com.stymsaw.cc.game.model

enum class TileType(val label: String) {
    Red("🍒"),
    Green("🍏"),
    Blue("🫐"),
    Yellow("🍋"),
    Purple("🍇");

    companion object {
        private val values = entries.toTypedArray()
        fun random(rand: kotlin.random.Random) = values[rand.nextInt(values.size)]
    }
}