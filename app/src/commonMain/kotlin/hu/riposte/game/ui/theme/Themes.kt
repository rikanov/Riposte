package hu.riposte.game.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.*
import riposte.app.generated.resources.*

private val destrezaWins = listOf(Res.string.quote_destreza_win_1, Res.string.quote_destreza_win_2, Res.string.quote_destreza_win_3)
private val destrezaLoses = listOf(Res.string.quote_destreza_lose_1, Res.string.quote_destreza_lose_2, Res.string.quote_destreza_lose_3)

private val maupinWins = listOf(Res.string.quote_maupin_win_1, Res.string.quote_maupin_win_2, Res.string.quote_maupin_win_3)
private val maupinLoses = listOf(Res.string.quote_maupin_lose_1, Res.string.quote_maupin_lose_2, Res.string.quote_maupin_lose_3)

private val highwaymanWins = listOf(Res.string.quote_highwayman_win_1, Res.string.quote_highwayman_win_2, Res.string.quote_highwayman_win_3)
private val highwaymanLoses = listOf(Res.string.quote_highwayman_lose_1, Res.string.quote_highwayman_lose_2, Res.string.quote_highwayman_lose_3)

private val veniceWins = listOf(Res.string.quote_venice_win_1, Res.string.quote_venice_win_2, Res.string.quote_venice_win_3)
private val veniceLoses = listOf(Res.string.quote_venice_lose_1, Res.string.quote_venice_lose_2, Res.string.quote_venice_lose_3)

private val carbonWins = listOf(Res.string.quote_carbon_win_1, Res.string.quote_carbon_win_2, Res.string.quote_carbon_win_3)
private val carbonLoses = listOf(Res.string.quote_carbon_lose_1, Res.string.quote_carbon_lose_2, Res.string.quote_carbon_lose_3)

private val innerWins = listOf(Res.string.quote_inner_win_1, Res.string.quote_inner_win_2, Res.string.quote_inner_win_3)
private val innerLoses = listOf(Res.string.quote_inner_lose_1, Res.string.quote_inner_lose_2, Res.string.quote_inner_lose_3)

private val parisWins = listOf(Res.string.quote_paris_win_1, Res.string.quote_paris_win_2, Res.string.quote_paris_win_3)
private val parisLoses = listOf(Res.string.quote_paris_lose_1, Res.string.quote_paris_lose_2, Res.string.quote_paris_lose_3)

private val esportWins = listOf(Res.string.quote_esport_win_1, Res.string.quote_esport_win_2, Res.string.quote_esport_win_3)
private val esportLoses = listOf(Res.string.quote_esport_lose_1, Res.string.quote_esport_lose_2, Res.string.quote_esport_lose_3)

private val winterWins = listOf(Res.string.quote_winter_win_1, Res.string.quote_winter_win_2, Res.string.quote_winter_win_3)
private val winterLoses = listOf(Res.string.quote_winter_lose_1, Res.string.quote_winter_lose_2, Res.string.quote_winter_lose_3)

private val daturaWins = listOf(Res.string.quote_datura_win_1, Res.string.quote_datura_win_2, Res.string.quote_datura_win_3)
private val daturaLoses = listOf(Res.string.quote_datura_lose_1, Res.string.quote_datura_lose_2, Res.string.quote_datura_lose_3)

private val mayanWins = listOf(Res.string.quote_mayan_win_1, Res.string.quote_mayan_win_2, Res.string.quote_mayan_win_3)
private val mayanLoses = listOf(Res.string.quote_mayan_lose_1, Res.string.quote_mayan_lose_2, Res.string.quote_mayan_lose_3)

private val africanWins = listOf(Res.string.quote_african_win_1, Res.string.quote_african_win_2, Res.string.quote_african_win_3)
private val africanLoses = listOf(Res.string.quote_african_lose_1, Res.string.quote_african_lose_2, Res.string.quote_african_lose_3)


object ThemeRegistry {

