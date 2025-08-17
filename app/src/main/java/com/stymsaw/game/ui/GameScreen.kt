package com.stymsaw.cc.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stymsaw.cc.game.presentation.GameViewModel
import com.stymsaw.cc.game.presentation.UiEvent
import com.stymsaw.cc.game.ui.board.BoardGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    vm: GameViewModel = viewModel()
) {
    val state = vm.ui.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candy Crush Clone") },
                actions = {
                    Text("Score: ${state.score}", style = MaterialTheme.typography.bodyLarge)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            BoardGrid(
                state = state,
                onSwap = { from, to -> vm.onEvent(UiEvent.SwapRequested(from, to)) }
            )
        }
    }
}