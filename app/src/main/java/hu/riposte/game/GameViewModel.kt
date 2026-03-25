package hu.riposte.game

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.PI
import kotlin.math.sqrt
import android.util.Log

class GameViewModel : ViewModel() {
    val board = mutableStateListOf<Int>()
    var playerCaptured by mutableStateOf(intArrayOf(0,0,0) )
    var winner by mutableStateOf<String?>(null)
    var gamePhase by mutableStateOf(GameWaitingFor.SETUP)
    var settings by mutableStateOf(GameSettings())

    private val history = mutableListOf<GameState>()
    private var pendingSnapshot: GameState? = null
    private var currentPlayerId: Int = 2
    private var afterTouche: Boolean = false
    private var matchCount: Int = 0

    init { resetBoard() }
    fun init() { resetBoard() }

    private fun resetBoard() {

        board.clear()
        repeat(35) { board.add(0) }
        for (i in 0..4) board[i] = 1
        for (i in 30..34) board[i] = 2

        board[17] = 4

        playerCaptured.fill(0)
        history.clear()
    }

    fun restartGame() {
        resetBoard()
        val firstPhase = when(settings.startingPlayer) {
            StartingPlayer.PLAYER -> GameWaitingFor.MOVE_PIECE
            StartingPlayer.AI -> GameWaitingFor.AI_MOVE
            StartingPlayer.ALTERNATING -> if ( ++matchCount % 2 == 1) GameWaitingFor.AI_MOVE else GameWaitingFor.MOVE_PIECE
        }

        gamePhase = firstPhase
        if (firstPhase == GameWaitingFor.AI_MOVE) aiStep()
    }
    fun startNewGame(newSettings: GameSettings) {
        settings = newSettings
        winner = null
        restartGame()
    }

    fun undo() {

        if (gamePhase == GameWaitingFor.MOVE_PIECE && history.isNotEmpty()) {
            val last = history.removeAt(history.size - 1)
            board.clear()
            board.addAll(last.snapshot)
            playerCaptured = last.captured
            afterTouche = last.afterTouche
        }
    }

    fun handleSwipe(index: Int, dragAmount: Offset) {
        if (gamePhase != GameWaitingFor.MOVE_PIECE || board[index] != currentPlayerId) return
        val x = dragAmount.x
        val y = dragAmount.y
        if (sqrt(x * x + y * y) < 30f) return
        val angle = atan2(y, x) * 180 / PI
        val offset = getOffsetFromAngle(angle) ?: return

        pendingSnapshot = GameState(board.toList(), playerCaptured, afterTouche)
        val targetIndex = GameLogic.calculateTargetIndex(board, index, offset)

        if (targetIndex != index && (settings.riposteAllowed || ! afterTouche || board[targetIndex] != 4)) {
            afterTouche = false
            executeMove(index, targetIndex)
        }
    }

    private fun executeMove(from: Int, to: Int) {

        pendingSnapshot?.let { history.add(it) }

        val hitTouche = board[to] == 4

        board[from] = 0

        board[to] = currentPlayerId

        if (hitTouche) {

            gamePhase = GameWaitingFor.TAKE_PIECE

        } else {

            if (settings.gameMode == GameMode.VS_AI) {

                aiStep()

            } else {

                currentPlayerId = if (currentPlayerId == 1) 2 else 1

                gamePhase = GameWaitingFor.MOVE_PIECE

            }

        }

    }

    fun onCellClick(index: Int) {

        if (gamePhase == GameWaitingFor.TAKE_PIECE && board[index] == 3 - currentPlayerId) {

            board[index] = 4

            afterTouche = true

            if ( ++playerCaptured[currentPlayerId] >= 2) {

                winner = "Player won"; gamePhase = GameWaitingFor.GAME_OVER; return

            }





            if (settings.gameMode == GameMode.VS_AI) {

                aiStep()

            } else {

                currentPlayerId = if (currentPlayerId == 1) 2 else 1

                gamePhase = GameWaitingFor.MOVE_PIECE

            }

        }

    }

    private fun aiStep() {

        gamePhase = GameWaitingFor.AI_MOVE

        viewModelScope.launch {

            val move = withContext(Dispatchers.Default) {

                getBestStepNative(board.toIntArray(), 1, settings.difficulty, settings.riposteAllowed)

            }

            applyAiMove(move)

            if (playerCaptured[1] >= 2) {

                winner = "AI won"; gamePhase = GameWaitingFor.GAME_OVER

            } else {

                gamePhase = GameWaitingFor.MOVE_PIECE

            }

        }

    }

    private fun applyAiMove(move: MoveData) {

        if (board[move.to] == 4) {

            playerCaptured[1]++

            afterTouche = true;

        }

        board[move.from] = 0

        board[move.to] = 1

        board[move.hotSpot] = 4

    }

    private fun getOffsetFromAngle(angle: Double): Int? {

        return when {

            angle >= -22.5 && angle < 22.5 -> 1

            angle >= 22.5 && angle < 67.5 -> 6

            angle >= 67.5 && angle < 112.5 -> 5

            angle >= 112.5 && angle < 157.5 -> 4

            angle >= 157.5 || angle < -157.5 -> -1

            angle >= -157.5 && angle < -112.5 -> -6

            angle >= -112.5 && angle < -67.5 -> -5

            angle >= -67.5 && angle < -22.5 -> -4

            else -> null

        }

    }

    private external fun getBestStepNative(b: IntArray, p: Int, d: Int, r: Boolean): MoveData

    companion object {

        init {

            try { System.loadLibrary("riposte") }

            catch (e: Exception) { Log.e("JNI", "Load failed: ${e.message}") }

        }

    }

}