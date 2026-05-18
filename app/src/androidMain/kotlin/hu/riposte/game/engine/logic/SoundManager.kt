package hu.riposte.game.engine.logic

import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

actual class SoundManager {
    private val context = androidContext ?: error("KMP not initialized")

    private var mediaPlayer: MediaPlayer? = null
    private var currentMusicPath: String? = null
    private var crossfadeJob: Job? = null
    private val soundScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    actual var isMusicGloballyEnabled: Boolean = true
    private var isAppPaused: Boolean = false

    // --- 1. HANGEFFEKTEK (SoundPool) ---
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(audioAttributes)
        .build()

    private var soundP1 = -1
    private var soundP2 = -1
    private var soundTouche = -1

    private fun getAssetPath(fileName: String): String = "composeResources/riposte.app.generated.resources/files/$fileName"

    private fun loadFromAsset(fileName: String): Int {
        return try {
            val afd = context.assets.openFd(getAssetPath(fileName))
            soundPool.load(afd, 1)
        } catch (e: Exception) {
            -1
        }
    }

    private val soundWin = loadFromAsset("victory.ogg")
    private val soundLose = loadFromAsset("defeat.ogg")
    private val soundClick = loadFromAsset("menu_click.ogg")
    private val soundToggleOn = loadFromAsset("toggle_on.ogg")
    private val soundToggleOff = loadFromAsset("toggle_off.ogg")

    actual var isMusicEnabled: Boolean
        get() = isMusicGloballyEnabled
        set(value) {
            isMusicGloballyEnabled = value
            if (value) resumeMusic() else pauseMusic()
        }

    actual fun loadThemeSFX(theme: ThemeSFX) {
        if (soundP1 != -1) soundPool.unload(soundP1)
        if (soundP2 != -1) soundPool.unload(soundP2)
        if (soundTouche != -1) soundPool.unload(soundTouche)

        soundP1 = loadFromAsset(theme.moveSoundP1)
        soundP2 = loadFromAsset(theme.moveSoundP2)
        soundTouche = loadFromAsset(theme.toucheSound)
    }

    actual fun playClick() { if (soundClick != -1) soundPool.play(soundClick, 0.6f, 0.6f, 0, 0, 1f) }
    actual fun playToggle(isOn: Boolean) { 
        val soundId = if (isOn) soundToggleOn else soundToggleOff
        if (soundId != -1) soundPool.play(soundId, 0.6f, 0.6f, 0, 0, 1f) 
    }
    actual fun playMove(playerId: Int) {
        val soundId = if (playerId == 1) soundP1 else soundP2
        if (soundId != -1) soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }
    actual fun playTouche() { if (soundTouche != -1) soundPool.play(soundTouche, 1f, 1f, 1, 0, 1f) }
    actual fun playWin() { if (soundWin != -1) soundPool.play(soundWin, 1f, 1f, 1, 0, 1f) }
    actual fun playLose() { if (soundLose != -1) soundPool.play(soundLose, 1f, 1f, 1, 0, 1f) }

    // --- 2. ALÁFESTŐ ZENE ---

    actual fun startMusic() {
        if (!isMusicGloballyEnabled) return
        if (mediaPlayer == null && currentMusicPath != null) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    val afd = context.assets.openFd(getAssetPath(currentMusicPath!!))
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    isLooping = true
                    setVolume(0.4f, 0.4f)
                    prepare()
                }
            } catch (e: Exception) {
                mediaPlayer = null
            }
        }
        if (!isAppPaused) {
            mediaPlayer?.start()
        }
    }

    actual fun pauseMusic() {
        isAppPaused = true
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {}
    }

    actual fun resumeMusic() {
        isAppPaused = false
        if (!isMusicGloballyEnabled) return
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                }
            } else if (currentMusicPath != null) {
                startMusic()
            }
        } catch (e: Exception) {
            try {
                mediaPlayer?.release()
            } catch (ex: Exception) {}
            mediaPlayer = null
            startMusic()
        }
    }

    actual fun playThemeMusic(musicFileName: String) {
        if (!isMusicGloballyEnabled) {
            currentMusicPath = musicFileName
            return
        }
        if (currentMusicPath == musicFileName && mediaPlayer?.isPlaying == true) return

        crossfadeJob?.cancel()
        crossfadeJob = soundScope.launch { crossfadeTo(musicFileName) }
    }

    private suspend fun crossfadeTo(newMusicFileName: String) {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    val fadeSteps = 10
                    for (i in fadeSteps downTo 0) {
                        if (!coroutineContext.isActive) break
                        val volume = (i.toFloat() / fadeSteps) * 0.4f
                        player.setVolume(volume, volume)
                        delay(50)
                    }
                }
                player.stop()
            } catch (e: Exception) { } finally { player.release() }
        }
        mediaPlayer = null

        if (!coroutineContext.isActive) return

        currentMusicPath = newMusicFileName
        try {
            mediaPlayer = MediaPlayer().apply {
                val afd = context.assets.openFd(getAssetPath(newMusicFileName))
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                setVolume(0f, 0f)
                prepare()
                if (!isAppPaused) {
                    start()
                }
            }
        } catch (e: Exception) {
            mediaPlayer = null
            return
        }

        mediaPlayer?.let { player ->
            try {
                val fadeSteps = 10
                for (i in 0..fadeSteps) {
                    if (!coroutineContext.isActive) break
                    val volume = (i.toFloat() / fadeSteps) * 0.4f
                    player.setVolume(volume, volume)
                    delay(50)
                }
            } catch (e: Exception) { }
        }
    }

    actual fun releaseMusic() {
        crossfadeJob?.cancel()
        soundScope.cancel()
        try {
            mediaPlayer?.stop()
        } catch (e: Exception) { }
        mediaPlayer?.release()
        mediaPlayer = null
    }

    actual fun release() {
        soundPool.release()
    }
}
