package hu.riposte.game

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

enum class GameWaitingFor {
    SETUP,
    MOVE_PIECE,
    TAKE_PIECE,
    ANIMATION,
    AI_MOVE,
    GAME_OVER
}
enum class GameMode {
    VS_AI, LOCAL_MULTIPLAYER
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

data class Coord(val x: Int, val y: Int) {
    fun toIndex(): Int = y * 5 + x
    companion object {
        fun fromIndex(index: Int): Coord = Coord(index % 5, index / 5)
        val Invalid = Coord(-1, -1)
    }
}
enum class PieceState {
    IN_PLAY,
    BEING_CAPTURED,
    CAPTURED
}

data class Piece(
    val id: Int,
    val owner: Int,
    var pos: Coord,
    val state: PieceState = PieceState.IN_PLAY // A két Boolean helyett!
)
data class GameStateSnapshot(
    val board: IntArray,
    val pieces: List<Piece>, // A Piece data class, így a copy() miatt biztonságos
    val playerCaptured: IntArray,
    val currentPlayerId: Int,
    val afterTouche: Boolean,
    val gamePhase: GameWaitingFor
)


enum class MenuScreen {
    MAIN,
    VS_AI_SETTINGS,
    ONLINE_LOBBY,
    HOW_TO_PLAY
}
enum class SoundType {
    MOVE,
    TOUCHE,
    WIN,
    LOSE }
data class SoundEvent(
    val type: SoundType,
    val playerId: Int,
    val triggerId:
    Long = System.currentTimeMillis()
)