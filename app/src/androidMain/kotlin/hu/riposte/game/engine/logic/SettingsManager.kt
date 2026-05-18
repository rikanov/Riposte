package hu.riposte.game.engine.logic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "riposte_settings")

actual class SettingsManager {
    private val context = androidContext ?: error("KMP not initialized")

    companion object {
        val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        val PLAYER_TITLE_KEY = stringPreferencesKey("player_title")

        val MUSIC_KEY = booleanPreferencesKey("music_enabled")
        val SFX_KEY = booleanPreferencesKey("sfx_enabled")
        val HAPTIC_KEY = booleanPreferencesKey("haptic_enabled")
        val VISUAL_ASSISTS_KEY = booleanPreferencesKey("visual_assists_enabled")
        val RIPOSTE_KEY = booleanPreferencesKey("riposte_enabled")
        val THEME_ID_KEY = stringPreferencesKey("theme_id")
        val HAS_SEEN_TUTORIAL_KEY = booleanPreferencesKey("has_seen_tutorial")
        val NIGHT_MODE_KEY = booleanPreferencesKey("night_mode_enabled")

        val SHOW_TIPS_KEY = booleanPreferencesKey("show_tips")
        val TOURNAMENT_RANK_KEY = intPreferencesKey("tournament_rank")
        val TOURNAMENT_HIGHEST_KEY = intPreferencesKey("tournament_highest")
        val TOURNAMENT_DEFENDING_KEY = booleanPreferencesKey("tournament_defending")
        val TOURNAMENT_HISTORY_KEY = stringPreferencesKey("tournament_history")
        val TOURNAMENT_SCORE_HISTORY_KEY = stringPreferencesKey("tournament_score_history") // <--- ÚJ KULCS AZ ADATBÁZISNAK
        val HIGHEST_RATING_KEY = intPreferencesKey("highest_rating")
        val PEAK_STREAK_KEY = intPreferencesKey("peak_streak")
        val LAST_TIP_TIME_KEY = longPreferencesKey("last_tip_time")

        val HAS_SAVED_TOURNEY_KEY = booleanPreferencesKey("has_saved_tourney")
        val SAVED_TOURNEY_P_SCORE = intPreferencesKey("saved_tourney_p_score")
        val SAVED_TOURNEY_O_SCORE = intPreferencesKey("saved_tourney_o_score")
        val SAVED_TOURNEY_P_TIME = longPreferencesKey("saved_tourney_p_time")
        val SAVED_TOURNEY_O_TIME = longPreferencesKey("saved_tourney_o_time")
        val SAVED_TOURNEY_BOARD = stringPreferencesKey("saved_tourney_board")
        val SAVED_TOURNEY_PIECES = stringPreferencesKey("saved_tourney_pieces")
        val SAVED_TOURNEY_PLAYER = intPreferencesKey("saved_tourney_player")
        val SAVED_TOURNEY_AFTER_TOUCHE = booleanPreferencesKey("saved_tourney_after_touche")

        val LAST_INFO_TAB_KEY = intPreferencesKey("last_info_tab")
        val INFO_USEFUL_IDX_KEY = intPreferencesKey("info_useful_idx")
        val INFO_STRATEGY_IDX_KEY = intPreferencesKey("info_strategy_idx")
        val INFO_LORE_IDX_KEY = intPreferencesKey("info_lore_idx")
        val INFO_LEGEND_IDX_KEY = intPreferencesKey("info_legend_idx")
        val INFO_HISTORY_IDX_KEY = intPreferencesKey("info_history_idx")
        val RECENT_THREATS_KEY = stringPreferencesKey("recent_threats")
    }

    actual val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            playerName = preferences[PLAYER_NAME_KEY] ?: "CHALLENGER",
            playerTitle = preferences[PLAYER_TITLE_KEY] ?: "Novice",

            musicEnabled = preferences[MUSIC_KEY] ?: true,
            sfxEnabled = preferences[SFX_KEY] ?: true,
            hapticEnabled = preferences[HAPTIC_KEY] ?: true,
            visualAssistsEnabled = preferences[VISUAL_ASSISTS_KEY] ?: true,
            riposteEnabled = preferences[RIPOSTE_KEY] ?: true,
            themeId = preferences[THEME_ID_KEY] ?: "abstract_sunrise",
            hasSeenTutorial = preferences[HAS_SEEN_TUTORIAL_KEY] ?: false,
            nightModeEnabled = preferences[NIGHT_MODE_KEY] ?: false,
            showDailyTips = preferences[SHOW_TIPS_KEY] ?: true,

            tournamentRank = preferences[TOURNAMENT_RANK_KEY] ?: 20,
            tournamentHighest = preferences[TOURNAMENT_HIGHEST_KEY] ?: 20,
            tournamentDefending = preferences[TOURNAMENT_DEFENDING_KEY] ?: false,
            tournamentMatchHistory = preferences[TOURNAMENT_HISTORY_KEY] ?: "",
            tournamentScoreHistory = preferences[TOURNAMENT_SCORE_HISTORY_KEY] ?: "", // <--- BEOLVASÁS!
            lastTipTime = preferences[LAST_TIP_TIME_KEY] ?: 0L,
            highestRating = preferences[HIGHEST_RATING_KEY] ?: 0,
            peakStreak = preferences[PEAK_STREAK_KEY] ?: 0,

            hasSavedTournamentMatch = preferences[HAS_SAVED_TOURNEY_KEY] ?: false,
            savedTourneyPlayerScore = preferences[SAVED_TOURNEY_P_SCORE] ?: 0,
            savedTourneyOppScore = preferences[SAVED_TOURNEY_O_SCORE] ?: 0,
            savedTourneyPlayerTime = preferences[SAVED_TOURNEY_P_TIME] ?: 180000L,
            savedTourneyOppTime = preferences[SAVED_TOURNEY_O_TIME] ?: 180000L,
            savedTourneyBoardStr = preferences[SAVED_TOURNEY_BOARD] ?: "",
            savedTourneyPiecesStr = preferences[SAVED_TOURNEY_PIECES] ?: "",
            savedTourneyCurrentPlayer = preferences[SAVED_TOURNEY_PLAYER] ?: 2,
            savedTourneyAfterTouche = preferences[SAVED_TOURNEY_AFTER_TOUCHE] ?: false,

            lastInfoTab = preferences[LAST_INFO_TAB_KEY] ?: 0,
            infoUsefulIndex = preferences[INFO_USEFUL_IDX_KEY] ?: 0,
            infoStrategyIndex = preferences[INFO_STRATEGY_IDX_KEY] ?: 0,
            infoLoreIndex = preferences[INFO_LORE_IDX_KEY] ?: 0,
            infoLegendIndex = preferences[INFO_LEGEND_IDX_KEY] ?: 0,
            infoHistoryIndex = preferences[INFO_HISTORY_IDX_KEY] ?: 0,

            recentThreats = preferences[RECENT_THREATS_KEY] ?: ""
        )
    }

    actual suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PLAYER_NAME_KEY] = settings.playerName
            preferences[PLAYER_TITLE_KEY] = settings.playerTitle

            preferences[MUSIC_KEY] = settings.musicEnabled
            preferences[SFX_KEY] = settings.sfxEnabled
            preferences[HAPTIC_KEY] = settings.hapticEnabled
            preferences[VISUAL_ASSISTS_KEY] = settings.visualAssistsEnabled
            preferences[RIPOSTE_KEY] = settings.riposteEnabled
            preferences[THEME_ID_KEY] = settings.themeId
            preferences[HAS_SEEN_TUTORIAL_KEY] = settings.hasSeenTutorial
            preferences[NIGHT_MODE_KEY] = settings.nightModeEnabled
            preferences[SHOW_TIPS_KEY] = settings.showDailyTips

            preferences[TOURNAMENT_RANK_KEY] = settings.tournamentRank
            preferences[TOURNAMENT_HIGHEST_KEY] = settings.tournamentHighest
            preferences[TOURNAMENT_DEFENDING_KEY] = settings.tournamentDefending
            preferences[TOURNAMENT_HISTORY_KEY] = settings.tournamentMatchHistory
            preferences[TOURNAMENT_SCORE_HISTORY_KEY] = settings.tournamentScoreHistory // <--- MENTÉS!
            preferences[LAST_TIP_TIME_KEY] = settings.lastTipTime
            preferences[HIGHEST_RATING_KEY] = settings.highestRating
            preferences[PEAK_STREAK_KEY] = settings.peakStreak

            preferences[HAS_SAVED_TOURNEY_KEY] = settings.hasSavedTournamentMatch
            preferences[SAVED_TOURNEY_P_SCORE] = settings.savedTourneyPlayerScore
            preferences[SAVED_TOURNEY_O_SCORE] = settings.savedTourneyOppScore
            preferences[SAVED_TOURNEY_P_TIME] = settings.savedTourneyPlayerTime
            preferences[SAVED_TOURNEY_O_TIME] = settings.savedTourneyOppTime
            preferences[SAVED_TOURNEY_BOARD] = settings.savedTourneyBoardStr
            preferences[SAVED_TOURNEY_PIECES] = settings.savedTourneyPiecesStr
            preferences[SAVED_TOURNEY_PLAYER] = settings.savedTourneyCurrentPlayer
            preferences[SAVED_TOURNEY_AFTER_TOUCHE] = settings.savedTourneyAfterTouche

            preferences[LAST_INFO_TAB_KEY] = settings.lastInfoTab
            preferences[INFO_USEFUL_IDX_KEY] = settings.infoUsefulIndex
            preferences[INFO_STRATEGY_IDX_KEY] = settings.infoStrategyIndex
            preferences[INFO_LORE_IDX_KEY] = settings.infoLoreIndex
            preferences[INFO_LEGEND_IDX_KEY] = settings.infoLegendIndex
            preferences[INFO_HISTORY_IDX_KEY] = settings.infoHistoryIndex

            preferences[RECENT_THREATS_KEY] = settings.recentThreats
        }
    }
}