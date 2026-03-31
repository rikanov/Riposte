package hu.riposte.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager(context: Context) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(audioAttributes)
        .build()

    // Hangminták betöltése (Figyelj a res/raw fájlnevekre!)
    private val soundP1 = soundPool.load(context, R.raw.p1_move, 1)
    private val soundP2 = soundPool.load(context, R.raw.p2_move, 1)
    private val soundTouche = soundPool.load(context, R.raw.touche_hit, 1)

    fun playMove(playerId: Int) {
        val soundId = if (playerId == 1) soundP1 else soundP2
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    fun playTouche() {
        soundPool.play(soundTouche, 1f, 1f, 1, 0, 1f)
    }
    private val soundWin = soundPool.load(context, R.raw.victory, 1)
    private val soundLose = soundPool.load(context, R.raw.defeat, 1)

    fun playWin() { soundPool.play(soundWin, 1f, 1f, 1, 0, 1f) }
    fun playLose() { soundPool.play(soundLose, 1f, 1f, 1, 0, 1f) }
}