package hu.riposte.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VolumetricLightOrgan(
    accentColor: Color,
    modifier: Modifier = Modifier,
    centerYRatio: Float = 0.5f // Alapból középen van
) {
    // Réteg 1: Alap arany/témaszín (Leggyorsabb)
    GodRaysVFX(
        rayColor = accentColor,
        rotationDuration = 40000,
        pulseDuration = 3000,
        pulseDelay = 0,
        centerYRatio = centerYRatio,
        modifier = modifier
    )

    // Réteg 2: Meleg borostyán (Lassabb, fázis eltolva)
    GodRaysVFX(
        rayColor = Color(0xFFFFD180),
        rotationDuration = 62000,
        pulseDuration = 4500,
        pulseDelay = 1500,
        startRotation = 45f,
        centerYRatio = centerYRatio,
        modifier = modifier
    )

    // Réteg 3: Jeges kék/fehér (Visszafelé forog, lassú lüktetés)
    GodRaysVFX(
        rayColor = Color(0xFFE0F7FA),
        rotationDuration = 85000,
        pulseDuration = 6000,
        pulseDelay = 3000,
        reverse = true,
        centerYRatio = centerYRatio,
        modifier = modifier
    )

    // Réteg 4: Tiszta fehér (Nagyon lassú, vékonyabb sugarak az élénkségért)
    GodRaysVFX(
        rayColor = Color.White,
        rotationDuration = 120000,
        pulseDuration = 8000,
        pulseDelay = 4000,
        rayWidth = 0.10f,
        centerYRatio = centerYRatio,
        modifier = modifier
    )
}

@Composable
private fun GodRaysVFX(
    rayColor: Color,
    rotationDuration: Int,
    pulseDuration: Int,
    pulseDelay: Int = 0,
    startRotation: Float = 0f,
    rayWidth: Float = 0.24f,
    reverse: Boolean = false,
    centerYRatio: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "godrays_vfx_$rotationDuration")

    // Forgás animáció
    val rotation by infiniteTransition.animateFloat(
        initialValue = if (reverse) 360f else 0f,
        targetValue = if (reverse) 0f else 360f,
        animationSpec = infiniteRepeatable(tween(rotationDuration, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    // Fáziseltolt Alpha lüktetés
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.01f,
        targetValue = 0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration, delayMillis = pulseDelay, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * centerYRatio
        val radius = size.maxDimension * 1.5f

        rotate(degrees = rotation + startRotation, pivot = Offset(cx, cy)) {
            for (i in 0 until 12) {
                val angleStart = (i * 30f) * (PI / 180f).toFloat()
                val angleEnd = angleStart + rayWidth

                val path = Path().apply {
                    moveTo(cx, cy)
                    lineTo(cx + cos(angleStart) * radius, cy + sin(angleStart) * radius)
                    lineTo(cx + cos(angleEnd) * radius, cy + sin(angleEnd) * radius)
                    close()
                }

                drawPath(
                    path = path,
                    brush = Brush.radialGradient(
                        colors = listOf(rayColor.copy(alpha = alpha), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = radius * 0.7f
                    ),
                    blendMode = BlendMode.Screen
                )
            }
        }
    }
}