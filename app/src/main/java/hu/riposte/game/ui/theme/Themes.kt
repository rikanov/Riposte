package hu.riposte.game.ui.theme

import androidx.compose.ui.graphics.Color
import hu.riposte.game.R

object ThemeRegistry {

    val Destreza = GameTheme(
        id = "destreza",
        displayName = "Manuscript",
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
        boardCellDark = Color(0x26000000), // Nagyon halvány, áttetsző fekete tinta a sötét mezőkhöz
        boardCellLight = Color(0x1AFFFFFF), // Halvány pergamen-fehér a világos mezőkhöz
        auraP1Color = Color(0xFFB71C1C), // Mély vörös (Sangre)
        auraP2Color = Color(0xFF455A64), // Hideg acélkék (Acero)
        containerColor = Color(0xFF3E2723), // Sötét fa/bőr a UI paneleknek
        textColor = Color(0xFFFFE082), // Meleg, aranyozott pergamen szín a szövegeknek
        uiAccentColor = Color(0xFFD4AF37) // Arany (Brass) a gomboknak
    )
    // 2.A. OLYMPIC CARBON
    val OlypicCarbpn = GameTheme(
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
        auraP1Color = Color(0xFF80DEEA), // Jégkék
        auraP2Color = Color(0xFFD32F2F), // Pagoda piros
        containerColor = Color(0xFFECEFF1),
        textColor = Color(0xFF455A64),
        uiAccentColor = Color(0xFF00BCD4) // Élénk jég-cián
    )
    // 2.B. INNER SPIRIT
    val InnerSpirit = GameTheme(
        id = "inner_spirit",
        displayName = "Inner Spirit",
        backgroundRes = R.drawable.bg_inner,
        boardBackgroundRes = R.drawable.bg_board_inner,
        pieceP1Res = R.drawable.ic_inner_p1,
        pieceP2Res = R.drawable.ic_inner_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_board_inner,
        bgMusicRes = R.raw.bg_music_inner,
        moveSoundP1Res = R.raw.p1_move,
        moveSoundP2Res = R.raw.p2_move,
        toucheSoundRes = R.raw.touche_hit,
        boardCellDark = Color(0xFFB0BEC5).copy(alpha = 0.2f),
        boardCellLight = Color.White.copy(alpha = 0.15f),
        auraP1Color = Color(0xFF80DEEA), // Jégkék
        auraP2Color = Color(0xFFD32F2F), // Pagoda piros
        containerColor = Color(0xFFECEFF1),
        textColor = Color(0xFF455A64),
        uiAccentColor = Color(0xFF00BCD4) // Élénk jég-cián
    )
    // 3. WINTER PAGODA
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
        textColor = Color(0xFF455A64),
        uiAccentColor = Color(0xFF00BCD4) // Élénk jég-cián
    )

    // 4. DATURA BLOSSOM
    val DaturaBlossom = GameTheme(
        id = "datura_blossom",
        displayName = "Datura Blossom",
        backgroundRes = R.drawable.bg_datura,
        boardBackgroundRes = R.drawable.bg_datura,
        pieceP1Res = R.drawable.ic_datura_p1,
        pieceP2Res = R.drawable.ic_datura_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_datura,
        bgMusicRes = R.raw.bg_music_datura,
        moveSoundP1Res = R.raw.p1_move,
        moveSoundP2Res = R.raw.p2_move,
        toucheSoundRes = R.raw.touche_hit,
        boardCellDark = Color(0xFF4A148C).copy(alpha = 0.2f),
        boardCellLight = Color(0xFFCE93D8).copy(alpha = 0.15f),
        auraP1Color = Color(0xFFEA80FC), // Misztikus lila
        auraP2Color = Color(0xFF00E5FF), // Kontrasztos cián
        containerColor = Color(0xFFF3E5F5),
        textColor = Color(0xFF4A148C),
        uiAccentColor = Color(0xFFAB47BC) // Mély lila accent
    )
    // 5. MAYAN FRESCO
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
        boardCellDark = Color(0x36000000), // Nagyon halvány, áttetsző fekete tinta a sötét mezőkhöz
        boardCellLight = Color(0x24FFFFFF),
        auraP1Color = Color(0xFFFFB300),
        auraP2Color = Color(0xFF8D6E63),
        containerColor = Color(0xFFF1F8E9),
        textColor = Color(0xFF2E7D32),
        uiAccentColor = Color(0xFFF4511E)
    )
    val allThemes = listOf(Destreza, OlypicCarbpn, InnerSpirit, WinterPagoda, DaturaBlossom, MayanFresco)

    fun getThemeById(id: String): GameTheme {
        if (id == "random") {
            return allThemes.filter { it.id != "random" }.random()
        }
        return allThemes.find { it.id == id } ?: Destreza
    }
}