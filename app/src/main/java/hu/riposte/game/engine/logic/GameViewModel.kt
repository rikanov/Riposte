package hu.riposte.game.engine.logic

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import hu.riposte.game.engine.data.Coord
import hu.riposte.game.engine.data.GameMode
import hu.riposte.game.engine.data.GameSettings
import hu.riposte.game.engine.data.GameStateSnapshot
import hu.riposte.game.engine.data.GameWaitingFor
import hu.riposte.game.engine.data.MoveData
import hu.riposte.game.engine.data.Piece
import hu.riposte.game.engine.data.PieceState
import hu.riposte.game.engine.data.SoundEvent
import hu.riposte.game.engine.data.SoundType
import hu.riposte.game.engine.data.StartingPlayer
import hu.riposte.game.engine.data.TutorialPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sqrt

/**
 * RIPOSTE - Core Game ViewModel
 * Handles game state, AI steps, and Tournament logic.
 * Follows AI_AGENT.md guidelines for state management.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    // --- INFRASTRUCTURE ---
    val settingsManager = SettingsManager(application)
    val tournamentManager = TournamentManager()
    private var timerJob: Job? = null
    private val undoStack = mutableListOf<GameStateSnapshot>()

    // --- GLOBAL STATE ---
    var isPremiumVersion: Boolean by mutableStateOf(true)
    var isGamePaused by mutableStateOf(false)
    var isInterruptedGame by mutableStateOf(false)
    var settings by mutableStateOf(GameSettings())
    var matchCount: Int = 0

    // --- BOARD & PIECES STATE ---
    val board = mutableStateListOf<Int>()
    val pieces = mutableStateListOf<Piece>()
    var playerCaptured by mutableStateOf(intArrayOf(0, 0, 0))
    var activeHint by mutableStateOf<MoveData?>(null)
    private var afterTouche: Boolean = false

    // --- GAME PHASE & WINNER ---
    var gamePhase by mutableStateOf(GameWaitingFor.SETUP)
    var winner by mutableStateOf<String?>(null)

    // --- TUTORIAL STATE ---
    var isTutorialMode by mutableStateOf(false)
    var tutorialPhase by mutableStateOf(TutorialPhase.NOT_ACTIVE)
    var tutorialMoveCount by mutableIntStateOf(0)

    // --- TOURNAMENT & CLOCK STATE ---
    var isTournamentMode by mutableStateOf(false)
    var tournamentOpponentNameRes: Int? by mutableStateOf(null)
    var tournamentTargetRank: Int? by mutableStateOf(null)
    var tournamentChallengerName: String = "YOU"
    var currentPlayerId: Int = if(matchCount++ % 2 == 1) 1 else 2
    var playerTimeMs by mutableLongStateOf(180_000L)
    var opponentTimeMs by mutableLongStateOf(180_000L)

    // --- STATS & VFX EVENTS ---
    var lastViewedRating: Int? = null
    var lastViewedRank: Int? = null
    var tacticalMultiplier by mutableStateOf(1.0f)
        private set
    var soundEvent by mutableStateOf<SoundEvent?>(null)
        private set

    init {
        resetBoard()
    }

    // --- CORE LOGIC ---

    fun triggerSound(type: SoundType, playerId: Int) {
        soundEvent = SoundEvent(type, playerId)
    }

    /**
     * Increments tactical bonus for skilled moves (Riposte/Remise).
     */
    fun onTacticalMoveExecuted() {
        tacticalMultiplier += 0.1f
    }

    private fun resetBoard() {
        board.clear()
        pieces.clear()
        repeat(35) { board.add(0) }

        // P1 Pieces (AI/Opponent)
        for (i in 0..4) {
            board[i] = 1
            pieces.add(Piece(id = i, owner = 1, pos = Coord.fromIndex(i)))
        }
        // P2 Pieces (Player)
        for (i in 30..34) {
            board[i] = 2
            pieces.add(Piece(id = i, owner = 2, pos = Coord.fromIndex(i)))
        }
        // Initial HotSpot (★)
        board[17] = 4

        playerCaptured = intArrayOf(0, 0, 0)
        activeHint = null
        undoStack.clear()
        soundEvent = null
        timerJob?.cancel()
    }

    fun restartGame() {
        resetBoard()
        tacticalMultiplier = 1.0f

        currentPlayerId = if (isTournamentMode) {
            if (tournamentManager.isDefending) 1 else 2
        } else {
            if(matchCount++ % 2 == 1) 1 else 2
        }

        if (isTournamentMode) {
            playerTimeMs = 180_000L
            opponentTimeMs = 180_000L
            startTimer()
        }

        if (settings.gameMode == GameMode.VS_AI && currentPlayerId == 1) {
            aiStep()
        } else {
            gamePhase = GameWaitingFor.MOVE_PIECE
        }
    }

    fun startNewGame(newSettings: GameSettings, isTournament: Boolean = false) {
        isInterruptedGame = false
        isTournamentMode = isTournament
        settings = newSettings
        winner = null

        if (isTournamentMode) {
            val opponent = tournamentManager.getNextOpponent()
            tournamentOpponentNameRes = opponent.nameRes
            tournamentTargetRank = if (tournamentManager.isDefending) tournamentManager.currentRank else tournamentManager.currentRank - 1
        } else {
            tournamentOpponentNameRes = null
            tournamentTargetRank = null
            playerTimeMs = 180_000L
            opponentTimeMs = 180_000L
        }

        restartGame()
    }

    fun startTutorial() {
        isInterruptedGame = false
        isTournamentMode = false
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

    // --- CHESS CLOCK LOGIC ---

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (true) {
                delay(50)
                val now = System.currentTimeMillis()
                val delta = now - lastTime
                lastTime = now

                if (gamePhase == GameWaitingFor.GAME_OVER) break

                if (!isGamePaused && isTournamentMode && (gamePhase == GameWaitingFor.MOVE_PIECE || gamePhase == GameWaitingFor.AI_MOVE || gamePhase == GameWaitingFor.TAKE_PIECE)) {
                    if (currentPlayerId == 2) {
                        playerTimeMs -= delta
                        if (playerTimeMs <= 0) {
                            playerTimeMs = 0
                            handleTimeout(1)
                        }
                    } else if (currentPlayerId == 1) {
                        opponentTimeMs -= delta
                        if (opponentTimeMs <= 0) {
                            opponentTimeMs = 0
                            handleTimeout(2)
                        }
                    }
                }
            }
        }
    }

    private fun handleTimeout(winnerId: Int) {
        timerJob?.cancel()
        if (winnerId == 1) {
            triggerSound(SoundType.LOSE, 1)
            winner = "Time's Up! Opponent claims the bout."
        } else {
            triggerSound(SoundType.WIN, 2)
            winner = "Time's Up! You claim the bout."
        }
        gamePhase = GameWaitingFor.GAME_OVER
    }

    // --- MOVE EXECUTION ---

    private fun synchronizeMove(fromIdx: Int, toIdx: Int, owner: Int) {
        val hitTouche = board[toIdx] == 4
        board[fromIdx] = 0
        board[toIdx] = owner

        val pieceIdx = pieces.indexOfFirst { it.pos == Coord.fromIndex(fromIdx) && it.state != PieceState.CAPTURED }
        if (pieceIdx != -1) pieces[pieceIdx] = pieces[pieceIdx].copy(pos = Coord.fromIndex(toIdx))

        viewModelScope.launch {
            triggerSound(SoundType.MOVE, owner)
            delay(400L)

            if (hitTouche) {
                gamePhase = GameWaitingFor.TAKE_PIECE
                if (isTutorialMode && (tutorialPhase == TutorialPhase.WAIT_FOR_TOUCH || tutorialPhase == TutorialPhase.SHOW_TOUCHE)) {
                    tutorialPhase = TutorialPhase.SHOW_CAPTURE
                }
            } else {
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
        val targetIndex = MoveLogic.calculateTargetIndex(board, index, offset)

        if (targetIndex != index) {
            if (settings.riposteAllowed || !afterTouche || board[targetIndex] != 4) {
                // Tactical Bonus Check (Riposte)
                if (afterTouche && board[targetIndex] == 4) {
                    onTacticalMoveExecuted()
                }

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

                    if (isTournamentMode) playerTimeMs += 60_000L

                    pieces[pIdx] = pieces[pIdx].copy(state = PieceState.BEING_CAPTURED)
                    board[index] = 4
                    afterTouche = true

                    playerCaptured[currentPlayerId]++
                    gamePhase = GameWaitingFor.ANIMATION
                    delay(300)
                    pieces[pIdx] = pieces[pIdx].copy(state = PieceState.CAPTURED, pos = Coord.Invalid)

                    if (playerCaptured[currentPlayerId] >= 2) {
                        timerJob?.cancel()
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

    // --- AI & ENGINE INTEGRATION ---

    fun requestHint() {
        if (gamePhase != GameWaitingFor.MOVE_PIECE) return
        viewModelScope.launch {
            val move = withContext(Dispatchers.Default) {
                getBestStepNative(board.toIntArray(), currentPlayerId, 7, settings.riposteAllowed)
            }
            if (move.from != -1) activeHint = move
        }
    }

    private fun getRandomOpeningMove(currentBoard: List<Int>): MoveData {
        val validMoves = mutableListOf<MoveData>()
        val offsets = intArrayOf(1, 4, 5, 6, -1, -4, -5, -6)
        val hotSpot = currentBoard.indexOf(4)

        for (i in 0..4) {
            if (currentBoard[i] == 1) {
                for (offset in offsets) {
                    val target = MoveLogic.calculateTargetIndex(currentBoard, i, offset)
                    if (target != i && currentBoard[target] == 0) {
                        validMoves.add(MoveData(i, target, hotSpot))
                    }
                }
            }
        }
        return if (validMoves.isNotEmpty()) {
            validMoves.random()
        } else {
            getBestStepNative(currentBoard.toIntArray(), 1, settings.difficulty, settings.riposteAllowed)
        }
    }

    private fun aiStep() {
        currentPlayerId = 1
        gamePhase = GameWaitingFor.AI_MOVE

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val move = withContext(Dispatchers.Default) {
                val isFirstMove = (0..4).all { board[it] == 1 }

                if(isFirstMove) {
                    getRandomOpeningMove(board.toList())
                }
                else if (settings.difficulty >= 10) {
                    getBestStepNativeTT(board.toIntArray(), 1, settings.difficulty, settings.riposteAllowed)
                } else {
                    getBestStepNative(board.toIntArray(), 1, settings.difficulty, settings.riposteAllowed)
                }
            }
            val thinkTime = System.currentTimeMillis() - startTime
            if (thinkTime < 600) delay(600 - thinkTime)

            applyAiMove(move)

            if (playerCaptured[1] >= 2) {
                timerJob?.cancel()
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
            delay(300L)
            capturedIdx = pieces.indexOfFirst { it.pos == Coord.fromIndex(move.hotSpot) && it.state != PieceState.CAPTURED }
            if (capturedIdx != -1) pieces[capturedIdx] = pieces[capturedIdx].copy(state = PieceState.BEING_CAPTURED)

            if (isTournamentMode) opponentTimeMs += 60_000L
        }

        board[move.from] = 0
        board[move.to] = 1
        board[move.hotSpot] = 4

        if (isCapture && capturedIdx != -1) {
            triggerSound(SoundType.TOUCHE, 1)
            delay(600)
            pieces[capturedIdx] = pieces[capturedIdx].copy(state = PieceState.CAPTURED, pos = Coord.Invalid)
        }
    }

    // --- UTILS ---

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
        }
    }

    // --- PERSISTENCE & TOURNAMENT FLOW ---

    fun processTournamentMatchEnd(isWin: Boolean): String {
        val rank = tournamentTargetRank ?: 20
        val baseScore = ((20 - rank) * (20 - rank)).toFloat()

        val finalScore = if (!isWin) {
            (baseScore * 0.3f).toInt()
        } else {
            val timeDiffSec = (playerTimeMs - opponentTimeMs) / 1000f
            val timeBonus = if (timeDiffSec > 0) (timeDiffSec / 60f).coerceAtMost(1.0f) * 0.2f else 0.0f
            (baseScore * (tacticalMultiplier + timeBonus)).toInt()
        }

        return tournamentManager.onMatchFinished(isWin, finalScore)
    }

    suspend fun saveTournamentStateToDisk() {
        val currentSettings = settingsManager.settingsFlow.first()
        val boardStr = board.joinToString(",")
        val piecesStr = pieces.joinToString("|") { p ->
            "${p.id}:${p.owner}:${p.pos.x}:${p.pos.y}:${p.state.name}"
        }

        settingsManager.updateSettings(
            currentSettings.copy(
                hasSavedTournamentMatch = true,
                savedTourneyPlayerScore = playerCaptured[2],
                savedTourneyOppScore = playerCaptured[1],
                savedTourneyPlayerTime = playerTimeMs,
                savedTourneyOppTime = opponentTimeMs,
                savedTourneyBoardStr = boardStr,
                savedTourneyPiecesStr = piecesStr,
                savedTourneyCurrentPlayer = currentPlayerId,
                savedTourneyAfterTouche = afterTouche
            )
        )
    }

    fun loadTournamentState(savedSettings: AppSettings) {
        isTournamentMode = true
        isInterruptedGame = true
        playerCaptured[2] = savedSettings.savedTourneyPlayerScore
        playerCaptured[1] = savedSettings.savedTourneyOppScore
        playerTimeMs = savedSettings.savedTourneyPlayerTime
        opponentTimeMs = savedSettings.savedTourneyOppTime
        currentPlayerId = savedSettings.savedTourneyCurrentPlayer
        afterTouche = savedSettings.savedTourneyAfterTouche

        val opponent = tournamentManager.getNextOpponent()
        tournamentOpponentNameRes = opponent.nameRes
        tournamentTargetRank = if (tournamentManager.isDefending) tournamentManager.currentRank else tournamentManager.currentRank - 1

        board.clear()
        savedSettings.savedTourneyBoardStr.split(",").forEach {
            if(it.isNotEmpty()) board.add(it.toInt())
        }

        pieces.clear()
        savedSettings.savedTourneyPiecesStr.split("|").forEach { pieceStr ->
            if (pieceStr.isNotEmpty()) {
                val parts = pieceStr.split(":")
                pieces.add(
                    Piece(id = parts[0].toInt(), owner = parts[1].toInt(), pos = Coord(parts[2].toInt(), parts[3].toInt()), state = PieceState.valueOf(parts[4]))
                )
            }
        }

        winner = null
        undoStack.clear()
        startTimer()

        if (currentPlayerId == 1) {
            aiStep()
        } else {
            gamePhase = GameWaitingFor.MOVE_PIECE
        }
    }

    fun forfeitTournamentMatch() {
        processTournamentMatchEnd(isWin = false)
        isTournamentMode = false
        isInterruptedGame = false

        viewModelScope.launch {
            val currentSettings = settingsManager.settingsFlow.first()
            settingsManager.updateSettings(
                currentSettings.copy(
                    hasSavedTournamentMatch = false,
                    tournamentRank = tournamentManager.currentRank,
                    tournamentHighest = tournamentManager.highestRank,
                    tournamentDefending = tournamentManager.isDefending,
                    tournamentMatchHistory = tournamentManager.matchHistoryList.joinToString(",")
                )
            )
        }
    }

    private external fun getBestStepNative(b: IntArray, p: Int, d: Int, r: Boolean): MoveData
    private external fun getBestStepNativeTT(b: IntArray, p: Int, d: Int, r: Boolean): MoveData

    companion object {
        init {
            System.loadLibrary("riposte")
        }
    }
}
