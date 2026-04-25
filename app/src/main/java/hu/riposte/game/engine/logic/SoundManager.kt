package hu.riposte.game.engine.logic

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import hu.riposte.game.R
import hu.riposte.game.ui.theme.GameTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class SoundManager(private val context: Context) {

    // --- GLOBÁLIS ZENEI ÁLLAPOT (Minden képernyő ezt osztja meg) ---
    companion object {
        private var mediaPlayer: MediaPlayer? = null
        private var currentMusicRes: Int? = null
        private var crossfadeJob: Job? = null
        private val soundScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        var isMusicGloballyEnabled: Boolean = true

        fun releaseMusic() {
            crossfadeJob?.cancel()
            soundScope.cancel()
            try {
                mediaPlayer?.stop()
            } catch (e: Exception) { }
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // --- 1. HANGEFFEKTEK (SoundPool - Ez marad példányszintű) ---
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(audioAttributes)
        .build()

    private var soundP1 = -1
    private var soundP2 = -1
    private var soundTouche = -1

    private val soundWin = soundPool.load(context, R.raw.victory, 1)
    private val soundLose = soundPool.load(context, R.raw.defeat, 1)
    private val soundClick = soundPool.load(context, R.raw.menu_click, 1)
    private val soundToggleOn = soundPool.load(context, R.raw.toggle_on, 1)
    private val soundToggleOff = soundPool.load(context, R.raw.toggle_off, 1)
    private val sliceSoundId = soundPool.load(context, R.raw.p2_move, 1) // Helyettesítsd a fájlod nevével!
    var isMusicEnabled: Boolean
        get() = isMusicGloballyEnabled
        set(value) {
            isMusicGloballyEnabled = value
            if (value) resumeMusic() else pauseMusic()
        }

    fun loadThemeSFX(theme: GameTheme) {
        if (soundP1 != -1) soundPool.unload(soundP1)
        if (soundP2 != -1) soundPool.unload(soundP2)
        if (soundTouche != -1) soundPool.unload(soundTouche)

        soundP1 = soundPool.load(context, theme.moveSoundP1Res, 1)
        soundP2 = soundPool.load(context, theme.moveSoundP2Res, 1)
        soundTouche = soundPool.load(context, theme.toucheSoundRes, 1)
    }

    fun playClick() { soundPool.play(soundClick, 0.6f, 0.6f, 0, 0, 1f) }
    fun playToggle(isOn: Boolean) { soundPool.play(if (isOn) soundToggleOn else soundToggleOff, 0.6f, 0.6f, 0, 0, 1f) }
    fun playMove(playerId: Int) {
        val soundId = if (playerId == 1) soundP1 else soundP2
        if (soundId != -1) soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }
    fun playTouche() { if (soundTouche != -1) soundPool.play(soundTouche, 1f, 1f, 1, 0, 1f) }
    fun playWin() { soundPool.play(soundWin, 1f, 1f, 1, 0, 1f) }
    fun playLose() { soundPool.play(soundLose, 1f, 1f, 1, 0, 1f) }

    // --- 2. ALÁFESTŐ ZENE (Mostantól a Companion-t használja) ---

    fun startMusic() {
        if (!isMusicGloballyEnabled) return
        if (mediaPlayer == null && currentMusicRes != null) {
            mediaPlayer = MediaPlayer.create(context, currentMusicRes!!)?.apply {
                isLooping = true
                setVolume(0.4f, 0.4f)
            }
        }
        mediaPlayer?.start()
    }

    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun resumeMusic() {
        if (isMusicGloballyEnabled && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun playThemeMusic(musicResId: Int) {
        if (!isMusicGloballyEnabled) {
            currentMusicRes = musicResId
            return
        }
        // Ha UGYANAZ a zene szólna (pl. Menüből mész a Bajnokságba és ugyanaz az alap téma), ne indítsa újra!
        if (currentMusicRes == musicResId && mediaPlayer?.isPlaying == true) return

        crossfadeJob?.cancel()
        crossfadeJob = soundScope.launch { crossfadeTo(musicResId) }
    }

    private suspend fun crossfadeTo(newMusicResId: Int) {
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

        currentMusicRes = newMusicResId
        mediaPlayer = MediaPlayer.create(context, newMusicResId)?.apply {
            isLooping = true
            setVolume(0f, 0f)
            start()
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
    // A memóriatakarításnál csak a hangeffekteket (SoundPool) dobjuk el, a zenét HAGYJUK SZÓLNI!
    fun release() {
        soundPool.release()
    }
}