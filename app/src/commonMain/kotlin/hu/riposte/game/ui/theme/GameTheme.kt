package hu.riposte.game.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class GameTheme(
    val id: String,
    val displayName: String,

    // --- PICTURE ASSETS ---
    val backgroundRes: DrawableResource,
    val boardBackgroundRes: DrawableResource,
    val pieceP1Res: DrawableResource,
    val pieceP2Res: DrawableResource,
    val toucheStarRes: DrawableResource,
    val previewImageRes: DrawableResource,

    // --- MUSIC & SFX ASSETS ---
    val bgMusicRes: String,
    val moveSoundP1Res: String,
    val moveSoundP2Res: String,
    val toucheSoundRes: String,

    // --- COLORS ---
    val boardCellDark: Color,
    val boardCellLight: Color,
    val auraP1Color: Color,
    val auraP2Color: Color,
    val containerColor: Color,
    val textColor: Color,
    val uiAccentColor: Color,

    // --- QUOTES & TYPOGRAPHY ---
    val victoryQuotes: List<StringResource>,
    val defeatQuotes: List<StringResource>,
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
