package hu.riposte.game.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import hu.riposte.game.R

private val classicWins = listOf(R.string.quote_classic_win_1, R.string.quote_classic_win_2, R.string.quote_classic_win_3, R.string.quote_classic_win_4)
private val classicLoses = listOf(R.string.quote_classic_lose_1, R.string.quote_classic_lose_2, R.string.quote_classic_lose_3)

private val zenWins = listOf(R.string.quote_zen_win_1, R.string.quote_zen_win_2, R.string.quote_zen_win_3)
private val zenLoses = listOf(R.string.quote_zen_lose_1, R.string.quote_zen_lose_2, R.string.quote_zen_lose_3)

private val modernWins = listOf(R.string.quote_modern_win_1, R.string.quote_modern_win_2, R.string.quote_modern_win_3)
private val modernLoses = listOf(R.string.quote_modern_lose_1, R.string.quote_modern_lose_2, R.string.quote_modern_lose_3)

object ThemeRegistry {

    val Destreza = GameTheme(
        id = "destreza",
        displayName = "Destreza Manuscript",
        backgroundRes = R.drawable.bg_board_destreza,
        boardBackgroundRes = R.drawable.bg_destreza,
        pieceP1Res = R.drawable.ic_destreza_p1,
        pieceP2Res = R.drawable.ic_destreza_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_destreza,
        bgMusicRes = R.raw.bg_music_destreza,
        moveSoundP1Res = R.raw.p1_move_destreza,
        moveSoundP2Res = R.raw.p2_move_destreza,
        toucheSoundRes = R.raw.touche_hit_destreza,
        boardCellDark = Color(0x26000000),
        boardCellLight = Color(0x1AFFFFFF),
        auraP1Color = Color(0xFFB71C1C),
        auraP2Color = Color(0xFF455A64),
        containerColor = Color(0xFF3E2723),
        textColor = Color(0xFFFFE082),
        uiAccentColor = Color(0xFFD4AF37),
        victoryQuotes = classicWins,
        defeatQuotes = classicLoses,
        fontFamily = FontFamily.Serif
    )
    val LaMaupin = GameTheme(
        id = "la_maupin",
        displayName = "La Maupin",
        backgroundRes = R.drawable.bg_la_maupin,
        boardBackgroundRes = R.drawable.bg_board_la_maupin,
        pieceP1Res = R.drawable.ic_la_maupin_p1,
        pieceP2Res = R.drawable.ic_la_maupin_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_la_maupin,
        bgMusicRes = R.raw.bg_music_la_maupin,
        moveSoundP1Res = R.raw.p1_move_la_maupin,
        moveSoundP2Res = R.raw.p2_move_la_maupin,
        toucheSoundRes = R.raw.touche_hit_la_maupin,
        boardCellDark = Color(0x668B2245),
        boardCellLight = Color(0x33FFD1DC),
        auraP1Color = Color(0xFFE91E63),
        auraP2Color = Color(0xFF90A4AE),
        containerColor = Color(0xFF2A1318),
        textColor = Color(0xFFFFF0F5),
        uiAccentColor = Color(0xFFFFB6C1),
        victoryQuotes = classicWins,
        defeatQuotes = classicLoses,
        fontFamily = FontFamily.Serif
    )
    val Highwayman = GameTheme(
        id = "highwayman",
        displayName = "The Highwayman",
        backgroundRes = R.drawable.bg_highwayman,
        boardBackgroundRes = R.drawable.bg_board_highwayman,
        pieceP1Res = R.drawable.ic_highwayman_p1,
        pieceP2Res = R.drawable.ic_highwayman_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_highwayman,
        bgMusicRes = R.raw.bg_music_highwayman,
        moveSoundP1Res = R.raw.p1_move_destreza,
        moveSoundP2Res = R.raw.p2_move_destreza,
        toucheSoundRes = R.raw.touche_hit_destreza,
        boardCellDark = Color(0xFF141B12).copy(alpha = 0.6f),
        boardCellLight = Color(0xFFB0BEC5).copy(alpha = 0.15f),
        auraP1Color = Color(0xFFD32F2F),
        auraP2Color = Color(0xFF90A4AE),
        containerColor = Color(0xFF2D241E),
        textColor = Color(0xFFF5F5F5),
        uiAccentColor = Color(0xFFE6A86A),
        victoryQuotes = classicWins,
        defeatQuotes = classicLoses,
        fontFamily = FontFamily.Serif
    )
    val VenetianMasquerade = GameTheme(
        id = "venetian_masquerade",
        displayName = "Venetian Masquerade",
        backgroundRes = R.drawable.bg_venice,
        boardBackgroundRes = R.drawable.bg_board_venice,
        pieceP1Res = R.drawable.ic_venice_p1,
        pieceP2Res = R.drawable.ic_venice_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_venice,
        bgMusicRes = R.raw.bg_music_venice,
        moveSoundP1Res = R.raw.p1_move_destreza,
        moveSoundP2Res = R.raw.p2_move_destreza,
        toucheSoundRes = R.raw.touche_hit_destreza,
        boardCellDark = Color(0xFF4B0082).copy(alpha = 0.3f),
        boardCellLight = Color(0xFFFFD700).copy(alpha = 0.1f),
        auraP1Color = Color(0xFFFFD700),
        auraP2Color = Color(0xFF00C853),
        containerColor = Color(0xFF310A31),
        textColor = Color(0xFFFFF8E1),
        uiAccentColor = Color(0xFFFFD180),
        victoryQuotes = classicWins,
        defeatQuotes = classicLoses,
        fontFamily = FontFamily.Serif
    )
    val OlympicCarbon = GameTheme(
        id = "olympic_carbon",
        displayName = "Olympic Carbon",
        backgroundRes = R.drawable.bg_carbon,
        boardBackgroundRes = R.drawable.bg_board_carbon,
        pieceP1Res = R.drawable.ic_carbon_p1,
        pieceP2Res = R.drawable.ic_carbon_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_board_carbon,
        bgMusicRes = R.raw.bg_music_carbon,
        moveSoundP1Res = R.raw.p1_move,
        moveSoundP2Res = R.raw.p2_move,
        toucheSoundRes = R.raw.touche_hit,
        boardCellDark = Color(0xFFB0BEC5).copy(alpha = 0.2f),
        boardCellLight = Color.White.copy(alpha = 0.15f),
        auraP1Color = Color(0xFF80DEEA),
        auraP2Color = Color(0xFFD32F2F),
        containerColor = Color(0xFFECEFF1),
        textColor = Color(0xFFE0F7FA),
        uiAccentColor = Color(0xFF18FFFF),
        victoryQuotes = modernWins,
        defeatQuotes = modernLoses,
        fontFamily = FontFamily.Monospace
    )
    val InnerSpirit = GameTheme(
        id = "inner_spirit",
        displayName = "Inner Spirit",
        backgroundRes = R.drawable.bg_inner,
        boardBackgroundRes = R.drawable.bg_board_inner,
        pieceP1Res = R.drawable.ic_inner_p1,
        pieceP2Res = R.drawable.ic_inner_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_inner,
        bgMusicRes = R.raw.bg_music_inner,
        moveSoundP1Res = R.raw.p1_move,
        moveSoundP2Res = R.raw.p2_move,
        toucheSoundRes = R.raw.touche_hit,
        boardCellDark = Color(0xFF311B92).copy(alpha = 0.3f),
        boardCellLight = Color(0xFFB2EBF2).copy(alpha = 0.15f),
        auraP1Color = Color(0xFFE1BEE7),
        auraP2Color = Color(0xFF80CBC4),
        containerColor = Color(0xFF1A237E),
        textColor = Color(0xFFE0F7FA),
        uiAccentColor = Color(0xFF82B1FF),
        victoryQuotes = modernWins,
        defeatQuotes = modernLoses,
        fontFamily = FontFamily.Monospace
    )
    val Vive2024Paris = GameTheme(
        id = "vive_2024_paris",
        displayName = "Vive 2024 Paris",
        backgroundRes = R.drawable.bg_vive2024,
        boardBackgroundRes = R.drawable.bg_board_vive2024,
        pieceP1Res = R.drawable.ic_vive2024_p1,
        pieceP2Res = R.drawable.ic_vive2024_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_vive2024,
        bgMusicRes = R.raw.bg_music_vive2024,
        moveSoundP1Res = R.raw.p1_move,
        moveSoundP2Res = R.raw.p2_move,
        toucheSoundRes = R.raw.touche_hit,
        boardCellDark = Color(0xFF0D1B3E).copy(alpha = 0.45f),
        boardCellLight = Color(0xFFE0F7FA).copy(alpha = 0.12f),
        auraP1Color = Color(0xFF9D50BB),
        auraP2Color = Color(0xFF00E5FF),
        containerColor = Color(0xFF0A122A),
        textColor = Color(0xFFF0F4F8),
        uiAccentColor = Color(0xFFFFD700),
        victoryQuotes = modernWins,
        defeatQuotes = modernLoses,
        fontFamily = FontFamily.Monospace
    )
    val eSportVision = GameTheme(
        id = "esport_vision",
        displayName = "eSport Vision",
        backgroundRes = R.drawable.bg_esport,
        boardBackgroundRes = R.drawable.bg_board_esport,
        pieceP1Res = R.drawable.ic_esport_p1,
        pieceP2Res = R.drawable.ic_esport_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_esport,
        bgMusicRes = R.raw.bg_music_esport,
        moveSoundP1Res = R.raw.p1_move,
        moveSoundP2Res = R.raw.p2_move,
        toucheSoundRes = R.raw.touche_hit,
        boardCellDark = Color(0xFF0A0A0C).copy(alpha = 0.85f),
        boardCellLight = Color(0xFF1A1A20).copy(alpha = 0.65f),
        auraP1Color = Color(0xFF00E5FF),
        auraP2Color = Color(0xFFFF0055),
        containerColor = Color(0xFF111114),
        textColor = Color(0xFFF0F4F8),
        uiAccentColor = Color(0xFF00FFAA),
        victoryQuotes = modernWins,
        defeatQuotes = modernLoses,
        fontFamily = FontFamily.Monospace
    )

