package com.stymsaw.cc.game.ui.board

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stymsaw.cc.game.presentation.GameUiState
import com.stymsaw.cc.game.ui.animation.swapModifier
import com.stymsaw.cc.game.domain.Index

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoardGrid(
    state: GameUiState,
    onSwap: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(state.width),
        modifier = modifier.fillMaxWidth(),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.Center
    ) {
        itemsIndexed(state.tiles, key = { _, tile -> tile.id }) { idx, tile ->
            val isClearingCell = state.clearing.contains(Index.of(idx % state.width, idx / state.width))
            TileCell(
                tile = tile,
                isClearing = isClearingCell,
                swapInfo = state.swapping,
                onSwap = onSwap,
                boardWidth = state.width,
                modifier = Modifier
                    .size(48.dp)
                    .swapModifier(idx, state.swapping)
            )
        }
    }
}