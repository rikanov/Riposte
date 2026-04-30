package hu.riposte.game.engine.logic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    // Bajnokság progresszió
    val tournamentRank: Int = 20,
    val tournamentHighest: Int = 20,
    val tournamentDefending: Boolean = false,
    val tournamentMatchHistory: String = "",
    val lastTipTime: Long = 0L,
    val highestRating: Int = 0,
    // Bajnokság mentési hely (Tournament Save Slot)
    val hasSavedTournamentMatch: Boolean = false,
    val savedTourneyPlayerScore: Int = 0,
    val savedTourneyOppScore: Int = 0,
    val savedTourneyPlayerTime: Long = 180000L,
    val savedTourneyOppTime: Long = 180000L,
    val savedTourneyBoardStr: String = "",
    val savedTourneyPiecesStr: String = "",
    val savedTourneyCurrentPlayer: Int = 2,
    val savedTourneyAfterTouche: Boolean = false,

    // Napi tippek perzisztencia
    val usefulTipIndex: Int = 0,
    val loreTipIndex: Int = 0,
    val lastTipWasUseful: Boolean = false
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "riposte_settings")

class SettingsManager(private val context: Context) {

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
        val HIGHEST_RATING_KEY = intPreferencesKey("highest_rating")
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

        // Új kulcsok a tippekhez
        val USEFUL_TIP_INDEX_KEY = intPreferencesKey("useful_tip_index")
        val LORE_TIP_INDEX_KEY = intPreferencesKey("lore_tip_index")
        val LAST_TIP_WAS_USEFUL_KEY = booleanPreferencesKey("last_tip_was_useful")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
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
            lastTipTime = preferences[LAST_TIP_TIME_KEY] ?: 0L,

            hasSavedTournamentMatch = preferences[HAS_SAVED_TOURNEY_KEY] ?: false,
            savedTourneyPlayerScore = preferences[SAVED_TOURNEY_P_SCORE] ?: 0,
            savedTourneyOppScore = preferences[SAVED_TOURNEY_O_SCORE] ?: 0,
            savedTourneyPlayerTime = preferences[SAVED_TOURNEY_P_TIME] ?: 180000L,
            savedTourneyOppTime = preferences[SAVED_TOURNEY_O_TIME] ?: 180000L,
            savedTourneyBoardStr = preferences[SAVED_TOURNEY_BOARD] ?: "",
            savedTourneyPiecesStr = preferences[SAVED_TOURNEY_PIECES] ?: "",
            savedTourneyCurrentPlayer = preferences[SAVED_TOURNEY_PLAYER] ?: 2,
            savedTourneyAfterTouche = preferences[SAVED_TOURNEY_AFTER_TOUCHE] ?: false,

            usefulTipIndex = preferences[USEFUL_TIP_INDEX_KEY] ?: 0,
            loreTipIndex = preferences[LORE_TIP_INDEX_KEY] ?: 0,
            lastTipWasUseful = preferences[LAST_TIP_WAS_USEFUL_KEY] ?: false
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
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
            preferences[LAST_TIP_TIME_KEY] = settings.lastTipTime

            preferences[HAS_SAVED_TOURNEY_KEY] = settings.hasSavedTournamentMatch
            preferences[SAVED_TOURNEY_P_SCORE] = settings.savedTourneyPlayerScore
            preferences[SAVED_TOURNEY_O_SCORE] = settings.savedTourneyOppScore
            preferences[SAVED_TOURNEY_P_TIME] = settings.savedTourneyPlayerTime
            preferences[SAVED_TOURNEY_O_TIME] = settings.savedTourneyOppTime
            preferences[SAVED_TOURNEY_BOARD] = settings.savedTourneyBoardStr
            preferences[SAVED_TOURNEY_PIECES] = settings.savedTourneyPiecesStr
            preferences[SAVED_TOURNEY_PLAYER] = settings.savedTourneyCurrentPlayer
            preferences[SAVED_TOURNEY_AFTER_TOUCHE] = settings.savedTourneyAfterTouche

            preferences[USEFUL_TIP_INDEX_KEY] = settings.usefulTipIndex
            preferences[LORE_TIP_INDEX_KEY] = settings.loreTipIndex
            preferences[LAST_TIP_WAS_USEFUL_KEY] = settings.lastTipWasUseful
        }
    }
}
