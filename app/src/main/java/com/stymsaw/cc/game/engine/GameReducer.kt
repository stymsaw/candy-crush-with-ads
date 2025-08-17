package com.stymsaw.cc.game.engine

import com.stymsaw.cc.game.domain.Board
import com.stymsaw.cc.game.domain.Index
import com.stymsaw.cc.game.domain.MatchFinder

/**
 * Pure step reducer: given a board, performs one resolve step.
 * A step = find matches → clear → gravity → refill.
 */
data class StepResult(
    val cleared: Set<Index>,
    val isStable: Boolean
)

object GameReducer {
    /**
     * Executes one cascade step.
     * Returns indices cleared this step, and whether board is stable.
     */
    fun step(board: Board): StepResult {
        val matches = MatchFinder.findMatches(board.width, board.height, board.tilesSnapshot())
        if (matches.isEmpty()) {
            return StepResult(emptySet(), isStable = true)
        }

        board.clear(matches)
        board.applyGravity()
        board.refill()

        return StepResult(matches, isStable = false)
    }
}