package hu.riposte.game

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "riposte_settings")

class SettingsManager(private val context: Context) {

    companion object {
        val MUSIC_KEY = booleanPreferencesKey("music_enabled")
        val SFX_KEY = booleanPreferencesKey("sfx_enabled")
        val HAPTIC_KEY = booleanPreferencesKey("haptic_enabled")
        val VISUAL_ASSISTS_KEY = booleanPreferencesKey("visual_assists_enabled")
        val RIPOSTE_KEY = booleanPreferencesKey("riposte_enabled")
        val THEME_ID_KEY = stringPreferencesKey("theme_id")
        val HAS_SEEN_TUTORIAL_KEY = booleanPreferencesKey("has_seen_tutorial")

        // --- 1. LÉPÉS: ÚJ KULCS AZ ÉJSZAKAI MÓDHOZ ---
        val NIGHT_MODE_KEY = booleanPreferencesKey("night_mode_enabled")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            musicEnabled = preferences[MUSIC_KEY] ?: true,
            sfxEnabled = preferences[SFX_KEY] ?: true,
            hapticEnabled = preferences[HAPTIC_KEY] ?: true,
            visualAssistsEnabled = preferences[VISUAL_ASSISTS_KEY] ?: true,
            riposteEnabled = preferences[RIPOSTE_KEY] ?: true,
            themeId = preferences[THEME_ID_KEY] ?: "abstract_sunrise",
            hasSeenTutorial = preferences[HAS_SEEN_TUTORIAL_KEY] ?: false,

            // --- 2. LÉPÉS: BEOLVASÁS ---
            nightModeEnabled = preferences[NIGHT_MODE_KEY] ?: false
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_KEY] = settings.musicEnabled
            preferences[SFX_KEY] = settings.sfxEnabled
            preferences[HAPTIC_KEY] = settings.hapticEnabled
            preferences[VISUAL_ASSISTS_KEY] = settings.visualAssistsEnabled // Ezt a biztonság kedvéért pótoltam!
            preferences[RIPOSTE_KEY] = settings.riposteEnabled
            preferences[THEME_ID_KEY] = settings.themeId
            preferences[HAS_SEEN_TUTORIAL_KEY] = settings.hasSeenTutorial

            // --- 3. LÉPÉS: MENTÉS ---
            preferences[NIGHT_MODE_KEY] = settings.nightModeEnabled
        }
    }
}