    val Destreza = GameTheme(
        id = "destreza",
        displayName = "Destreza Manuscript",
        backgroundRes = Res.drawable.bg_board_destreza,
        boardBackgroundRes = Res.drawable.bg_destreza,
        pieceP1Res = Res.drawable.ic_destreza_p1,
        pieceP2Res = Res.drawable.ic_destreza_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_destreza,
        bgMusicRes = "bg_music_destreza.ogg",
        moveSoundP1Res = "p1_move_destreza.ogg",
        moveSoundP2Res = "p2_move_destreza.ogg",
        toucheSoundRes = "touche_hit_destreza.ogg",
        boardCellDark = Color(0x26000000),
        boardCellLight = Color(0x1AFFFFFF),
        auraP1Color = Color(0xFFB71C1C),
        auraP2Color = Color(0xFF455A64),
        containerColor = Color(0xFF3E2723),
        textColor = Color(0xFFFFE082),
        uiAccentColor = Color(0xFFD4AF37),
        victoryQuotes = destrezaWins,
        defeatQuotes = destrezaLoses,
        fontFamily = FontFamily.Serif
    )
    val LaMaupin = GameTheme(
        id = "la_maupin",
        displayName = "La Maupin",
        backgroundRes = Res.drawable.bg_la_maupin,
        boardBackgroundRes = Res.drawable.bg_board_la_maupin,
        pieceP1Res = Res.drawable.ic_la_maupin_p1,
        pieceP2Res = Res.drawable.ic_la_maupin_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_la_maupin,
        bgMusicRes = "bg_music_la_maupin.ogg",
        moveSoundP1Res = "p1_move_la_maupin.ogg",
        moveSoundP2Res = "p2_move_la_maupin.ogg",
        toucheSoundRes = "touche_hit_la_maupin.ogg",
        boardCellDark = Color(0x668B2245),
        boardCellLight = Color(0x33FFD1DC),
        auraP1Color = Color(0xFFE91E63),
        auraP2Color = Color(0xFF90A4AE),
        containerColor = Color(0xFF2A1318),
        textColor = Color(0xFFFFF0F5),
        uiAccentColor = Color(0xFFFFB6C1),
        victoryQuotes = maupinWins,
        defeatQuotes = maupinLoses,
        fontFamily = FontFamily.Serif
    )
    val Highwayman = GameTheme(
        id = "highwayman",
        displayName = "The Highwayman",
        backgroundRes = Res.drawable.bg_highwayman,
        boardBackgroundRes = Res.drawable.bg_board_highwayman,
        pieceP1Res = Res.drawable.ic_highwayman_p1,
        pieceP2Res = Res.drawable.ic_highwayman_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_highwayman,
        bgMusicRes = "bg_music_highwayman.ogg",
        moveSoundP1Res = "p1_move.ogg",
        moveSoundP2Res = "p2_move.ogg",
        toucheSoundRes = "touche_hit_destreza.ogg",
        boardCellDark = Color(0xFF141B12).copy(alpha = 0.6f),
        boardCellLight = Color(0xFFB0BEC5).copy(alpha = 0.15f),
        auraP1Color = Color(0xFFD32F2F),
        auraP2Color = Color(0xFF90A4AE),
        containerColor = Color(0xFF2D241E),
        textColor = Color(0xFFF5F5F5),
        uiAccentColor = Color(0xFFE6A86A),
        victoryQuotes = highwaymanWins,
        defeatQuotes = highwaymanLoses,
        fontFamily = FontFamily.Serif
    )
    val VenetianMasquerade = GameTheme(
        id = "venetian_masquerade",
        displayName = "Venetian Masquerade",
        backgroundRes = Res.drawable.bg_venice,
        boardBackgroundRes = Res.drawable.bg_board_venice,
        pieceP1Res = Res.drawable.ic_venice_p1,
        pieceP2Res = Res.drawable.ic_venice_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_venice,
        bgMusicRes = "bg_music_venice.ogg",
        moveSoundP1Res = "p1_move_destreza.ogg",
        moveSoundP2Res = "p2_move_destreza.ogg",
        toucheSoundRes = "touche_hit_destreza.ogg",
        boardCellDark = Color(0xFF4B0082).copy(alpha = 0.3f),
        boardCellLight = Color(0xFFFFD700).copy(alpha = 0.1f),
        auraP1Color = Color(0xFFFFD700),
        auraP2Color = Color(0xFF00C853),
        containerColor = Color(0xFF310A31),
        textColor = Color(0xFFFFF8E1),
        uiAccentColor = Color(0xFFFFD180),
        victoryQuotes = veniceWins,
        defeatQuotes = veniceLoses,
        fontFamily = FontFamily.Serif
    )
    val OlympicCarbon = GameTheme(
        id = "olympic_carbon",
        displayName = "Olympic Carbon",
        backgroundRes = Res.drawable.bg_carbon,
        boardBackgroundRes = Res.drawable.bg_board_carbon,
        pieceP1Res = Res.drawable.ic_carbon_p1,
        pieceP2Res = Res.drawable.ic_carbon_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_board_carbon,
        bgMusicRes = "bg_music_carbon.ogg",
        moveSoundP1Res = "p1_move.ogg",
        moveSoundP2Res = "p2_move.ogg",
        toucheSoundRes = "touche_hit.ogg",
        boardCellDark = Color(0xFFB0BEC5).copy(alpha = 0.2f),
        boardCellLight = Color.White.copy(alpha = 0.15f),
        auraP1Color = Color(0xFF80DEEA),
        auraP2Color = Color(0xFFD32F2F),
        containerColor = Color(0xFFECEFF1),
        textColor = Color(0xFFE0F7FA),
        uiAccentColor = Color(0xFF18FFFF),
        victoryQuotes = carbonWins,
        defeatQuotes = carbonLoses,
        fontFamily = FontFamily.Monospace
    )
    val InnerSpirit = GameTheme(
        id = "inner_spirit",
        displayName = "Inner Spirit",
        backgroundRes = Res.drawable.bg_inner,
        boardBackgroundRes = Res.drawable.bg_board_inner,
        pieceP1Res = Res.drawable.ic_inner_p1,
        pieceP2Res = Res.drawable.ic_inner_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_inner,
        bgMusicRes = "bg_music_inner.ogg",
        moveSoundP1Res = "p1_move.ogg",
        moveSoundP2Res = "p2_move.ogg",
        toucheSoundRes = "touche_hit.ogg",
        boardCellDark = Color(0xFF311B92).copy(alpha = 0.3f),
        boardCellLight = Color(0xFFB2EBF2).copy(alpha = 0.15f),
        auraP1Color = Color(0xFFE1BEE7),
        auraP2Color = Color(0xFF80CBC4),
        containerColor = Color(0xFF1A237E),
        textColor = Color(0xFFE0F7FA),
        uiAccentColor = Color(0xFF82B1FF),
        victoryQuotes = innerWins,
        defeatQuotes = innerLoses,
        fontFamily = FontFamily.Monospace
    )
    val Vive2024Paris = GameTheme(
        id = "vive_2024_paris",
        displayName = "Vive 2024 Paris",
        backgroundRes = Res.drawable.bg_vive2024,
        boardBackgroundRes = Res.drawable.bg_board_vive2024,
        pieceP1Res = Res.drawable.ic_vive2024_p1,
        pieceP2Res = Res.drawable.ic_vive2024_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_vive2024,
        bgMusicRes = "bg_music_vive2024.ogg",
        moveSoundP1Res = "p1_move.ogg",
        moveSoundP2Res = "p2_move.ogg",
        toucheSoundRes = "touche_hit.ogg",
        boardCellDark = Color(0xFF0D1B3E).copy(alpha = 0.45f),
        boardCellLight = Color(0xFFE0F7FA).copy(alpha = 0.12f),
        auraP1Color = Color(0xFF9D50BB),
        auraP2Color = Color(0xFF00E5FF),
        containerColor = Color(0xFF0A122A),
        textColor = Color(0xFFF0F4F8),
        uiAccentColor = Color(0xFFFFD700),
        victoryQuotes = parisWins,
        defeatQuotes = parisLoses,
        fontFamily = FontFamily.Monospace
    )
    val eSportVision = GameTheme(
        id = "esport_vision",
        displayName = "eSport Vision",
        backgroundRes = Res.drawable.bg_esport,
        boardBackgroundRes = Res.drawable.bg_board_esport,
        pieceP1Res = Res.drawable.ic_esport_p1,
        pieceP2Res = Res.drawable.ic_esport_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_esport,
        bgMusicRes = "bg_music_esport.ogg",
        moveSoundP1Res = "p1_move.ogg",
        moveSoundP2Res = "p2_move.ogg",
        toucheSoundRes = "touche_hit.ogg",
        boardCellDark = Color(0xFF0A0A0C).copy(alpha = 0.85f),
        boardCellLight = Color(0xFF1A1A20).copy(alpha = 0.25f),
        auraP1Color = Color(0xFF00E5FF),
        auraP2Color = Color(0xFFFF0055),
        containerColor = Color(0xFF111114),
        textColor = Color(0xFFF0F4F8),
        uiAccentColor = Color(0xFF00FFAA),
        victoryQuotes = esportWins,
        defeatQuotes = esportLoses,
        fontFamily = FontFamily.Monospace
    )

