package com.stymsaw.cc.game.engine

/**
 * One-shot effects triggered by game events.
 * Example: play sound, vibrate, show fireworks.
 *
 * These are separated so GameViewModel can emit them
 * and UI layer can react (haptics, SFX).
 */
sealed interface Effect {
    data class MatchCleared(val count: Int) : Effect
    data object InvalidSwap : Effect
    data object NoMovesReshuffle : Effect
}