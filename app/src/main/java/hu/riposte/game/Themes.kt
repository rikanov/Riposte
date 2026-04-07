package hu.riposte.game

import androidx.compose.ui.graphics.Color

object ThemeRegistry {

    // 1. ABSTRACT SUNRISE
    val CloudySunrise = GameTheme(
        id = "cloudy_sunrise",
        displayName = "Cloudy Sunrise",
        backgroundRes = R.drawable.bg_abstract,
        pieceP1Res = R.drawable.ic_piece_p1,
        pieceP2Res = R.drawable.ic_piece_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.prev_sunrise,
        bgMusicRes = R.raw.bg_music,
        moveSoundP1Res = R.raw.p1_move,
        moveSoundP2Res = R.raw.p2_move,
        toucheSoundRes = R.raw.touche_hit,
        boardCellDark = Color.White.copy(alpha = 0.05f),
        boardCellLight = Color.White.copy(alpha = 0.12f),
        auraP1Color = Color(0xFF00E5FF),
        auraP2Color = Color(0xFFE53935),
        containerColor = Color(0xFFE3F2FD),
        textColor = Color(0xFF1976D2),
        uiAccentColor = Color(0xFFFF6B6B)
    )

    // 2. JUNGLE MORNING
    val JungleMorning = GameTheme(
        id = "jungle_morning",
        displayName = "Jungle's Dawn",
        backgroundRes = R.drawable.bg_jungle,
        pieceP1Res = R.drawable.ic_jungle_p1,
        pieceP2Res = R.drawable.ic_jungle_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_jungle,
        bgMusicRes = R.raw.bg_music_jungle,
        moveSoundP1Res = R.raw.p1_move_jungle,
        moveSoundP2Res = R.raw.p2_move_jungle,
        toucheSoundRes = R.raw.touche_hit_jungle,
        boardCellDark = Color(0xFF1B5E20).copy(alpha = 0.2f),
        boardCellLight = Color(0xFF81C784).copy(alpha = 0.15f),
        auraP1Color = Color(0xFFFFB300),
        auraP2Color = Color(0xFF8D6E63),
        containerColor = Color(0xFFF1F8E9),
        textColor = Color(0xFF2E7D32),
        uiAccentColor = Color(0xFFF4511E)
    )
    // 3. WINTER PAGODA
    val WinterPagoda = GameTheme(
        id = "winter_pagoda",
        displayName = "Winter Pagoda",
        backgroundRes = R.drawable.bg_winter, // TODO: Pótold az assetet
        pieceP1Res = R.drawable.ic_winter_p1,
        pieceP2Res = R.drawable.ic_winter_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_winter,
        bgMusicRes = R.raw.bg_music_winter,
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

    // 4. DATURA BLOSSOM
    val DaturaBlossom = GameTheme(
        id = "datura_blossom",
        displayName = "Datura Blossom",
        backgroundRes = R.drawable.bg_datura,
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
        pieceP1Res = R.drawable.ic_mayan_p1,
        pieceP2Res = R.drawable.ic_mayan_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_mayan,
        bgMusicRes = R.raw.bg_music_mayan,
        moveSoundP1Res = R.raw.p1_move_jungle,
        moveSoundP2Res = R.raw.p2_move_jungle,
        toucheSoundRes = R.raw.touche_hit_jungle,
        boardCellDark = Color(0xFF1B5E20).copy(alpha = 0.2f),
        boardCellLight = Color(0xFF81C784).copy(alpha = 0.15f),
        auraP1Color = Color(0xFFFFB300),
        auraP2Color = Color(0xFF8D6E63),
        containerColor = Color(0xFFF1F8E9),
        textColor = Color(0xFF2E7D32),
        uiAccentColor = Color(0xFFF4511E)
    )
    val RandomTheme = CloudySunrise.copy(
        id = "random",
        displayName = "Shuffle",
        previewImageRes = R.drawable.bg_abstract // TODO: Egy kérdőjeles ikon
    )

    val allThemes = listOf(CloudySunrise, JungleMorning, WinterPagoda, DaturaBlossom, MayanFresco, RandomTheme)

    fun getThemeById(id: String): GameTheme {
        if (id == "random") {
            return allThemes.filter { it.id != "random" }.random()
        }
        return allThemes.find { it.id == id } ?: CloudySunrise
    }
}