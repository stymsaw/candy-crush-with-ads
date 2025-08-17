package com.stymsaw.cc.game.domain

enum class Direction(val dx: Int, val dy: Int) {
    Left(-1, 0), Right(1, 0), Up(0, -1), Down(0, 1);

    companion object {
        val cardinals = values()
    }
}