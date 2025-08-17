package com.stymsaw.cc.game.presentation

/**
 * User intents coming from the UI.
 * Your GameViewModel can expose a `fun onEvent(event: UiEvent)` to handle these.
 */
sealed interface UiEvent {
    data class SwapRequested(val fromIndex: Int, val toIndex: Int) : UiEvent
    data object NewGame : UiEvent

    // Optional extras you may implement later:
    data object HintRequested : UiEvent
    data object ReshuffleRequested : UiEvent
    data class CellTapped(val index: Int) : UiEvent
}