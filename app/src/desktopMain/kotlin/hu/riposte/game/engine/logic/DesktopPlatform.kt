package hu.riposte.game.engine.logic

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import riposte.app.generated.resources.Res
import java.io.ByteArrayInputStream
import java.io.File
import com.google.gson.Gson
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import kotlin.math.log10

actual class SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var bgMusicClip: Clip? = null
    private var currentMusicPath: String? = null

    private var soundP1: Clip? = null
    private var soundP2: Clip? = null
    private var soundTouche: Clip? = null

    private val staticSounds = mutableMapOf<String, Clip>()

    actual var isMusicGloballyEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) pauseMusic() else resumeMusic()
        }
    actual var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) pauseMusic() else resumeMusic()
        }

    init {
        loadStaticSound("menu_click.ogg", "click")
        loadStaticSound("toggle_on.ogg", "toggle_on")
        loadStaticSound("toggle_off.ogg", "toggle_off")
        loadStaticSound("victory.ogg", "win")
        loadStaticSound("defeat.ogg", "lose")
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun loadStaticSound(fileName: String, key: String) {
        scope.launch {
            try {
                val bytes = Res.readBytes("files/$fileName")
                val clip = loadClip(bytes)
                if (clip != null) {
                    staticSounds[key] = clip
                }
            } catch (e: Exception) {
                // Silently ignore
            }
        }
    }

    private fun loadClip(bytes: ByteArray): Clip? {
        return try {
            val inputStream = java.io.BufferedInputStream(ByteArrayInputStream(bytes))
            val originalStream = AudioSystem.getAudioInputStream(inputStream)
            val baseFormat = originalStream.format

            val sampleRate = if (baseFormat.sampleRate > 0) baseFormat.sampleRate else 44100f
            val channels = if (baseFormat.channels > 0) baseFormat.channels else 2

            // Stage 1: Decode to native PCM
            val decodedFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                16,
                channels,
                channels * 2,
                sampleRate,
                false
            )
            val pcmStream = AudioSystem.getAudioInputStream(decodedFormat, originalStream)
            var pcmBytes = pcmStream.readBytes()
            pcmStream.close()
            var currentFormat = decodedFormat

            // Stage 2: Manual Mono to Stereo Upmix (ALSA strictness)
            if (currentFormat.channels == 1) {
                val stereoBytes = ByteArray(pcmBytes.size * 2)
                var i = 0
                while (i < pcmBytes.size - 1) {
                    val b1 = pcmBytes[i]
                    val b2 = pcmBytes[i + 1]
                    val dest = i * 2
                    stereoBytes[dest] = b1
                    stereoBytes[dest + 1] = b2
                    stereoBytes[dest + 2] = b1
                    stereoBytes[dest + 3] = b2
                    i += 2
                }
                pcmBytes = stereoBytes
                currentFormat = AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sampleRate,
                    16,
                    2,
                    4,
                    sampleRate,
                    false
                )
            }

            val clip = AudioSystem.getClip()
            
            // Stage 3: Attempt open, with Resampling Fallback
            try {
                clip.open(currentFormat, pcmBytes, 0, pcmBytes.size)
            } catch (e: Exception) {
                // If ALSA rejects the sample rate (e.g., 48kHz), force resample to 44.1kHz
                val targetFormat = AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    44100f, 16, 2, 4, 44100f, false
                )
                val sourceStream = javax.sound.sampled.AudioInputStream(
                    ByteArrayInputStream(pcmBytes),
                    currentFormat,
                    (pcmBytes.size / currentFormat.frameSize).toLong()
                )
                if (AudioSystem.isConversionSupported(targetFormat, currentFormat)) {
                    val resampledStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream)
                    val resampledBytes = resampledStream.readBytes()
                    clip.open(targetFormat, resampledBytes, 0, resampledBytes.size)
                } else {
                    throw e
                }
            }
            clip
        } catch (e: Exception) {
            System.err.println("CRITICAL AUDIO ERROR: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    actual fun loadThemeSFX(theme: ThemeSFX) {
        scope.launch {
            soundP1?.close()
            soundP2?.close()
            soundTouche?.close()

            soundP1 = try {
                loadClip(Res.readBytes("files/${theme.moveSoundP1}"))
            } catch (e: Exception) {
                null
            } ?: loadClip(Res.readBytes("files/p1_move.ogg"))

            soundP2 = try {
                loadClip(Res.readBytes("files/${theme.moveSoundP2}"))
            } catch (e: Exception) {
                null
            } ?: loadClip(Res.readBytes("files/p2_move.ogg"))

            soundTouche = try {
                loadClip(Res.readBytes("files/${theme.toucheSound}"))
            } catch (e: Exception) {
                null
            } ?: loadClip(Res.readBytes("files/touche_hit.ogg"))
        }
    }

    private fun playClip(clip: Clip?, volume: Float = 1.0f) {
        clip?.let {
            try {
                if (it.isRunning) it.stop()
                it.framePosition = 0
                val gainControl = it.getControl(FloatControl.Type.MASTER_GAIN) as? FloatControl
                gainControl?.let { gc ->
                    val dB = (log10(volume.toDouble().coerceIn(0.0001, 1.0)) * 20.0).toFloat()
                    gc.value = dB
                }
                it.start()
            } catch (e: Exception) {
                println("Error playing clip: ${e.message}")
            }
        }
    }

    actual fun playClick() { playClip(staticSounds["click"], 0.6f) }
    actual fun playToggle(isOn: Boolean) { playClip(staticSounds[if (isOn) "toggle_on" else "toggle_off"], 0.6f) }
    actual fun playMove(playerId: Int) { playClip(if (playerId == 1) soundP1 else soundP2, 1.0f) }
    actual fun playTouche() { playClip(soundTouche, 1.0f) }
    actual fun playWin() { playClip(staticSounds["win"], 1.0f) }
    actual fun playLose() { playClip(staticSounds["lose"], 1.0f) }

    actual fun startMusic() {
        if (isMusicEnabled && isMusicGloballyEnabled) {
            bgMusicClip?.loop(Clip.LOOP_CONTINUOUSLY)
        }
    }

    actual fun pauseMusic() {
        bgMusicClip?.stop()
    }

    actual fun resumeMusic() {
        if (isMusicEnabled && isMusicGloballyEnabled) {
            bgMusicClip?.loop(Clip.LOOP_CONTINUOUSLY)
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    actual fun playThemeMusic(musicFileName: String) {
        if (currentMusicPath == musicFileName) return
        currentMusicPath = musicFileName

        scope.launch {
            try {
                bgMusicClip?.stop()
                bgMusicClip?.close()

                val bytes = Res.readBytes("files/$musicFileName")
                bgMusicClip = loadClip(bytes)

                if (isMusicEnabled && isMusicGloballyEnabled) {
                    bgMusicClip?.loop(Clip.LOOP_CONTINUOUSLY)
                }
            } catch (e: Exception) {
                println("Failed to play theme music $musicFileName: ${e.message}")
            }
        }
    }

    actual fun releaseMusic() {
        bgMusicClip?.stop()
        bgMusicClip?.close()
    }

    actual fun release() {
        scope.cancel()
        releaseMusic()
        staticSounds.values.forEach { it.close() }
        soundP1?.close()
        soundP2?.close()
        soundTouche?.close()
    }
}

actual class SettingsManager {
    private val prefsFile = File(System.getProperty("user.home"), ".riposte/settings.json")
    private val gson = Gson()
    private val _settingsFlow = MutableStateFlow(loadSettings())

    actual val settingsFlow: Flow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        return try {
            if (prefsFile.exists()) {
                val json = prefsFile.readText()
                gson.fromJson(json, AppSettings::class.java) ?: AppSettings()
            } else {
                AppSettings()
            }
        } catch (e: Exception) {
            println("Failed to load settings: ${e.message}")
            AppSettings()
        }
    }

    actual suspend fun updateSettings(settings: AppSettings) {
        _settingsFlow.value = settings
        withContext(Dispatchers.IO) {
            try {
                prefsFile.parentFile?.mkdirs()
                prefsFile.writeText(gson.toJson(settings))
            } catch (e: Exception) {
                println("Failed to save settings: ${e.message}")
            }
        }
    }
}

private val sharedSoundManager by lazy { SoundManager() }
private val sharedSettingsManager by lazy { SettingsManager() }

actual fun createSoundManager(): SoundManager = sharedSoundManager
actual fun createSettingsManager(): SettingsManager = sharedSettingsManager

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
