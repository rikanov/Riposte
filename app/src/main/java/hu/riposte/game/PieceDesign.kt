package hu.riposte.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun PieceDesign(
    owner: Int,
    pieceId: Int,
    isPulsing: Boolean = false,
    isAnimationEnabled: Boolean = true, // <-- ÚJ PARAMÉTER (Alapértelmezetten true)
    pieceX: Float,
    pieceY: Float,
    starX: Float?,
    starY: Float?,
    boardWidth: Float,
    boardHeight: Float,
    starPulseValue: Float
) {
    val currentTheme = LocalGameTheme.current

    val imageRes = if (owner == 1) currentTheme.pieceP1Res else currentTheme.pieceP2Res
    val auraColor = if (owner == 1) currentTheme.auraP1Color else currentTheme.auraP2Color

    val infiniteTransition = rememberInfiniteTransition(label = "PieceAnim")
    val timeOffset = pieceId * 400

    // 1. BASE SIZE & PULSE (Ez marad, mert jelzi, hogy melyik bábura lehet rákattintani)
    val scale by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = 0.75f, targetValue = 0.95f,
            animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(0.8f) }
    }

    // 2. BREATHING AURA (Kiszámoljuk, de csak akkor használjuk, ha az animáció be van kapcsolva)
    val baseAuraPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse, initialStartOffset = StartOffset(timeOffset)),
        label = "aura"
    )
    val auraPhase = if (isAnimationEnabled) baseAuraPhase else 0.5f // Ha ki van kapcsolva, egy fix, köztes ragyogást kap

    // 3. HOVERING UP AND DOWN (Súlytalan lebegés)
    val baseHoverY by infiniteTransition.animateFloat(
        initialValue = -7.5f, targetValue = 7.5f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse, initialStartOffset = StartOffset(timeOffset)),
        label = "hoverY"
    )
    val hoverY = if (isAnimationEnabled) baseHoverY else 0f // Földbe gyökerezik, ha ki van kapcsolva!

    // 4. SUBTLE WOBBLE (Finom billegés a levegőben)
    val baseHoverRotation by infiniteTransition.animateFloat(
        initialValue = -5.5f, targetValue = 5.5f,
        animationSpec = infiniteRepeatable(tween(2700, easing = EaseInOutSine), RepeatMode.Reverse, initialStartOffset = StartOffset(timeOffset)),
        label = "wobble"
    )
    val hoverRotation = if (isAnimationEnabled) baseHoverRotation else 0f // Fixen áll, ha ki van kapcsolva!

    // --- 2.5D LIGHT MATHEMATICS (No Shadows, pure reflection) ---
    var finalLightAlpha = 0.3f
    var lightOffsetX = 0f
    var lightOffsetY = -0.5f

    if (starX != null && starY != null) {
        val dx = starX - pieceX
        val dy = starY - pieceY
        val distance = sqrt(dx * dx + dy * dy)
        val maxDistance = sqrt(boardWidth * boardWidth + boardHeight * boardHeight)

        val distanceRatio = (distance / maxDistance).coerceIn(0f, 1f)
        val angleToStar = atan2(dy, dx)
        val normalizedPulse = ((starPulseValue - 0.8f) * 5f).coerceIn(0f, 1f)

        // Light intensity logic (Syncs with star)
        val distanceFade = 1f - distanceRatio
        val baseIntensity = distanceFade.coerceIn(0.2f, 0.9f)
        finalLightAlpha = baseIntensity * (0.3f + 0.7f * normalizedPulse)

        // HIGHLIGHT OFFSET: Agresszívabb eltolás a bábu pereme felé!
        val offsetFactor = (distanceRatio * 2.2f).coerceIn(0f, 1.2f)
        lightOffsetX = cos(angleToStar) * offsetFactor
        lightOffsetY = sin(angleToStar) * offsetFactor
    }

    // MAIN CONTAINER (Minden együtt lebeg és mozog)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = hoverY // Dinamikusan 0 vagy animált
                rotationZ = hoverRotation // Dinamikusan 0 vagy animált
            },
        contentAlignment = Alignment.Center
    ) {

        // --- LAYER 1: AURA ---
        Box(
            modifier = Modifier.fillMaxSize().drawBehind {
                val currentRadius = size.minDimension * (0.25f + (0.3f * auraPhase))
                val currentAlpha = 0.7f - (0.5f * auraPhase)
                val brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    0.0f to auraColor.copy(alpha = currentAlpha),
                    0.6f to auraColor.copy(alpha = currentAlpha * 0.7f),
                    1.0f to Color.Transparent,
                    radius = currentRadius
                )
                drawCircle(brush = brush, radius = currentRadius)
            }
        )

        // --- LAYER 2: THE PIECE & DYNAMIC HIGHLIGHT ---
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Game Piece",
            modifier = Modifier
                .fillMaxSize(0.85f)
                .drawWithContent {
                    drawContent()

                    val r = size.minDimension / 2f
                    val lightCenter = Offset(
                        x = size.width / 2f + lightOffsetX * r,
                        y = size.height / 2f + lightOffsetY * r
                    )

                    val lightBrush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = finalLightAlpha),
                            Color.Transparent
                        ),
                        center = lightCenter,
                        radius = r * 1.3f // Széles, lágy fénysugár
                    )

                    drawCircle(brush = lightBrush, blendMode = BlendMode.Overlay)
                }
        )
    }
}