package hu.riposte.game.engine.logic

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class SoundManager {
    actual var isMusicGloballyEnabled: Boolean = false
    actual var isMusicEnabled: Boolean = false

    actual fun loadThemeSFX(theme: ThemeSFX) {}
    actual fun playClick() {}
    actual fun playToggle(isOn: Boolean) {}
    actual fun playMove(playerId: Int) {}
    actual fun playTouche() {}
    actual fun playWin() {}
    actual fun playLose() {}

    actual fun startMusic() {}
    actual fun pauseMusic() {}
    actual fun resumeMusic() {}
    actual fun playThemeMusic(musicResId: Int) {}
    actual fun releaseMusic() {}
    actual fun release() {}
}

actual class SettingsManager {
    actual val settingsFlow: Flow<AppSettings> = flowOf(AppSettings())
    actual suspend fun updateSettings(settings: AppSettings) {}
}

actual fun createSoundManager(): SoundManager = SoundManager()
actual fun createSettingsManager(): SettingsManager = SettingsManager()

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
