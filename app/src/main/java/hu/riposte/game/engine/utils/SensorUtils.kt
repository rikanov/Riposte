package hu.riposte.game.engine.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberDeviceTilt(): State<Offset> {
    val context = LocalContext.current
    val tilt = remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // A sima gyorsulásmérőt használjuk, mert ez minden eszközön stabilan működik
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            // "Low-pass filter" változók a vajas, sima mozgáshoz
            private var smoothedX = 0f
            private var smoothedY = 0f
            private val alpha = 0.15f // Kisebb érték = lassabb, de sokkal simább követés

            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        // Android Accelerometer:
                        // X: balra/jobbra dőlés
                        // Y: előre/hátra dőlés
                        val rawX = it.values[0]
                        val rawY = it.values[1]

                        smoothedX = alpha * rawX + (1f - alpha) * smoothedX
                        smoothedY = alpha * rawY + (1f - alpha) * smoothedY

                        tilt.value = Offset(smoothedX, smoothedY)
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // SENSOR_DELAY_GAME: Kifejezetten 60 FPS-es frissítéshez optimalizálva
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    return tilt
}