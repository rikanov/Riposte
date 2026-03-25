package hu.riposte.game

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

enum class GameWaitingFor {
    SETUP,
    MOVE_PIECE,
    TAKE_PIECE,
    AI_MOVE,
    GAME_OVER
}
enum class GameMode {
    VS_AI, VS_PLAYER
}
enum class StartingPlayer {
    PLAYER, AI, ALTERNATING
}

data class GameSettings(
    val gameMode: GameMode = GameMode.VS_AI,
    val startingPlayer: StartingPlayer = StartingPlayer.PLAYER,
    val difficulty: Int = 5,
    val riposteAllowed: Boolean = true
)

class GameState(
    val snapshot: List<Int> = mutableStateListOf<Int>(),
    var captured: IntArray = intArrayOf(0,0,0),
    var afterTouche: Boolean = false
)