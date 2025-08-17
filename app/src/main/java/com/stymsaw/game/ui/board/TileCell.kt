package com.stymsaw.cc.game.ui.board

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.stymsaw.cc.game.presentation.TileUi
import kotlin.math.roundToInt

@Composable
fun TileCell(
    tile: TileUi,
    isClearing: Boolean,
    swapInfo: Pair<Int, Int>?,
    onSwap: (from: Int, to: Int) -> Unit,
    boardWidth: Int,
    modifier: Modifier = Modifier
) {
    var dragOffsetX by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    val scale by animateFloatAsState(targetValue = if (isClearing) 0f else 1f, label = "clear-scale")

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(tile.index) {
                detectDragGestures(
                    onDragEnd = {
                        val dx = dragOffsetX
                        val dy = dragOffsetY
                        val threshold = 40f
                        val target = when {
                            dx > threshold -> tile.index + 1
                            dx < -threshold -> tile.index - 1
                            dy > threshold -> tile.index + boardWidth
                            dy < -threshold -> tile.index - boardWidth
                            else -> null
                        }
                        target?.let { onSwap(tile.index, it) }
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    dragOffsetX += dragAmount.x
                    dragOffsetY += dragAmount.y
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = dragOffsetX
                translationY = dragOffsetY
            }
    ) {
        CandyFace(tile)
    }
}