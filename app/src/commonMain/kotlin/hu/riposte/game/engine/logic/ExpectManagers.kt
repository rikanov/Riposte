package hu.riposte.game.engine.logic

import kotlinx.coroutines.flow.Flow

expect class SoundManager {
    var isMusicGloballyEnabled: Boolean
    var isMusicEnabled: Boolean

    fun loadThemeSFX(theme: ThemeSFX)
    fun playClick()
    fun playToggle(isOn: Boolean)
    fun playMove(playerId: Int)
    fun playTouche()
    fun playWin()
    fun playLose()

    fun startMusic()
    fun pauseMusic()
    fun resumeMusic()
    fun playThemeMusic(musicFileName: String)
    fun releaseMusic()
    fun release()
}

expect class SettingsManager {
    val settingsFlow: Flow<AppSettings>
    suspend fun updateSettings(settings: AppSettings)
}

expect fun createSoundManager(): SoundManager
expect fun createSettingsManager(): SettingsManager
