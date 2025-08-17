package com.stymsaw.cc.game.ui.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stymsaw.cc.game.presentation.TileUi
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.math.abs

/**
 * Axis-locked drag (horizontal OR vertical), rubber-band clamped to one cell,
 * and a springy swap animation driven by [swapInfo].
 */
@Composable
fun TileCell(
    tile: TileUi,
    isClearing: Boolean, // currently unused here; your clear anim can live in the cell background
    swapInfo: Pair<Int, Int>?,
    boardWidth: Int,
    size: Dp = 48.dp,
    onSwap: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // --- Drag state (user interaction) ---
    val cellPx = with(LocalDensity.current) { size.toPx() }
    val dragX = remember { Animatable(0f) }
    val dragY = remember { Animatable(0f) }
    var lockedAxis by remember { mutableStateOf(Axis.None) }

    // --- Swap animation (programmatic) ---
    // These offsets animate when ViewModel sets swapInfo = a to b
    val swapX = remember { Animatable(0f) }
    val swapY = remember { Animatable(0f) }

    // Drive the swap animation from VM state
    LaunchedEffect(swapInfo) {
        if (swapInfo == null) {
            swapX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
            swapY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
        } else {
            val (a, b) = swapInfo
            if (tile.index == a || tile.index == b) {
                val ax = a % boardWidth; val ay = a / boardWidth
                val bx = b % boardWidth; val by = b / boardWidth
                val dx = (bx - ax).coerceIn(-1, 1)
                val dy = (by - ay).coerceIn(-1, 1)
                swapX.snapTo(0f); swapY.snapTo(0f)
                // Nudge one whole cell with a spring
                swapX.animateTo(dx * cellPx, spring(stiffness = Spring.StiffnessMedium))
                swapY.animateTo(dy * cellPx, spring(stiffness = Spring.StiffnessMedium))
            } else {
                swapX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                swapY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
            }
        }
    }

    // Axis-locked drag gestures
    val thresholdPx = with(LocalDensity.current) { 14.dp.toPx() } // small nudge to lock axis

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(tile.index) {
                detectDragGestures(
                    onDragEnd = {
                        // Decide neighbor based on locked axis + offset
                        val toIndex = when (lockedAxis) {
                            Axis.Horizontal -> when {
                                dragX.value > 0.5f * cellPx -> tile.index + 1
                                dragX.value < -0.5f * cellPx -> tile.index - 1
                                else -> null
                            }
                            Axis.Vertical -> when {
                                dragY.value > 0.5f * cellPx -> tile.index + boardWidth
                                dragY.value < -0.5f * cellPx -> tile.index - boardWidth
                                else -> null
                            }
                            Axis.None -> null
                        }
                        toIndex?.let { onSwap(tile.index, it) }

                        // Rubber-band back
                        scope.launch { dragX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) }
                        scope.launch { dragY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) }
                        lockedAxis = Axis.None
                    }
                ) { change, delta ->
                    change.consume()
                    val (dx, dy) = delta

                    // Lock axis once movement is clear
                    if (lockedAxis == Axis.None) {
                        if (abs(dx) > abs(dy) && abs(dx) > thresholdPx) lockedAxis = Axis.Horizontal
                        else if (abs(dy) > thresholdPx) lockedAxis = Axis.Vertical
                    }

                    // Update only the locked axis; clamp to one cell
                    when (lockedAxis) {
                        Axis.Horizontal -> {
                            val next = (dragX.value + dx).coerceIn(-cellPx, cellPx)
                            scope.launch { dragX.snapTo(next) }
                            if (dragY.value != 0f) scope.launch { dragY.snapTo(0f) }
                        }
                        Axis.Vertical -> {
                            val next = (dragY.value + dy).coerceIn(-cellPx, cellPx)
                            scope.launch { dragY.snapTo(next) }
                            if (dragX.value != 0f) scope.launch { dragX.snapTo(0f) }
                        }
                        Axis.None -> Unit
                    }
                }
            }
            .graphicsLayer {
                // Combine programmatic swap offset + user drag offset
                translationX = swapX.value + dragX.value
                translationY = swapY.value + dragY.value

                // Subtle “picked up” feel during drag
                val s = if (lockedAxis != Axis.None) 1.06f else 1f
                scaleX = s; scaleY = s
            }
    ) {
        // Draw the candy face/content
        CandyFace(tile)
    }
}

private enum class Axis { None, Horizontal, Vertical }