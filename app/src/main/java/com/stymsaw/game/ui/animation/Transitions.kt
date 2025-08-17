package com.stymsaw.cc.game.ui.animation

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Modifier helpers for swap & clear animations.
 */
fun Modifier.swapModifier(
    idx: Int,
    swapping: Pair<Int, Int>?
): Modifier {
    if (swapping == null) return this
    val (from, to) = swapping
    return if (idx == from || idx == to) {
        this.graphicsLayer {
            // Could animate offset here with AnimSpecs.swapSpring
        }
    } else this
}