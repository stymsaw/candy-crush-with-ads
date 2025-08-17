package com.stymsaw.cc.game.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.stymsaw.cc.game.domain.TileType
import com.stymsaw.cc.game.presentation.TileUi

@Composable
fun CandyFace(tile: TileUi) {
    // Skip placeholder empty cells (we used negative ids for empties in the VM)
    if (tile.id < 0L) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val bg = when (tile.type) {
        TileType.Red -> MaterialTheme.colorScheme.errorContainer
        TileType.Blue -> MaterialTheme.colorScheme.primaryContainer
        TileType.Green -> MaterialTheme.colorScheme.tertiaryContainer
        TileType.Yellow -> MaterialTheme.colorScheme.secondaryContainer
        TileType.Purple -> MaterialTheme.colorScheme.inversePrimary
    }

    val emoji = when (tile.type) {
        TileType.Red -> "🍒"
        TileType.Blue -> "🫐"
        TileType.Green -> "🍏"
        TileType.Yellow -> "🍋"
        TileType.Purple -> "🍇"
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}