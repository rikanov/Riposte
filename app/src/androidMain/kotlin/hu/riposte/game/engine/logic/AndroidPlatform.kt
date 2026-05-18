package hu.riposte.game.engine.logic

import android.content.Context

internal var androidContext: Context? = null

fun initKmp(context: Context) {
    androidContext = context.applicationContext
}

actual fun createSoundManager(): SoundManager {
    return SoundManager()
}

actual fun createSettingsManager(): SettingsManager {
    return SettingsManager()
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
