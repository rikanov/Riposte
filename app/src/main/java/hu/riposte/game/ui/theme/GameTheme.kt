package hu.riposte.game.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class GameTheme(
    val id: String,
    val displayName: String,

    // --- PICTURE ASSETS ---
    val backgroundRes: Int,
    val boardBackgroundRes: Int,
    val pieceP1Res: Int,
    val pieceP2Res: Int,
    val toucheStarRes: Int,
    val previewImageRes: Int,

    // --- MUSIC & SFX ASSETS ---
    val bgMusicRes: Int,
    val moveSoundP1Res: Int,
    val moveSoundP2Res: Int,
    val toucheSoundRes: Int,

    // --- COLORS ---
    val boardCellDark: Color,
    val boardCellLight: Color,
    val auraP1Color: Color,
    val auraP2Color: Color,
    val containerColor: Color,
    val textColor: Color,
    val uiAccentColor: Color,

    // --- QUOTES & TYPOGRAPHY ---
    val victoryQuotes: List<Int>,
    val defeatQuotes: List<Int>,
    val fontFamily: FontFamily,

    // --- PARAMETERS FOR ANIMATIONS ---
    val touchePulseDuration: Int = 1200,
    val toucheRotationDuration: Int = 12000,
    val toucheScaleMin: Float = 0.8f,
    val toucheScaleMax: Float = 1.0f
)

val LocalGameTheme = staticCompositionLocalOf<GameTheme> {
    error("error")
}