package hu.riposte.game

import kotlin.math.abs

object GameLogic {
    fun getNextIndex(current: Int, offset: Int): Int? {
        val next = current + offset
        if (next !in 0..34) return null

        val currentCol = current % 5
        val nextCol = next % 5
        if (abs(currentCol - nextCol) > 1) return null

        return next
    }

    fun calculateTargetIndex(board: List<Int>, startIndex: Int, offset: Int): Int {
        var current = startIndex
        var next = getNextIndex(current, offset)

        while (next != null && board[next] % 4 == 0) {
            current = next
            next = getNextIndex(current, offset)
        }
        return current
    }
}