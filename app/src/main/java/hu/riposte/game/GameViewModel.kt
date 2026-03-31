package hu.riposte.game

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

enum class SoundType { MOVE, TOUCHE, WIN, LOSE }
data class SoundEvent(val type: SoundType, val playerId: Int, val triggerId: Long = System.currentTimeMillis())
class GameViewModel : ViewModel() {
    // A C++ motor számára
    val board = mutableStateListOf<Int>()
    // A UI animáció számára (Stabil ID-kkal)
    val pieces = mutableStateListOf<Piece>()

    var playerCaptured by mutableStateOf(intArrayOf(0, 0, 0))
    var winner by mutableStateOf<String?>(null)
    var gamePhase by mutableStateOf(GameWaitingFor.SETUP)
    var settings by mutableStateOf(GameSettings())
    var matchCount: Int = 0
    var currentPlayerId: Int = when( settings.startingPlayer ) {
        StartingPlayer.AI -> 1
        StartingPlayer.PLAYER -> 2
        StartingPlayer.ALTERNATING -> if(matchCount++ % 2 == 1) 1 else 2
    }
    private var afterTouche: Boolean = false
    var soundEvent by mutableStateOf<SoundEvent?>(null)
        private set

    fun triggerSound(type: SoundType, playerId: Int) {
        soundEvent = SoundEvent(type, playerId)
    }
    private val undoStack = mutableListOf<GameStateSnapshot>()
    init { resetBoard() }

    private fun resetBoard() {
        board.clear()
        pieces.clear()
        repeat(35) { board.add(0) }

        for (i in 0..4) {
            board[i] = 1
            pieces.add(Piece(id = i, owner = 1, pos = Coord.fromIndex(i)))
        }
        for (i in 30..34) {
            board[i] = 2
            pieces.add(Piece(id = i, owner = 2, pos = Coord.fromIndex(i)))
        }
        board[17] = 4 // Arany bábu kezdőhelye
        playerCaptured = intArrayOf(0, 0, 0)
        undoStack.clear()
    }

    // --- SZINKRONIZÁLT MOZGÁS ---
    private fun synchronizeMove(fromIdx: Int, toIdx: Int, owner: Int) {
        val hitTouche = board[toIdx] == 4

        board[fromIdx] = 0
        board[toIdx] = owner

        // 2. Piece lista frissítése (UI/Animáció indulása)
        val pieceIdx = pieces.indexOfFirst { it.pos == Coord.fromIndex(fromIdx) && it.state != PieceState.CAPTURED }
        if (pieceIdx != -1) {
            pieces[pieceIdx] = pieces[pieceIdx].copy(pos = Coord.fromIndex(toIdx))
        }

        // ÚJ: Indítunk egy szálat, ami megvárja a csúszás animációját!
        viewModelScope.launch {
            delay(550) // 550ms: pont azelőtt egy pattanással szólal meg, hogy befejeződne a mozgás
            triggerSound(SoundType.MOVE, owner)

            if (hitTouche) {
                gamePhase = GameWaitingFor.TAKE_PIECE
            } else {
                finalizeTurn()
            }
        }
    }

    fun handleSwipe(index: Int, dragAmount: Offset) {
        if (gamePhase != GameWaitingFor.MOVE_PIECE || board[index] != currentPlayerId) return

        val x = dragAmount.x
        val y = dragAmount.y
        if (sqrt(x * x + y * y) < 30f) return

        val angle = atan2(y, x) * 180 / PI
        val offset = getOffsetFromAngle(angle) ?: return
        val targetIndex = GameLogic.calculateTargetIndex(board, index, offset)

        if (targetIndex != index) {
            if (settings.riposteAllowed || !afterTouche || board[targetIndex] != 4) {
                saveState()
                afterTouche = false
                synchronizeMove(index, targetIndex, currentPlayerId)
            }
        }
    }

