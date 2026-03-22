package hu.riposte.game

import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.PI
import kotlin.math.sqrt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class GameViewModel : ViewModel() {
    init {
        try {
            System.loadLibrary("riposte")
            println("Native library loaded successfully!")
        } catch (e: Exception) {
            println("ERROR: Could not load native library: ${e.message}")
        }
    }
    val board = mutableStateListOf<Int>().apply {
        repeat(35) { add(0) }
        for (i in 0..4) this[i] = 1
        for (i in 30..34) this[i] = 2

        this[17] = 4
    }
    fun resetGame() {
        board.clear()
        repeat(35) { board.add(0) }
        for (i in 0..4) board[i] = 1
        for (i in 30..34) board[i] = 2
        board[17] = 4

        playerCaptured = 0
        aiCaptured = 0
        winner = null
        gamePhase = GameWaitingFor.MOVE_PIECE
    }

    var playerCaptured = 0
        private set
    var aiCaptured = 0
        private set
    var winner by mutableStateOf<String?>(null)
        private set
    enum class GameWaitingFor{
        AI_MOVE,
        MOVE_PIECE,
        TAKE_PIECE,
        GAME_OVER
    }
    private fun endGame(message: String) {
        winner = message
        gamePhase = GameWaitingFor.GAME_OVER
    }
    var gamePhase = GameWaitingFor.MOVE_PIECE
    fun handleSwipe(index: Int, dragAmount: Offset) {
        if ( gamePhase != GameWaitingFor.MOVE_PIECE || board[index] != 2) return

        val x = dragAmount.x
        val y = dragAmount.y

        val distance = sqrt(x * x + y * y)
        if (distance < 50f) return

        val angle = atan2(y, x) * 180 / PI

        val offset = when {
            angle >= -22.5 && angle < 22.5 -> 1
            angle >= 22.5 && angle < 67.5 -> 6
            angle >= 67.5 && angle < 112.5 -> 5
            angle >= 112.5 && angle < 157.5 -> 4
            angle >= 157.5 || angle < -157.5 -> -1
            angle >= -157.5 && angle < -112.5 -> -6
            angle >= -112.5 && angle < -67.5 -> -5
            angle >= -67.5 && angle < -22.5 -> -4
            else -> return
        }

        movePiece(index, offset)
    }
    private fun movePiece(index: Int, offset: Int) {
        var currentIndex = index
        var nextIndex = getNextIndex(currentIndex, offset)

        while (nextIndex != null && board[nextIndex] % 4 == 0) {
            currentIndex = nextIndex
            nextIndex = getNextIndex(currentIndex, offset)
        }

        val hitHotSpot = board[currentIndex] == 4
        board[index] = 0
        board[currentIndex] = 2
        if( hitHotSpot ) {
            gamePhase = GameWaitingFor.TAKE_PIECE
        }
        else
        {
            aiStep()
        }
    }

    fun onCellClick(index: Int) {
        if (gamePhase == GameWaitingFor.TAKE_PIECE) {
            if (board[index] == 1) {
                board[index] = 4
                playerCaptured++
                aiStep()
            }
        }
    }
    private fun getNextIndex(current: Int, offset: Int): Int? {
        val next = current + offset

        if (next !in 0..34) return null

        val currentCol = current % 5
        val nextCol = next % 5

        if (abs(currentCol - nextCol) > 1) return null

        return next
    }

    private external fun getBestStepNative(
        board: IntArray,
        playerId: Int,
        depth: Int,
        isRiposteAllowed: Boolean
    ): MoveData
    fun aiStep() {
        if (playerCaptured >= 2) {
            endGame("Player won")
            return
        }
        if(aiCaptured >= 2) {
            return
        }
        gamePhase = GameWaitingFor.AI_MOVE
        viewModelScope.launch {
            val move = withContext(Dispatchers.Default) {
                getBestStepNative(board.toIntArray(), playerId = 1, depth = 5, isRiposteAllowed = true)
            }
            applyAiMove(move)
            if( aiCaptured >= 2 ) {
                endGame("AI won")
            }
            else {
                gamePhase = GameWaitingFor.MOVE_PIECE
            }
        }
    }
    private fun applyAiMove(move: MoveData) {
        if (board[move.to] == 4) {
            aiCaptured++
        }
        board[move.from] = 0
        board[move.to] = 1
        board[move.hotSpot] = 4

    }
}