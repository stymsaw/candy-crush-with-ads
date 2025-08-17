package com.stymsaw.cc.game.ui.board

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.stymsaw.cc.game.domain.Index
import com.stymsaw.cc.game.presentation.GameUiState

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
            Modifier
                .size(48.dp)
            TileCell(
                tile = tile,
                isClearing = isClearingCell,
                swapInfo = state.swapping,
                boardWidth = state.width,
                size = 48.dp,
                onSwap = onSwap,
                modifier = Modifier.animateItem(
                    fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                )
            )
        }
    }
}