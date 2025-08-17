package com.stymsaw.cc.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stymsaw.cc.game.domain.Board
import com.stymsaw.cc.game.domain.Index
import com.stymsaw.cc.game.domain.Shuffler
import com.stymsaw.cc.game.domain.Tile
import com.stymsaw.cc.game.domain.TileType
import com.stymsaw.cc.game.engine.Effect
import com.stymsaw.cc.game.engine.GameReducer
import com.stymsaw.cc.game.engine.GameRules
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

class GameViewModel(
    private val seedProvider: () -> Int = { 42 }, // deterministic by default for reproducibility
    private val width: Int = GameRules.DEFAULT_WIDTH,
    private val height: Int = GameRules.DEFAULT_HEIGHT
) : ViewModel() {

    // --- Domain state ---
    private val rand = Random(seedProvider())
    private val board = Board(width, height, rand)

    // --- Presentation state ---
    private val _ui = MutableStateFlow(
        GameUiState(
            width = width,
            height = height,
            tiles = emptyList(),
            score = 0,
            isProcessing = false,
            clearing = emptySet(),
            swapping = null
        )
    )
    val ui: StateFlow<GameUiState> = _ui

    // One-shot effects (haptics, SFX, toasts)
    private val _effects = MutableSharedFlow<Effect>(
        replay = 0, extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<Effect> = _effects

    init {
        newGame()
    }

    // --- Public API ---

    fun onEvent(event: UiEvent) {
        when (event) {
            is UiEvent.SwapRequested -> {
                val a = event.fromIndex
                val b = event.toIndex
                trySwapByIndex(a, b)
            }
            UiEvent.NewGame -> newGame()
            UiEvent.HintRequested -> { /* implement later */ }
            UiEvent.ReshuffleRequested -> reshuffleIfNeeded(force = true)
            is UiEvent.CellTapped -> { /* optional: tap-to-select flow */ }
        }
    }

    fun newGame() {
        viewModelScope.launch {
            _ui.update { it.copy(isProcessing = true, score = 0, clearing = emptySet(), swapping = null) }

            board.fillNewBoard(noImmediateMatches = true)
            _ui.update { it.copy(tiles = toUiTiles(board.tilesSnapshot()), isProcessing = false) }

            // Optional: ensure at least one legal move exists, reshuffle if not
            reshuffleIfNeeded(force = false)
        }
    }

    // --- Core interactions ---

    private fun trySwapByIndex(aIdx: Int, bIdx: Int) {
        if (_ui.value.isProcessing) return
        if (aIdx == bIdx) return

        val ax = aIdx % width
        val ay = aIdx / width
        val bx = bIdx % width
        val by = bIdx / width
        if (!board.areAdjacent(ax, ay, bx, by)) return

        viewModelScope.launch {
            // mark swapping for swap animation
            _ui.update { it.copy(isProcessing = true, swapping = aIdx to bIdx) }

            // Optimistic swap (UI will animate)
            board.swap(ax, ay, bx, by)
            _ui.update { it.copy(tiles = toUiTiles(board.tilesSnapshot())) }

            // allow swap animation to show
            delay(140L)

            // Step once to see if the swap yielded a match
            val step = GameReducer.step(board)
            if (step.cleared.isEmpty()) {
                // Revert swap + bounce back animation (swap pair reversed)
                board.swap(ax, ay, bx, by)
                _ui.update {
                    it.copy(
                        swapping = bIdx to aIdx,
                        tiles = toUiTiles(board.tilesSnapshot())
                    )
                }
                delay(140L)
                _ui.update { it.copy(swapping = null, isProcessing = false) }
                _effects.tryEmit(Effect.InvalidSwap)
                return@launch
            }

            // Valid swap; show clearing state for animation
            _ui.update { it.copy(swapping = null, clearing = step.cleared) }

            // Pace the animations using rules
            delay(GameRules.CLEAR_DELAY_MS)
            // After reducer step we already cleared/dropped/refilled once; animate intermediate frames
            _ui.update { it.copy(tiles = toUiTiles(board.tilesSnapshot())) }
            delay(GameRules.POST_CLEAR_PAUSE_MS)

            // Continue cascades until stable
            resolveCascades(initialCleared = step.cleared.size)
        }
    }

    private fun resolveCascades(initialCleared: Int = 0) {
        viewModelScope.launch {
            var cascadeIndex = if (initialCleared > 0) 0 else -1 // initial step counted as first cascade
            var gainedScore = 0
            var clearedCount = initialCleared

            if (clearedCount > 0) {
                gainedScore += GameRules.scoreFor(clearedCount, cascadeIndex.coerceAtLeast(0))
                _effects.tryEmit(Effect.MatchCleared(clearedCount))
            }

            while (true) {
                val step = GameReducer.step(board)
                if (step.cleared.isEmpty()) break
                cascadeIndex++
                clearedCount = step.cleared.size
                gainedScore += GameRules.scoreFor(clearedCount, cascadeIndex)
                _effects.tryEmit(Effect.MatchCleared(clearedCount))

                // Drive animations: show which cells are clearing
                _ui.update { it.copy(clearing = step.cleared) }
                delay(GameRules.CLEAR_DELAY_MS)

                // Reflect new board state (after clear/drop/refill in reducer)
                _ui.update { it.copy(tiles = toUiTiles(board.tilesSnapshot())) }
                delay(GameRules.DROP_PAUSE_MS)
            }

            _ui.update {
                it.copy(
                    isProcessing = false,
                    clearing = emptySet(),
                    tiles = toUiTiles(board.tilesSnapshot()),
                    score = it.score + gainedScore
                )
            }

            // If no legal moves after cascade, reshuffle
            reshuffleIfNeeded(force = false)
        }
    }

    // --- Utilities ---

    private fun reshuffleIfNeeded(force: Boolean) {
        viewModelScope.launch {
            val snapshot = board.tilesSnapshot()
            val hasMove = Shuffler.hasAnyLegalMove(width, height, snapshot)
            if (!hasMove || force) {
                val newTiles = Shuffler.reshuffle(width, height, snapshot, rand)
                // Write reshuffled tiles back into the board
                for (i in newTiles.indices) {
                    val x = i % width
                    val y = i / width
                    board.set(x, y, newTiles[i])
                }
                _ui.update { it.copy(tiles = toUiTiles(board.tilesSnapshot())) }
                _effects.tryEmit(Effect.NoMovesReshuffle)
            }
        }
    }

    private fun toUiTiles(tiles: List<Tile?>): List<TileUi> {
        // Keep length == width*height; assign stable synthetic ids for nulls to keep animation stability.
        return tiles.mapIndexed { idx, t ->
            if (t == null) {
                // pick a deterministic placeholder type for empties; UI may render as transparent
                TileUi(
                    id = syntheticEmptyId(idx),
                    type = TileType.Red, // UI can ignore type for empties; or use a separate CellUi model
                    index = idx
                )
            } else {
                TileUi(id = t.id, type = t.type, index = idx)
            }
        }
    }

    private fun syntheticEmptyId(index: Int): Long = -1_000_000L - index.toLong()
}