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
import hu.riposte.game.engine.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
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
 */
data class ThreatRecord(val hs1: Int, val hs2: Int)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val settingsManager = SettingsManager(application)
    val tournamentManager = TournamentManager()
    val soundManager = SoundManager(application)
    private var timerJob: Job? = null
    private val undoStack = mutableListOf<GameStateSnapshot>()
    private val matchThreatLog = mutableListOf<ThreatRecord>()

    private val historyStack = mutableListOf<BoardHash>()
    private var historyBaseIndex = 0
    var separationStepsLeft by mutableIntStateOf(0)

    private var reviewCursorIndex by mutableIntStateOf(-1)
    private var finalGameStateSnapshot: GameStateSnapshot? = null

    var isPremiumVersion: Boolean by mutableStateOf(true)
    var isGamePaused by mutableStateOf(false)
    var isInterruptedGame by mutableStateOf(false)
    var settings by mutableStateOf(GameSettings())
    var matchCount: Int = 0

    val board = mutableStateListOf<Int>()
    val pieces = mutableStateListOf<Piece>()
    var playerCaptured by mutableStateOf(intArrayOf(0, 0, 0))
    var activeHint by mutableStateOf<MoveData?>(null)
    private var afterTouche: Boolean = false

    var gamePhase by mutableStateOf(GameWaitingFor.SETUP)
    var winner by mutableStateOf<String?>(null)
    var isMatchProcessed = false
    var isTutorialMode by mutableStateOf(false)
    var tutorialPhase by mutableStateOf(TutorialPhase.NOT_ACTIVE)
    var tutorialMoveCount by mutableIntStateOf(0)

    var isTournamentMode by mutableStateOf(false)
    var tournamentOpponentNameRes: Int? by mutableStateOf(null)
    var tournamentTargetRank: Int? by mutableStateOf(null)
    var tournamentChallengerName: String = "YOU"
    var currentPlayerId: Int = if(matchCount++ % 2 == 1) 1 else 2
    var playerTimeMs by mutableLongStateOf(180_000L)
    var opponentTimeMs by mutableLongStateOf(180_000L)

    var soundEvent by mutableStateOf<SoundEvent?>(null)
        private set

    init {
        resetBoard()
    }

    fun triggerSound(type: SoundType, playerId: Int) {
        soundEvent = SoundEvent(type, playerId)
    }

    private fun resetBoard() {
        matchThreatLog.clear()
        board.clear()
        pieces.clear()
        isMatchProcessed = false
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
        soundEvent = null
        timerJob?.cancel()

        historyStack.clear()
        historyBaseIndex = 0
        separationStepsLeft = 0
        historyStack.add(calculateBoardHash())

        reviewCursorIndex = -1
        finalGameStateSnapshot = null
    }

    fun restartGame() {
        resetBoard()

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

    private fun calculateBoardHash(): BoardHash {
        var p1 = 0L
        var p2 = 0L
        var bitMask = 1L shl 56
        for (i in 0..34) {
            if (i % 5 == 0) bitMask = bitMask ushr 2
            when (board[i]) {
                1 -> p1 = p1 or bitMask
                2 -> p2 = p2 or bitMask
                4 -> { p1 = p1 or bitMask; p2 = p2 or bitMask }
            }
            bitMask = bitMask ushr 1
        }
        return BoardHash(p1, p2)
    }

    private fun restoreBoardFromHash(hash: BoardHash) {
        var bitMask = 1L shl 56
        for (i in 0..34) {
            if (i % 5 == 0) bitMask = bitMask ushr 2
            val isP1 = (hash.p1 and bitMask) != 0L
            val isP2 = (hash.p2 and bitMask) != 0L
            board[i] = when {
                isP1 && isP2 -> 4
                isP1 -> 1
                isP2 -> 2
                else -> 0
            }
            bitMask = bitMask ushr 1
        }
    }

    private fun recordStateAndCheckDeadlock() {
        val currentHash = calculateBoardHash()
        var count = 0
        for (i in historyBaseIndex until historyStack.size) {
            if (historyStack[i] == currentHash) count++
        }

        if (count >= 2) {
            separationStepsLeft = 2
            historyBaseIndex = historyStack.size
            println("⚔️ HALTE! DEADLOCK DETECTED! Separation phase started! ⚔️")
        } else {
            historyStack.add(currentHash)
        }
    }

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
        gamePhase = GameWaitingFor.GAME_OVER

        if (winnerId == 1) {
            triggerSound(SoundType.LOSE, 1)
            winner = "Time's Up! Opponent claims the bout."
            processTournamentMatchEnd(isWin = false)
        } else {
            triggerSound(SoundType.WIN, 2)
            winner = "Time's Up! You claim the bout."
            processTournamentMatchEnd(isWin = true)
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
            delay(400L)

            if (isTournamentMode) {
                matchThreatLog.add(evaluateThreatLevels(board.toIntArray()))
            }

            if (hitTouche) {
                gamePhase = GameWaitingFor.TAKE_PIECE
                if (isTutorialMode && (tutorialPhase == TutorialPhase.WAIT_FOR_TOUCH || tutorialPhase == TutorialPhase.SHOW_TOUCHE)) {
                    tutorialPhase = TutorialPhase.SHOW_CAPTURE
                }
            } else {
                if (separationStepsLeft > 0) separationStepsLeft-- else recordStateAndCheckDeadlock()
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
            if (separationStepsLeft > 0 && board[targetIndex] == 4) return

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

                    if (isTournamentMode) playerTimeMs += 60_000L

                    pieces[pIdx] = pieces[pIdx].copy(state = PieceState.BEING_CAPTURED)
                    board[index] = 4
                    afterTouche = true
                    historyBaseIndex = historyStack.size

                    playerCaptured[currentPlayerId]++
                    gamePhase = GameWaitingFor.ANIMATION
                    delay(300)
                    pieces[pIdx] = pieces[pIdx].copy(state = PieceState.CAPTURED, pos = Coord.Invalid)

                    if (playerCaptured[currentPlayerId] >= 2) {
                        timerJob?.cancel()
                        triggerSound(SoundType.WIN, currentPlayerId)
                        winner = "You won!"
                        gamePhase = GameWaitingFor.GAME_OVER
                        processTournamentMatchEnd(isWin = true)
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
            val hash = calculateBoardHash()
            val move = withContext(Dispatchers.Default) {
                getBestStepNative(hash.p1, hash.p2, currentPlayerId, 7, settings.riposteAllowed, separationStepsLeft, 10, 10)
            }
            if (move.from != -1) activeHint = move
        }
    }

    private fun getRandomOpeningMove(currentP1: Long, currentP2: Long, offW: Int, defW: Int): MoveData {
        val validMoves = mutableListOf<MoveData>()
        val currentBoard = board.toList()
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
            getBestStepNative(currentP1, currentP2, 1, settings.difficulty, settings.riposteAllowed, separationStepsLeft, offW, defW)
        }
    }

    private fun aiStep() {
        currentPlayerId = 1
        gamePhase = GameWaitingFor.AI_MOVE

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val hash = calculateBoardHash()
            val move = withContext(Dispatchers.Default) {
                val isFirstMove = (0..4).all { board[it] == 1 }

                var currentOffW = 10
                var currentDefW = 10
                if (isTournamentMode && tournamentOpponentNameRes != null) {
                    val opponent = TournamentRoster.opponents.values.find { it.nameRes == tournamentOpponentNameRes }
                    if (opponent != null) {
                        currentOffW = opponent.offensiveWeight
                        currentDefW = opponent.defensiveWeight
                    }
                } else {
                    currentOffW = settings.offensiveWeight
                    currentDefW = settings.defensiveWeight
                }

                if(isFirstMove) {
                    getRandomOpeningMove(hash.p1, hash.p2, currentOffW, currentDefW)
                }
                else if (settings.difficulty >= 10) {
                    getBestStepNativeTT(hash.p1, hash.p2, 1, settings.difficulty, settings.riposteAllowed, separationStepsLeft, currentOffW, currentDefW)
                } else {
                    getBestStepNative(hash.p1, hash.p2, 1, settings.difficulty, settings.riposteAllowed, separationStepsLeft, currentOffW, currentDefW)
                }
            }
            val thinkTime = System.currentTimeMillis() - startTime
            if (thinkTime < 600) delay(600 - thinkTime)

            applyAiMove(move)

            if (playerCaptured[1] >= 2) {
                timerJob?.cancel()
                triggerSound(SoundType.LOSE, 1)
                winner = "AI won :("
                gamePhase = GameWaitingFor.GAME_OVER
                processTournamentMatchEnd(isWin = false)
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

    private fun createCurrentSnapshot(): GameStateSnapshot {
        return GameStateSnapshot(
            hash = calculateBoardHash(),
            pieces = pieces.map { it.copy() },
            playerCaptured = playerCaptured.copyOf(),
            currentPlayerId = currentPlayerId,
            afterTouche = afterTouche,
            gamePhase = gamePhase,
            historyBaseIndex = historyBaseIndex,
            historyStackSize = historyStack.size,
            separationStepsLeft = separationStepsLeft
        )
    }

    private fun applySnapshot(snapshot: GameStateSnapshot, isReview: Boolean) {
        restoreBoardFromHash(snapshot.hash)
        pieces.clear()
        pieces.addAll(snapshot.pieces.map { it.copy() })
        playerCaptured = snapshot.playerCaptured
        currentPlayerId = snapshot.currentPlayerId
        afterTouche = snapshot.afterTouche

        if (!isReview) {
            gamePhase = snapshot.gamePhase
            historyBaseIndex = snapshot.historyBaseIndex
            while (historyStack.size > snapshot.historyStackSize) {
                historyStack.removeAt(historyStack.size - 1)
            }
            separationStepsLeft = snapshot.separationStepsLeft
            activeHint = null
        }
    }

    private fun saveState() {
        undoStack.add(createCurrentSnapshot())
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val lastState = undoStack.removeAt(undoStack.size - 1)
            applySnapshot(lastState, isReview = false)
        }
    }

    fun canReviewPrevious(): Boolean {
        if (reviewCursorIndex == -1) return undoStack.isNotEmpty()
        return reviewCursorIndex > 0
    }

    fun canReviewNext(): Boolean {
        return reviewCursorIndex != -1
    }

    fun reviewPreviousMove() {
        if (!canReviewPrevious()) return

        if (reviewCursorIndex == -1) {
            finalGameStateSnapshot = createCurrentSnapshot()
            reviewCursorIndex = undoStack.size - 1
        } else {
            reviewCursorIndex--
        }
        applySnapshot(undoStack[reviewCursorIndex], isReview = true)
    }

    fun reviewNextMove() {
        if (!canReviewNext()) return

        reviewCursorIndex++
        if (reviewCursorIndex >= undoStack.size) {
            finalGameStateSnapshot?.let { applySnapshot(it, isReview = true) }
            reviewCursorIndex = -1
        } else {
            applySnapshot(undoStack[reviewCursorIndex], isReview = true)
        }
    }

    fun endReviewMode() {
        if (reviewCursorIndex != -1 && finalGameStateSnapshot != null) {
            applySnapshot(finalGameStateSnapshot!!, isReview = true)
        }
        reviewCursorIndex = -1
        finalGameStateSnapshot = null
    }

    fun processTournamentMatchEnd(isWin: Boolean): String {
        if (!isTournamentMode) return ""

        isMatchProcessed = true
        isInterruptedGame = false

        if (matchThreatLog.isNotEmpty()) {
            val totalHs1 = matchThreatLog.sumOf { it.hs1 }.toFloat()
            val totalHs2 = matchThreatLog.sumOf { it.hs2 }.toFloat()
            val tacticalDifference = totalHs1 - totalHs2

            if (tacticalDifference != 0f) {
                val offensiveLevel = totalHs1 / tacticalDifference
                val defensiveLevel = -totalHs2 / tacticalDifference
                println("⚔️ PLAYER PROFILE: Offensive = $offensiveLevel, Defensive = $defensiveLevel ⚔️")
            } else {
                println("⚔️ PLAYER PROFILE: Balanced ⚔️")
            }
        }

        val rank = tournamentTargetRank ?: 20
        val baseScore = ((25 - rank) * (25 - rank)).toFloat()

        val finalScore = if (!isWin) {
            (baseScore * 0.3f).toInt()
        } else {
            val timeDiffSec = (playerTimeMs - opponentTimeMs) / 1000f
            val timeBonus = if (timeDiffSec > 0) (timeDiffSec / 60f).coerceAtMost(1.0f) * 0.2f else 0.0f
            (baseScore * (1.0f + timeBonus)).toInt()
        }

        val resultMsg = tournamentManager.onMatchFinished(isWin, finalScore)

        viewModelScope.launch {
            withContext(NonCancellable) {
                val currentSettings = settingsManager.settingsFlow.first()

                val currentThreats = currentSettings.recentThreats.split(",").filter { it.isNotEmpty() }.toMutableList()
                if (matchThreatLog.isNotEmpty()) {
                    val tHs1 = matchThreatLog.sumOf { it.hs1 }
                    val tHs2 = matchThreatLog.sumOf { it.hs2 }
                    currentThreats.add("$tHs1:$tHs2")
                }
                val updatedThreats = currentThreats.takeLast(3).joinToString(",")

                val cleanHistory = currentSettings.tournamentMatchHistory.filter { it == 'W' || it == 'L' }
                val newResultChar = if (isWin) "W" else "L"
                val safeHistory = (cleanHistory + newResultChar).takeLast(50)

                settingsManager.updateSettings(
                    currentSettings.copy(
                        hasSavedTournamentMatch = false,
                        recentThreats = updatedThreats,
                        tournamentRank = tournamentManager.currentRank,
                        tournamentHighest = tournamentManager.highestRank,
                        tournamentDefending = tournamentManager.isDefending,
                        tournamentMatchHistory = safeHistory,
                        tournamentScoreHistory = resultMsg,
                        highestRating = tournamentManager.highestRating
                    )
                )
            }
        }

        return resultMsg
    }

    suspend fun saveTournamentStateToDisk() {
        if (gamePhase == GameWaitingFor.GAME_OVER || !isTournamentMode) return

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
        gamePhase = GameWaitingFor.GAME_OVER
        processTournamentMatchEnd(isWin = false)
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.releaseMusic()
        soundManager.release()
    }

    private external fun getBestStepNative(p1: Long, p2: Long, p: Int, d: Int, r: Boolean, sep: Int, offW: Int, defW: Int): MoveData
    private external fun getBestStepNativeTT(p1: Long, p2: Long, p: Int, d: Int, r: Boolean, sep: Int, offW: Int, defW: Int): MoveData

    private fun evaluateThreatLevels(currentBoard: IntArray): ThreatRecord {
        val touchePoint = 4
        val hotSpotIndex = currentBoard.indexOf(touchePoint)
        if (hotSpotIndex == -1) return ThreatRecord(0, 0)

        val hotX = hotSpotIndex % 5
        val hotY = hotSpotIndex / 5

        var hs1 = 0
        var hs2 = 0

        val directions = arrayOf(
            Pair(0, -1), Pair(0, 1), Pair(-1, 0), Pair(1, 0),
            Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)
        )

        for (dir in directions) {
            var cx = hotX + dir.first
            var cy = hotY + dir.second

            while (cx in 0..4 && cy in 0..6) {
                val piece = currentBoard[cy * 5 + cx]
                if (piece == 1) { hs1++; break }
                else if (piece == 2) { hs2++; break }

                cx += dir.first
                cy += dir.second
            }
        }
        return ThreatRecord(hs1, hs2)
    }

    companion object {
        init {
            System.loadLibrary("riposte")
        }
    }
}