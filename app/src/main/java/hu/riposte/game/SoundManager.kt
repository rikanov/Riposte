package hu.riposte.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

class SoundManager(private val context: Context) {

    // --- COROUTINE A ZENEI ÁTTŰNÉSHEZ ---
    private val soundScope = CoroutineScope(Dispatchers.Main + Job())
    private var currentMusicRes: Int? = null
    private var crossfadeJob: Job? = null

    // --- 1. HANGEFFEKTEK (SoundPool) ---
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(audioAttributes)
        .build()

    // JAVÍTVA: Ezek VAR-ok lettek, hogy a témaváltáskor felülírhassuk őket!
    private var soundP1 = -1
    private var soundP2 = -1
    private var soundTouche = -1

    // Ezek maradnak fixek, mert a menühang nem témafüggő
    private val soundWin = soundPool.load(context, R.raw.victory, 1)
    private val soundLose = soundPool.load(context, R.raw.defeat, 1)
    private val soundClick = soundPool.load(context, R.raw.menu_click, 1)
    private val soundToggleOn = soundPool.load(context, R.raw.toggle_on, 1)
    private val soundToggleOff = soundPool.load(context, R.raw.toggle_off, 1)

    // --- ÚJ: HANGEFFEKTEK DINAMIKUS BETÖLTÉSE ---
    fun loadThemeSFX(theme: GameTheme) {
        // Ha már voltak betöltve hangok, dobjuk el őket a memóriából
        if (soundP1 != -1) soundPool.unload(soundP1)
        if (soundP2 != -1) soundPool.unload(soundP2)
        if (soundTouche != -1) soundPool.unload(soundTouche)

        // Töltsük be az új téma saját hangjait
        soundP1 = soundPool.load(context, theme.moveSoundP1Res, 1)
        soundP2 = soundPool.load(context, theme.moveSoundP2Res, 1)
        soundTouche = soundPool.load(context, theme.toucheSoundRes, 1)
    }

    fun playClick() {
        soundPool.play(soundClick, 0.6f, 0.6f, 0, 0, 1f)
    }

    fun playToggle(isOn: Boolean) {
        val s = if (isOn) soundToggleOn else soundToggleOff
        soundPool.play(s, 0.6f, 0.6f, 0, 0, 1f)
    }

    fun playMove(playerId: Int) {
        val soundId = if (playerId == 1) soundP1 else soundP2
        if (soundId != -1) soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    fun playTouche() {
        if (soundTouche != -1) soundPool.play(soundTouche, 1f, 1f, 1, 0, 1f)
    }

    fun playWin() { soundPool.play(soundWin, 1f, 1f, 1, 0, 1f) }
    fun playLose() { soundPool.play(soundLose, 1f, 1f, 1, 0, 1f) }


    // --- 2. ALÁFESTŐ ZENE (MediaPlayer) ---
    private var mediaPlayer: MediaPlayer? = null

    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) resumeMusic() else pauseMusic()
        }

    fun startMusic() {
        if (!isMusicEnabled) return

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
        if (isMusicEnabled && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    // --- TÉMA ZENE LOGIKA (Keresztáttűnéssel) ---

    fun playThemeMusic(musicResId: Int) {
        if (!isMusicEnabled) {
            currentMusicRes = musicResId
            return
        }
        if (currentMusicRes == musicResId && mediaPlayer?.isPlaying == true) return

        crossfadeJob?.cancel() // Megszakítjuk a korábbi áttűnést
        crossfadeJob = soundScope.launch {
            crossfadeTo(musicResId)
        }
    }

    fun previewThemeMusic(musicResId: Int) {
        if (!isMusicEnabled) return
        if (currentMusicRes == musicResId) return

        crossfadeJob?.cancel() // Megszakítjuk a korábbi áttűnést
        crossfadeJob = soundScope.launch {
            crossfadeTo(musicResId)
        }
    }

    // A mágia: elhalkítja a régit, és felhangosítja az újat BIZTONSÁGOSAN
    private suspend fun crossfadeTo(newMusicResId: Int) {
        // 1. Ha már szól valami, halkítsuk le
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    val fadeSteps = 10
                    for (i in fadeSteps downTo 0) {
                        // Ha a szálat kilőtték, azonnal kiszállunk a ciklusból
                        if (!coroutineContext.isActive) break
                        val volume = (i.toFloat() / fadeSteps) * 0.4f
                        player.setVolume(volume, volume)
                        delay(50)
                    }
                }
                player.stop()
            } catch (e: Exception) {
                // Ha az Android hisztizik, hagyjuk figyelmen kívül
            } finally {
                player.release()
            }
        }
        mediaPlayer = null

        // Ha a coroutine-t közben megszakították (lapozott), ne indítsuk el az újat
        if (!coroutineContext.isActive) return

        // 2. Indítsuk el az új zenét néma hangerővel
        currentMusicRes = newMusicResId
        mediaPlayer = MediaPlayer.create(context, newMusicResId)?.apply {
            isLooping = true
            setVolume(0f, 0f)
            start()
        }

        // 3. Hangosítsuk fel 0.4f-re
        mediaPlayer?.let { player ->
            try {
                val fadeSteps = 10
                for (i in 0..fadeSteps) {
                    if (!coroutineContext.isActive) break
                    val volume = (i.toFloat() / fadeSteps) * 0.4f
                    player.setVolume(volume, volume)
                    delay(50)
                }
            } catch (e: Exception) {
                // Biztonsági elkapás
            }
        }
    }

    // --- 3. MEMÓRIA FELSZABADÍTÁSA ---
    fun release() {
        crossfadeJob?.cancel()
        soundScope.cancel()
        soundPool.release()

        try {
            mediaPlayer?.stop()
        } catch (e: Exception) { }
        mediaPlayer?.release()
        mediaPlayer = null
    }
}