    // --- ZEN TÉMÁK: Cursive lecserélve SansSerif-re a maximális olvashatóságért ---
    val WinterPagoda = GameTheme(
        id = "winter_pagoda",
        displayName = "Winter Pagoda",
        backgroundRes = R.drawable.bg_winter,
        boardBackgroundRes = R.drawable.bg_board_winter,
        pieceP1Res = R.drawable.ic_winter_p1,
        pieceP2Res = R.drawable.ic_winter_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_board_winter,
        bgMusicRes = R.raw.bg_music_winter,
        moveSoundP1Res = R.raw.p1_move,
        moveSoundP2Res = R.raw.p2_move,
        toucheSoundRes = R.raw.touche_hit,
        boardCellDark = Color(0xFFB0B0C5).copy(alpha = 0.3f),
        boardCellLight = Color.White.copy(alpha = 0.15f),
        auraP1Color = Color(0x3080DEEA),
        auraP2Color = Color(0x1AD32F2F),
        containerColor = Color(0xFFECEFF1),
        textColor = Color(0xFFE1F5FE),
        uiAccentColor = Color(0xFF18FFFF),
        victoryQuotes = zenWins,
        defeatQuotes = zenLoses,
        fontFamily = FontFamily.SansSerif // JAVÍTVA
    )
    val DaturaBlossom = GameTheme(
        id = "datura_blossom",
        displayName = "Datura Blossom",
        backgroundRes = R.drawable.bg_datura,
        boardBackgroundRes = R.drawable.bg_board_datura,
        pieceP1Res = R.drawable.ic_datura_p1,
        pieceP2Res = R.drawable.ic_datura_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_board_datura,
        bgMusicRes = R.raw.bg_music_datura,
        moveSoundP1Res = R.raw.p1_move,
        moveSoundP2Res = R.raw.p2_move,
        toucheSoundRes = R.raw.touche_hit,
        boardCellDark = Color(0xFF4A148C).copy(alpha = 0.2f),
        boardCellLight = Color(0xFFCE93D8).copy(alpha = 0.15f),
        auraP1Color = Color(0xFFEA80FC),
        auraP2Color = Color(0xFF00E5FF),
        containerColor = Color(0xFFF3E5F5),
        textColor = Color(0xFFF3E5F5),
        uiAccentColor = Color(0xFFE040FB),
        victoryQuotes = zenWins,
        defeatQuotes = zenLoses,
        fontFamily = FontFamily.SansSerif // JAVÍTVA
    )
    val MayanFresco = GameTheme(
        id = "mayan_fresco",
        displayName = "Mayan Fresco",
        backgroundRes = R.drawable.bg_mayan,
        boardBackgroundRes = R.drawable.bg_board_mayan,
        pieceP1Res = R.drawable.ic_mayan_p1,
        pieceP2Res = R.drawable.ic_mayan_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_board_mayan,
        bgMusicRes = R.raw.bg_music_mayan,
        moveSoundP1Res = R.raw.p1_move_jungle,
        moveSoundP2Res = R.raw.p2_move_jungle,
        toucheSoundRes = R.raw.touche_hit_jungle,
        boardCellDark = Color(0x36000000),
        boardCellLight = Color(0x24FFFFFF),
        auraP1Color = Color(0xFFFFB300),
        auraP2Color = Color(0xFF8D6E63),
        containerColor = Color(0xFFF1F8E9),
        textColor = Color(0xFFE8F5E9),
        uiAccentColor = Color(0xFFFF7043),
        victoryQuotes = zenWins,
        defeatQuotes = zenLoses,
        fontFamily = FontFamily.SansSerif // JAVÍTVA
    )
    val AfricanSiesta = GameTheme(
        id = "african_siesta",
        displayName = "African Siesta",
        backgroundRes = R.drawable.bg_african,
        boardBackgroundRes = R.drawable.bg_board_african,
        pieceP1Res = R.drawable.ic_african_p1,
        pieceP2Res = R.drawable.ic_african_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_board_african,
        bgMusicRes = R.raw.bg_music_african,
        moveSoundP1Res = R.raw.p1_move_jungle,
        moveSoundP2Res = R.raw.p2_move_jungle,
        toucheSoundRes = R.raw.touche_hit_jungle,
        boardCellDark = Color(0x36000000),
        boardCellLight = Color(0x24FFFFFF),
        auraP1Color = Color(0xFFFFB300),
        auraP2Color = Color(0xFF8D6E63),
        containerColor = Color(0xFFF1F8E9),
        textColor = Color(0xFFFFF3E0),
        uiAccentColor = Color(0xFFFF7043),
        victoryQuotes = zenWins,
        defeatQuotes = zenLoses,
        fontFamily = FontFamily.SansSerif // JAVÍTVA
    )
    val Tournament = GameTheme(
        id = "tournament",
        displayName = "Hall of Legends",
        backgroundRes = R.drawable.bg_board_destreza,
        boardBackgroundRes = R.drawable.bg_board_destreza,
        pieceP1Res = R.drawable.ic_destreza_p1,
        pieceP2Res = R.drawable.ic_destreza_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_board_destreza,
        bgMusicRes = R.raw.main_menu_music,
        moveSoundP1Res = R.raw.p1_move_destreza,
        moveSoundP2Res = R.raw.p2_move_destreza,
        toucheSoundRes = R.raw.touche_hit_destreza,
        boardCellDark = Color(0xFF0F141E).copy(alpha = 0.6f),
        boardCellLight = Color.White.copy(alpha = 0.05f),
        auraP1Color = Color(0xFFD4AF37),
        auraP2Color = Color(0xFF4A5570),
        containerColor = Color(0xFF0A0C10),
        textColor = Color.White,
        uiAccentColor = Color(0xFFD4AF37),
        victoryQuotes = classicWins,
        defeatQuotes = classicLoses,
        fontFamily = FontFamily.Serif
    )

    val classicThemes = listOf(Destreza, LaMaupin, Highwayman, VenetianMasquerade )
    val modernThemes = listOf(OlympicCarbon, InnerSpirit, Vive2024Paris, eSportVision)
    val zenThemes = listOf(DaturaBlossom, WinterPagoda, MayanFresco, AfricanSiesta)

    val allThemes = classicThemes + modernThemes + zenThemes

    fun getThemeById(id: String): GameTheme {
        return allThemes.find { it.id == id } ?: Destreza
    }
}