package com.stymsaw.cc.game.ui.animation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object AnimSpecs {
    val swapSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val clearTween = tween<Float>(durationMillis = 200, easing = EaseInOut)
}