    val WinterPagoda = GameTheme(
        id = "winter_pagoda",
        displayName = "Winter Pagoda",
        backgroundRes = Res.drawable.bg_winter,
        boardBackgroundRes = Res.drawable.bg_board_winter,
        pieceP1Res = Res.drawable.ic_winter_p1,
        pieceP2Res = Res.drawable.ic_winter_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_board_winter,
        bgMusicRes = "bg_music_winter.ogg",
        moveSoundP1Res = "p1_move.ogg",
        moveSoundP2Res = "p2_move.ogg",
        toucheSoundRes = "touche_hit.ogg",
        boardCellDark = Color(0xFFB0B0C5).copy(alpha = 0.3f),
        boardCellLight = Color.White.copy(alpha = 0.15f),
        auraP1Color = Color(0x3080DEEA),
        auraP2Color = Color(0x1AD32F2F),
        containerColor = Color(0xFFECEFF1),
        textColor = Color(0xFFE1F5FE),
        uiAccentColor = Color(0xFF18FFFF),
        victoryQuotes = winterWins,
        defeatQuotes = winterLoses,
        fontFamily = FontFamily.SansSerif
    )
    val DaturaBlossom = GameTheme(
        id = "datura_blossom",
        displayName = "Datura Blossom",
        backgroundRes = Res.drawable.bg_datura,
        boardBackgroundRes = Res.drawable.bg_board_datura,
        pieceP1Res = Res.drawable.ic_datura_p1,
        pieceP2Res = Res.drawable.ic_datura_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_board_datura,
        bgMusicRes = "bg_music_datura.ogg",
        moveSoundP1Res = "p1_move_datura.ogg",
        moveSoundP2Res = "p2_move_datura.ogg",
        toucheSoundRes = "touche_hit_datura.ogg",
        boardCellDark = Color(0xFF4A148C).copy(alpha = 0.2f),
        boardCellLight = Color(0xFFCE93D8).copy(alpha = 0.15f),
        auraP1Color = Color(0xFFEA80FC),
        auraP2Color = Color(0xFF00E5FF),
        containerColor = Color(0xFFF3E5F5),
        textColor = Color(0xFFF3E5F5),
        uiAccentColor = Color(0xFFE040FB),
        victoryQuotes = daturaWins,
        defeatQuotes = daturaLoses,
        fontFamily = FontFamily.SansSerif
    )
    val MayanFresco = GameTheme(
        id = "mayan_fresco",
        displayName = "Mayan Fresco",
        backgroundRes = Res.drawable.bg_mayan,
        boardBackgroundRes = Res.drawable.bg_board_mayan,
        pieceP1Res = Res.drawable.ic_mayan_p1,
        pieceP2Res = Res.drawable.ic_mayan_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_board_mayan,
        bgMusicRes = "bg_music_mayan.ogg",
        moveSoundP1Res = "p1_move_jungle.ogg",
        moveSoundP2Res = "p2_move_jungle.ogg",
        toucheSoundRes = "touche_hit_jungle.ogg",
        boardCellDark = Color(0x36000000),
        boardCellLight = Color(0x24FFFFFF),
        auraP1Color = Color(0xFFFFB300),
        auraP2Color = Color(0xFF8D6E63),
        containerColor = Color(0xFFF1F8E9),
        textColor = Color(0xFFE8F5E9),
        uiAccentColor = Color(0xFFFF7043),
        victoryQuotes = mayanWins,
        defeatQuotes = mayanLoses,
        fontFamily = FontFamily.SansSerif
    )
    val AfricanSiesta = GameTheme(
        id = "african_siesta",
        displayName = "African Siesta",
        backgroundRes = Res.drawable.bg_african,
        boardBackgroundRes = Res.drawable.bg_board_african,
        pieceP1Res = Res.drawable.ic_african_p1,
        pieceP2Res = Res.drawable.ic_african_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_board_african,
        bgMusicRes = "bg_music_african.ogg",
        moveSoundP1Res = "p1_move_jungle.ogg",
        moveSoundP2Res = "p2_move_jungle.ogg",
        toucheSoundRes = "touche_hit_jungle.ogg",
        boardCellDark = Color(0x36000000),
        boardCellLight = Color(0x24FFFFFF),
        auraP1Color = Color(0xFFFFB300),
        auraP2Color = Color(0xFF8D6E63),
        containerColor = Color(0xFFF1F8E9),
        textColor = Color(0xFFFFF3E0),
        uiAccentColor = Color(0xFFFF7043),
        victoryQuotes = africanWins,
        defeatQuotes = africanLoses,
        fontFamily = FontFamily.SansSerif
    )
    val Tournament = GameTheme(
        id = "tournament",
        displayName = "Hall of Legends",
        backgroundRes = Res.drawable.bg_board_destreza,
        boardBackgroundRes = Res.drawable.bg_board_destreza,
        pieceP1Res = Res.drawable.ic_destreza_p1,
        pieceP2Res = Res.drawable.ic_destreza_p2,
        toucheStarRes = Res.drawable.ic_touche_star,
        previewImageRes = Res.drawable.bg_board_destreza,
        bgMusicRes = "main_menu_music.ogg",
        moveSoundP1Res = "p1_move_destreza.ogg",
        moveSoundP2Res = "p2_move_destreza.ogg",
        toucheSoundRes = "touche_hit_destreza.ogg",
        boardCellDark = Color(0xFF0F141E).copy(alpha = 0.6f),
        boardCellLight = Color.White.copy(alpha = 0.05f),
        auraP1Color = Color(0xFFD4AF37),
        auraP2Color = Color(0xFF4A5570),
        containerColor = Color(0xFF0A0C10),
        textColor = Color.White,
        uiAccentColor = Color(0xFFD4AF37),
        victoryQuotes = emptyList(),
        defeatQuotes = emptyList(),
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