    fun onCellClick(index: Int) {
        if (gamePhase == GameWaitingFor.TAKE_PIECE && board[index] == 3 - currentPlayerId) {
            // Keressük meg melyik AI bábu az
            val pIdx =
                pieces.indexOfFirst { it.pos == Coord.fromIndex(index) && it.state != PieceState.CAPTURED }
            if (pIdx != -1) {
                viewModelScope.launch {
                    triggerSound(SoundType.TOUCHE, currentPlayerId)
                    // 1. Fázis: Haláltusa indul
                    pieces[pIdx] = pieces[pIdx].copy(state = PieceState.BEING_CAPTURED)

                    board[index] = 4
                    afterTouche = true
                    playerCaptured[currentPlayerId]++

                    // Várás az animációra
                    delay(800)

                    // 2. Fázis: Végleg eltűnik
                    pieces[pIdx] = pieces[pIdx].copy(
                        state = PieceState.CAPTURED,
                        pos = Coord.Invalid
                    )

                    if (playerCaptured[currentPlayerId] >= 2) {
                        triggerSound(SoundType.WIN, currentPlayerId)
                        winner = "You won!"; gamePhase = GameWaitingFor.GAME_OVER
                    } else {
                        finalizeTurn()
                    }
                }
            }
        }
    }
    private fun finalizeTurn() {
        viewModelScope.launch {

            delay(350)

            if (settings.gameMode == GameMode.VS_AI) {
                aiStep()
            } else {
                currentPlayerId = 3 - currentPlayerId
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
                triggerSound(SoundType.LOSE, 1)
                winner = "AI won :("; gamePhase = GameWaitingFor.GAME_OVER
            } else {
                gamePhase = GameWaitingFor.MOVE_PIECE
                currentPlayerId = 2
            }
        }
    }

    private suspend fun applyAiMove(move: MoveData) {
        if (board[move.to] == 4) {
            playerCaptured[1]++
            afterTouche = true
        }

        val pIdx = pieces.indexOfFirst { it.pos == Coord.fromIndex(move.from) && it.state != PieceState.CAPTURED }
        if (pIdx != -1) pieces[pIdx] = pieces[pIdx].copy(pos = Coord.fromIndex(move.to))

        // ÚJ: Megvárjuk az AI bábu csúszását, és CSAK UTÁNA jön a hang!
        delay(550)
        triggerSound(SoundType.MOVE, 1)

        // 2. Ütés keresése (MIELŐTT felülírjuk a board-ot)
        val isCapture = move.hotSpot != move.to && board[move.hotSpot] != 4
        var capturedIdx = -1

        if (isCapture) {
            capturedIdx = pieces.indexOfFirst { it.pos == Coord.fromIndex(move.hotSpot) && it.state != PieceState.CAPTURED }
            if (capturedIdx != -1) {
                pieces[capturedIdx] = pieces[capturedIdx].copy(state = PieceState.BEING_CAPTURED)
            }
        }

        // 3. C++ Board frissítése
        board[move.from] = 0
        board[move.to] = 1
        board[move.hotSpot] = 4

        // 4. Várakozás és Véglegesítés
        if (isCapture && capturedIdx != -1) {
            delay(200) // Pici szünet a lépés és az ütés között
            triggerSound(SoundType.TOUCHE, 1)
            delay(600) // Haláltusa megvárása
            // FÁZIS 2: Végleg levesszük a tábláról
            pieces[capturedIdx] = pieces[capturedIdx].copy(state = PieceState.CAPTURED, pos = Coord.Invalid)
        }
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

    // --- RESTART ÉS UNDO ---
    fun restartGame() {
        resetBoard()
        currentPlayerId = when( settings.startingPlayer ) {
            StartingPlayer.AI -> 1
            StartingPlayer.PLAYER -> 2
            StartingPlayer.ALTERNATING -> if(matchCount++ % 2 == 1) 1 else 2
        }

        if (settings.gameMode == GameMode.VS_AI && currentPlayerId == 1) {
            aiStep()
        } else {
            gamePhase = GameWaitingFor.MOVE_PIECE
        }
    }
    private fun saveState() {
        undoStack.add(
            GameStateSnapshot(
                board = board.toIntArray(),
                pieces = pieces.toList(),
                playerCaptured = playerCaptured.copyOf(),
                currentPlayerId = currentPlayerId,
                afterTouche = afterTouche,
                gamePhase = gamePhase
            )
        )
    }
    fun undo() {
        if (undoStack.isNotEmpty()) {
            val lastState = undoStack.removeAt(undoStack.size - 1)

            // Visszatöltés
            board.clear()
            board.addAll(lastState.board.toTypedArray())

            pieces.clear()
            pieces.addAll(lastState.pieces)

            playerCaptured = lastState.playerCaptured
            currentPlayerId = lastState.currentPlayerId
            afterTouche = lastState.afterTouche
            gamePhase = lastState.gamePhase
        }
    }

    fun startNewGame(newSettings: GameSettings) {
        settings = newSettings
        winner = null
        restartGame()
    }

    private external fun getBestStepNative(b: IntArray, p: Int, d: Int, r: Boolean): MoveData
    companion object {
        init { System.loadLibrary("riposte") }
    }
}