package hu.riposte.game

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class GameTheme(
    val id: String,
    val displayName: String,

    // --- KÉPEK ÉS GRAFIKÁK ---
    val backgroundRes: Int,
    val pieceP1Res: Int,
    val pieceP2Res: Int,
    val toucheStarRes: Int,
    val previewImageRes: Int,

    // --- HANGOK ÉS ZENE ---
    val bgMusicRes: Int,
    val moveSoundP1Res: Int,
    val moveSoundP2Res: Int,
    val toucheSoundRes: Int,

    // --- SZÍNEK ---
    val boardCellDark: Color,
    val boardCellLight: Color,
    val auraP1Color: Color,
    val auraP2Color: Color,
    val containerColor: Color,
    val textColor: Color,
    val uiAccentColor: Color,

    // --- ÚJ ANIMÁCIÓS PARAMÉTEREK ---
    val touchePulseDuration: Int = 1200,
    val toucheRotationDuration: Int = 12000,
    val toucheScaleMin: Float = 0.8f,
    val toucheScaleMax: Float = 1.0f
)

val LocalGameTheme = staticCompositionLocalOf<GameTheme> {
    error("error")
}