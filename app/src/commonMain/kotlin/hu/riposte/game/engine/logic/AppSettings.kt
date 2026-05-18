package hu.riposte.game.engine.logic

data class AppSettings(
    val playerName: String = "CHALLENGER",
    val playerTitle: String = "Novice",
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val visualAssistsEnabled: Boolean = true,
    val riposteEnabled: Boolean = true,
    val themeId: String = "abstract_sunrise",
    val hasSeenTutorial: Boolean = false,
    val nightModeEnabled: Boolean = false,
    val showDailyTips: Boolean = true,

    val tournamentRank: Int = 20,
    val tournamentHighest: Int = 20,
    val tournamentDefending: Boolean = false,
    val tournamentMatchHistory: String = "",
    val tournamentScoreHistory: String = "",
    val lastTipTime: Long = 0L,
    val highestRating: Int = 0,
    val peakStreak: Int = 0,

    val hasSavedTournamentMatch: Boolean = false,
    val savedTourneyPlayerScore: Int = 0,
    val savedTourneyOppScore: Int = 0,
    val savedTourneyPlayerTime: Long = 180000L,
    val savedTourneyOppTime: Long = 180000L,
    val savedTourneyBoardStr: String = "",
    val savedTourneyPiecesStr: String = "",
    val savedTourneyCurrentPlayer: Int = 2,
    val savedTourneyAfterTouche: Boolean = false,

    val lastInfoTab: Int = 0,
    val infoUsefulIndex: Int = 0,
    val infoStrategyIndex: Int = 0,
    val infoLoreIndex: Int = 0,
    val infoLegendIndex: Int = 0,
    val infoHistoryIndex: Int = 0,

    val recentThreats: String = ""
)

data class ThemeSFX(
    val moveSoundP1: String,
    val moveSoundP2: String,
    val toucheSound: String
)
