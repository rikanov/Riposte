package hu.riposte.game

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

class GameViewModel : ViewModel() {
    val board = mutableStateListOf<Int>()
    val pieces = mutableStateListOf<Piece>()
    var isInterruptedGame by mutableStateOf(false)
    var playerCaptured by mutableStateOf(intArrayOf(0, 0, 0))
    var winner by mutableStateOf<String?>(null)
    var gamePhase by mutableStateOf(GameWaitingFor.SETUP)

    var isTutorialMode by mutableStateOf(false)
    var tutorialPhase by mutableStateOf(TutorialPhase.NOT_ACTIVE)
    var tutorialMoveCount by mutableIntStateOf(0)

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

    var activeHint by mutableStateOf<MoveData?>(null)

    // --- VÍVÓ SZÓTÁR VÁLTOZÓK ---
    var combatTextEvent by mutableStateOf<CombatTextEvent?>(null)
        private set
    private var lastScoringPlayerId: Int = 0
    private var lastStrikeDistance: Int = 0
    private var lastStrikeDiagonal: Boolean = false

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
        board[17] = 4
        playerCaptured = intArrayOf(0, 0, 0)
        activeHint = null
        undoStack.clear()
        lastScoringPlayerId = 0
        soundEvent = null
        combatTextEvent = null
    }
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
    fun startTutorial() {
        isInterruptedGame = false
        isTutorialMode = true
        tutorialPhase = TutorialPhase.FREE_PLAY
        tutorialMoveCount = 0
        settings = GameSettings(
            gameMode = GameMode.VS_AI,
            startingPlayer = StartingPlayer.PLAYER,
            difficulty = 4,
            riposteAllowed = true
        )
        restartGame()
    }

    private fun determineCombatText(playerId: Int, distance: Int, isDiagonal: Boolean): CombatTextType {
        return when {
            lastScoringPlayerId == 3 - playerId -> CombatTextType.RIPOSTE
            lastScoringPlayerId == playerId -> CombatTextType.REMISE
            distance >= 3 -> CombatTextType.LUNGE
            isDiagonal -> CombatTextType.FLECHE
            else -> CombatTextType.TOUCHE
        }
    }

    private fun synchronizeMove(fromIdx: Int, toIdx: Int, owner: Int) {
        val hitTouche = board[toIdx] == 4
        board[fromIdx] = 0
        board[toIdx] = owner

        val pieceIdx = pieces.indexOfFirst { it.pos == Coord.fromIndex(fromIdx) && it.state != PieceState.CAPTURED }
        if (pieceIdx != -1) pieces[pieceIdx] = pieces[pieceIdx].copy(pos = Coord.fromIndex(toIdx))

        viewModelScope.launch {
            triggerSound(SoundType.MOVE, owner)
            delay(400L) // Megvárjuk, amíg a bábu a helyére csúszik

            if (hitTouche) {
                val textType = determineCombatText(owner, lastStrikeDistance, lastStrikeDiagonal)
                combatTextEvent = CombatTextEvent(textType, Coord.fromIndex(toIdx))
                lastScoringPlayerId = owner
                // ----------------------------------------------

                gamePhase = GameWaitingFor.TAKE_PIECE
                if (isTutorialMode && (tutorialPhase == TutorialPhase.WAIT_FOR_TOUCH || tutorialPhase == TutorialPhase.SHOW_TOUCHE)) {
                    tutorialPhase = TutorialPhase.SHOW_CAPTURE
                }
            } else {
                lastScoringPlayerId = 0 // A kombó megszakad
                finalizeTurn()
            }
        }
    }
    fun handleSwipe(index: Int, dragAmount: Offset) {
        activeHint = null
        if (gamePhase != GameWaitingFor.MOVE_PIECE || board[index] != currentPlayerId) return

        val x = dragAmount.x
        val y = dragAmount.y

        if (sqrt(x * x + y * y) < 30f) return

        val angle = atan2(y, x) * 180 / PI
        val offset = getOffsetFromAngle(angle) ?: return
        val targetIndex = GameLogic.calculateTargetIndex(board, index, offset)

        // Távolság és irány mentése az esetleges ütéshez
        val sx = index % 5
        val sy = index / 5
        val tx = targetIndex % 5
        val ty = targetIndex / 5
        lastStrikeDistance = max(abs(sx - tx), abs(sy - ty))
        lastStrikeDiagonal = abs(sx - tx) > 0 && abs(sy - ty) > 0

        if (targetIndex != index) {
            if (settings.riposteAllowed || !afterTouche || board[targetIndex] != 4) {
                saveState()
                gamePhase = GameWaitingFor.ANIMATION
                afterTouche = false
                synchronizeMove(index, targetIndex, currentPlayerId)
            }
        }
    }

    fun onCellClick(index: Int) {
        activeHint = null
        if (gamePhase == GameWaitingFor.TAKE_PIECE && board[index] == 3 - currentPlayerId) {
            val pIdx = pieces.indexOfFirst { it.pos == Coord.fromIndex(index) && it.state != PieceState.CAPTURED }
            if (pIdx != -1) {
                viewModelScope.launch {
                    triggerSound(SoundType.TOUCHE, currentPlayerId)

                    pieces[pIdx] = pieces[pIdx].copy(state = PieceState.BEING_CAPTURED)
                    board[index] = 4
                    afterTouche = true

                    // (A Combat Text logikát innen kivettük)

                    playerCaptured[currentPlayerId]++
                    gamePhase = GameWaitingFor.ANIMATION
                    delay(300)
                    pieces[pIdx] = pieces[pIdx].copy(state = PieceState.CAPTURED, pos = Coord.Invalid)

                    if (playerCaptured[currentPlayerId] >= 2) {
                        triggerSound(SoundType.WIN, currentPlayerId)
                        winner = "You won!"; gamePhase = GameWaitingFor.GAME_OVER
                    } else {
                        if (isTutorialMode && (tutorialPhase == TutorialPhase.SHOW_CAPTURE || tutorialPhase == TutorialPhase.WAIT_FOR_TOUCH)) {
                            tutorialPhase = TutorialPhase.SHOW_WIN_COND
                        } else {
                            finalizeTurn()
                        }
                    }
                }
            }
        }
    }

    fun resumeTutorialTurn() { finalizeTurn() }

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

    fun requestHint() {
        if (gamePhase != GameWaitingFor.MOVE_PIECE) return

        viewModelScope.launch {
            val move = withContext(Dispatchers.Default) {
                getBestStepNative(board.toIntArray(), currentPlayerId, 7, settings.riposteAllowed)
            }
            if (move.from != -1) {
                activeHint = move
            }
        }
    }

    private fun aiStep() {
        gamePhase = GameWaitingFor.AI_MOVE
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val move = withContext(Dispatchers.Default) {
                if (settings.difficulty >= 10) {
                    getBestStepNativeTT(board.toIntArray(), 1, settings.difficulty, settings.riposteAllowed)
                } else {
                    getBestStepNative(board.toIntArray(), 1, settings.difficulty, settings.riposteAllowed)
                }
            }
            val thinkTime = System.currentTimeMillis() - startTime
            if (thinkTime < 600) delay(600 - thinkTime)

            applyAiMove(move)

            if (playerCaptured[1] >= 2) {
                triggerSound(SoundType.LOSE, 1)
                winner = "AI won :("; gamePhase = GameWaitingFor.GAME_OVER
            } else {
                gamePhase = GameWaitingFor.MOVE_PIECE
                currentPlayerId = 2

                if (isTutorialMode && tutorialPhase == TutorialPhase.FREE_PLAY) {
                    tutorialMoveCount++
                    if (tutorialMoveCount == 2) tutorialPhase = TutorialPhase.SHOW_TOUCHE
                }
            }
        }
    }

    private suspend fun applyAiMove(move: MoveData) {
        if (board[move.to] == 4) { playerCaptured[1]++; afterTouche = true }

        val pIdx = pieces.indexOfFirst { it.pos == Coord.fromIndex(move.from) && it.state != PieceState.CAPTURED }
        if (pIdx != -1) pieces[pIdx] = pieces[pIdx].copy(pos = Coord.fromIndex(move.to))

        triggerSound(SoundType.MOVE, 1)
        delay(400L)

        val isCapture = move.hotSpot != move.to && board[move.hotSpot] != 4
        var capturedIdx = -1

        if (isCapture) {
            // --- 3. VÁLTOZÁS: AI SZÖVEG KIVÁLTÁSA ---
            val sx = move.from % 5
            val sy = move.from / 5
            val tx = move.to % 5
            val ty = move.to / 5
            val dist = max(abs(sx - tx), abs(sy - ty))
            val diag = abs(sx - tx) > 0 && abs(sy - ty) > 0

            val textType = determineCombatText(1, dist, diag)
            combatTextEvent = CombatTextEvent(textType, Coord.fromIndex(move.to)) // Az AI bábuja felett pattan fel
            lastScoringPlayerId = 1
            // ----------------------------------------

            // Pici szünet az AI-nál is, hogy a szöveg elváljon az ütéstől!
            delay(300L)

            capturedIdx = pieces.indexOfFirst { it.pos == Coord.fromIndex(move.hotSpot) && it.state != PieceState.CAPTURED }
            if (capturedIdx != -1) pieces[capturedIdx] = pieces[capturedIdx].copy(state = PieceState.BEING_CAPTURED)
        } else {
            lastScoringPlayerId = 0 // Kombó megszakad
        }

        board[move.from] = 0
        board[move.to] = 1
        board[move.hotSpot] = 4

        if (isCapture && capturedIdx != -1) {
            // --- AI RÁZKÓDÁS KIVÁLTÁSA ---
            triggerSound(SoundType.TOUCHE, 1)
            delay(600)
            pieces[capturedIdx] = pieces[capturedIdx].copy(state = PieceState.CAPTURED, pos = Coord.Invalid)
        }
    }

    fun getOffsetFromAngle(angle: Double): Int? {
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
            board.clear()
            board.addAll(lastState.board.toTypedArray())
            pieces.clear()
            pieces.addAll(lastState.pieces)
            playerCaptured = lastState.playerCaptured
            currentPlayerId = lastState.currentPlayerId
            afterTouche = lastState.afterTouche
            gamePhase = lastState.gamePhase
            activeHint = null
            lastScoringPlayerId = 0
        }
    }

    fun startNewGame(newSettings: GameSettings) {
        isInterruptedGame = false
        settings = newSettings
        winner = null
        restartGame()
    }

    private external fun getBestStepNative(b: IntArray, p: Int, d: Int, r: Boolean): MoveData
    private external fun getBestStepNativeTT(b: IntArray, p: Int, d: Int, r: Boolean): MoveData
    companion object { init { System.loadLibrary("riposte") } }
}