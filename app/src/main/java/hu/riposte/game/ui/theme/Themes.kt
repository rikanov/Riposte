package hu.riposte.game.ui.theme

import androidx.compose.ui.graphics.Color
import hu.riposte.game.R

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
        boardCellDark = Color(0x26000000), // Nagyon halvány, áttetsző fekete tinta a sötét mezőkhöz
        boardCellLight = Color(0x1AFFFFFF), // Halvány pergamen-fehér a világos mezőkhöz
        auraP1Color = Color(0xFFB71C1C), // Mély vörös (Sangre)
        auraP2Color = Color(0xFF455A64), // Hideg acélkék (Acero)
        containerColor = Color(0xFF3E2723), // Sötét fa/bőr a UI paneleknek
        textColor = Color(0xFFFFE082), // Meleg, aranyozott pergamen szín a szövegeknek
        uiAccentColor = Color(0xFFD4AF37) // Arany (Brass) a gomboknak
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
        bgMusicRes = R.raw.bg_music_la_maupin, // Or a dramatic classical track if you add one
        moveSoundP1Res = R.raw.p1_move_la_maupin,
        moveSoundP2Res = R.raw.p2_move_la_maupin,
        toucheSoundRes = R.raw.touche_hit_la_maupin,
        boardCellDark = Color(0x668B2245), // Élénk kárminvörös / Bársonyos meggy
        boardCellLight = Color(0x33FFD1DC), // Lágy púderrózsaszín (Face powder pink)
        auraP1Color = Color(0xFFE91E63), // Ragyogó rubin/rózsa a P1 bábunak
        auraP2Color = Color(0xFF90A4AE), // Hideg, polírozott acél a P2 tőrnek (Tökéletes kontraszt!)
        containerColor = Color(0xFF2A1318), // Sötét szilva/mahagóni (Melegebb, mint a fekete)
        textColor = Color(0xFFFFF0F5), // "Lavender Blush" - lágy, meleg gyöngyfehér
        uiAccentColor = Color(0xFFE58D9A) // Ragyogó Rozéarany (Rose Gold)
    )
    val Highwayman = GameTheme(
        id = "highwayman",
        displayName = "The Highwayman",
        backgroundRes = R.drawable.bg_highwayman, // Az éjszakai erdei út a hintóval
        boardBackgroundRes = R.drawable.bg_board_highwayman,
        pieceP1Res = R.drawable.ic_highwayman_p1, // A királyi kitüntetés (Katona)
        pieceP2Res = R.drawable.ic_highwayman_p2, // A telihold (Bandita)
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_highwayman,
        bgMusicRes = R.raw.bg_music_highwayman, // A Lyria-val generált kocsmai/barokk zene
        moveSoundP1Res = R.raw.p1_move_destreza, // Használhatjuk a Destreza fémes hangjait
        moveSoundP2Res = R.raw.p2_move_destreza,
        toucheSoundRes = R.raw.touche_hit_destreza,

        // --- SZÍNEK ---
        boardCellDark = Color(0xFF141B12).copy(alpha = 0.6f), // Mély, "mocsári" erdőzöld/fekete
        boardCellLight = Color(0xFFB0BEC5).copy(alpha = 0.15f), // Ködös szürke (Moonlight Mist)
        auraP1Color = Color(0xFFD32F2F), // Királyi vörös (Scarlet Red - a zubbony színe)
        auraP2Color = Color(0xFF90A4AE), // Hideg ezüst (Moonlight Silver)
        containerColor = Color(0xFF2D241E), // Sötét, lakkozott tölgyfa vagy öreg bőr
        textColor = Color(0xFFE0E0E0), // Patinás ezüst/pergamen fehér
        uiAccentColor = Color(0xFFCD7F32) // Antik bronz/réz (a kitüntetés színe)
    )
    val VenetianMasquerade = GameTheme(
        id = "venetian_masquerade",
        displayName = "Venetian Masquerade",
        backgroundRes = R.drawable.bg_venice,
        boardBackgroundRes = R.drawable.bg_board_venice, // A freskós borítókép
        pieceP1Res = R.drawable.ic_venice_p1, // Az arany maszk korong
        pieceP2Res = R.drawable.ic_venice_p2, // A pestisdoktor korong
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_venice,
        bgMusicRes = R.raw.bg_music_venice,
        moveSoundP1Res = R.raw.p1_move_destreza,
        moveSoundP2Res = R.raw.p2_move_destreza,
        toucheSoundRes = R.raw.touche_hit_destreza,

        // --- SZÍNEK (Velencei Pompához) ---
        boardCellDark = Color(0xFF4B0082).copy(alpha = 0.3f), // Mély királyi lila (Indigo)
        boardCellLight = Color(0xFFFFD700).copy(alpha = 0.1f), // Arany csillogású mezők
        auraP1Color = Color(0xFFFFD700), // Ragyogó Arany (Gold)
        auraP2Color = Color(0xFF00C853), // Smaragdzöld (Emerald - az intrika színe)
        containerColor = Color(0xFF310A31), // Mély bordó/lila bársony panelek
        textColor = Color(0xFFFFF8E1), // Porcelán fehér/krém
        uiAccentColor = Color(0xFFFFAB40) // Meleg borostyán/arany akcentus
    )
    // 2.A. OLYMPIC CARBON
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
        previewImageRes = R.drawable.bg_inner,
        bgMusicRes = R.raw.bg_music_inner,
        moveSoundP1Res = R.raw.p1_move,
        moveSoundP2Res = R.raw.p2_move,
        toucheSoundRes = R.raw.touche_hit,
        boardCellDark = Color(0xFF311B92).copy(alpha = 0.3f), // Mély lila
        boardCellLight = Color(0xFFB2EBF2).copy(alpha = 0.15f), // Halvány jégkék
        auraP1Color = Color(0xFFE1BEE7), // Spirituális lila
        auraP2Color = Color(0xFF80CBC4), // Mentás zöld
        containerColor = Color(0xFF1A237E), // Éjkék panelek
        textColor = Color(0xFFE0F7FA),
        uiAccentColor = Color(0xFF82B1FF) // Világos kék akcentus
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
        uiAccentColor = Color(0xFFD4AF37)
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

        // --- COLORS (Dark Mode & Neon) ---
        boardCellDark = Color(0xFF0A0A0C).copy(alpha = 0.85f),
        boardCellLight = Color(0xFF1A1A20).copy(alpha = 0.65f),
        auraP1Color = Color(0xFF00E5FF),
        auraP2Color = Color(0xFFFF0055),
        containerColor = Color(0xFF111114),
        textColor = Color(0xFFF0F4F8),
        uiAccentColor = Color(0xFF00FFAA)
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
        boardCellDark = Color(0x36000000), // Nagyon halvány, áttetsző fekete tinta a sötét mezőkhöz
        boardCellLight = Color(0x24FFFFFF),
        auraP1Color = Color(0xFFFFB300),
        auraP2Color = Color(0xFF8D6E63),
        containerColor = Color(0xFFF1F8E9),
        textColor = Color(0xFF2E7D32),
        uiAccentColor = Color(0xFFF4511E)
    )

    // 6. TOURNAMENT THEME (Hall of Legends)
    val Tournament = GameTheme(
        id = "tournament",
        displayName = "Hall of Legends",
        backgroundRes = R.drawable.bg_board_destreza, // Placeholder or specific background
        boardBackgroundRes = R.drawable.bg_board_destreza,
        pieceP1Res = R.drawable.ic_destreza_p1,
        pieceP2Res = R.drawable.ic_destreza_p2,
        toucheStarRes = R.drawable.ic_touche_star,
        previewImageRes = R.drawable.bg_board_destreza,
        bgMusicRes = R.raw.main_menu_music, // Match Main Menu music as requested
        moveSoundP1Res = R.raw.p1_move_destreza,
        moveSoundP2Res = R.raw.p2_move_destreza,
        toucheSoundRes = R.raw.touche_hit_destreza,
        boardCellDark = Color(0xFF0F141E).copy(alpha = 0.6f),
        boardCellLight = Color.White.copy(alpha = 0.05f),
        auraP1Color = Color(0xFFD4AF37), // Gold
        auraP2Color = Color(0xFF4A5570), // Steel
        containerColor = Color(0xFF0A0C10),
        textColor = Color.White,
        uiAccentColor = Color(0xFFD4AF37)
    )

    // CATEGORIZED COLLECTIONS
    val classicThemes = listOf(Destreza, LaMaupin, Highwayman, VenetianMasquerade )
    val modernThemes = listOf(OlympicCarbon, InnerSpirit, Vive2024Paris, eSportVision)
    val zenThemes = listOf(DaturaBlossom, WinterPagoda, MayanFresco, AfricanSiesta)

    val allThemes = classicThemes + modernThemes + zenThemes

    fun getThemeById(id: String): GameTheme {
        return allThemes.find { it.id == id } ?: Destreza
    }
}
