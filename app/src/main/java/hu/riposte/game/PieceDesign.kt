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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Canvas
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

@Composable
fun PieceDesign(
    owner: Int,
    pieceId: Int,
    isPulsing: Boolean = false,
    isAnimationEnabled: Boolean = true,
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

    // 1. BASE SIZE & PULSE
    val scale by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = 0.75f, targetValue = 0.95f,
            animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(0.8f) }
    }

    // 2. BREATHING AURA
    val baseAuraPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse, initialStartOffset = StartOffset(timeOffset)),
        label = "aura"
    )
    val auraPhase = if (isAnimationEnabled) baseAuraPhase else 0.5f

    // 3. HOVERING UP AND DOWN (A bábu lebeg, az árnyék a földön marad!)
    val baseHoverY by infiniteTransition.animateFloat(
        initialValue = -7.5f, targetValue = 7.5f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse, initialStartOffset = StartOffset(timeOffset)),
        label = "hoverY"
    )
    val hoverY = if (isAnimationEnabled) baseHoverY else 0f

    // 4. SUBTLE WOBBLE
    val baseHoverRotation by infiniteTransition.animateFloat(
        initialValue = -5.5f, targetValue = 5.5f,
        animationSpec = infiniteRepeatable(tween(2700, easing = EaseInOutSine), RepeatMode.Reverse, initialStartOffset = StartOffset(timeOffset)),
        label = "wobble"
    )
    val hoverRotation = if (isAnimationEnabled) baseHoverRotation else 0f

    // --- 2.5D LIGHT & SHADOW MATHEMATICS ---
    var finalLightAlpha = 0.3f
    var lightOffsetX = 0f
    var lightOffsetY = -0.5f

    var shadowOffsetX = 0f
    var shadowOffsetY = 0f
    var shadowStretch = 1f
    var shadowAlpha = 0f

    if (starX != null && starY != null) {
        val dx = starX - pieceX
        val dy = starY - pieceY
        val distance = sqrt(dx * dx + dy * dy)
        val maxDistance = sqrt(boardWidth * boardWidth + boardHeight * boardHeight)

        val distanceRatio = (distance / maxDistance).coerceIn(0f, 1f)
        val angleToStar = atan2(dy, dx).toFloat()
        val normalizedPulse = ((starPulseValue - 0.8f) * 5f).coerceIn(0f, 1f)

        // Fény intenzitás
        val distanceFade = 1f - distanceRatio
        val baseIntensity = distanceFade.coerceIn(0.2f, 0.9f)
        finalLightAlpha = baseIntensity * (0.3f + 0.7f * normalizedPulse)

        // Fény eltolása a bábun
        val offsetFactor = (distanceRatio * 2.2f).coerceIn(0f, 1.2f)
        lightOffsetX = cos(angleToStar) * offsetFactor
        lightOffsetY = sin(angleToStar) * offsetFactor

        // --- SHADOW LOGIC ---
        // Az árnyék pontosan ellentétes a fényforrással
        val shadowAngle = angleToStar + PI.toFloat()

        // Ha távolabb van, jobban megnyúlik az árnyék
        shadowStretch = 1f + (distanceRatio * 1.5f)

        // SOKKAL sötétebb árnyék: 95%-os feketéről indul!
        shadowAlpha = (0.95f - (distanceRatio * 0.3f)) * (0.8f + 0.2f * normalizedPulse)

        // Az árnyék elcsúszása (max 50 pixel)
        val maxShadowOffsetPx = 50f
        shadowOffsetX = cos(shadowAngle) * (distanceRatio * maxShadowOffsetPx)
        shadowOffsetY = sin(shadowAngle) * (distanceRatio * maxShadowOffsetPx)
    }

    // MAIN CONTAINER: szétválasztjuk az árnyékot és a lebegő bábut!
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        // --- LAYER 0: STATIONARY DROP SHADOW (A talajon marad) ---
        if (starX != null && starY != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // A bábu méretéhez igazítjuk a sugárt
                val shadowRadius = size.width * 0.35f * scale

                val shadowCenter = Offset(
                    x = (size.width / 2f) + shadowOffsetX,
                    // Egy kis extra Y eltolás, hogy alapból is a bábu talpa "alatt" legyen vizuálisan
                    y = (size.height / 2f) + shadowOffsetY + (size.height * 0.15f)
                )

                // Varázslat: Összenyomjuk a vásznat Y tengelyen, így a kör színátmenetből tökéletes elipszis lesz!
                withTransform({
                    translate(left = shadowCenter.x, top = shadowCenter.y)
                    scale(scaleX = 1f, scaleY = 0.35f * shadowStretch) // 0.35 = lapítás aránya
                }) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = shadowAlpha), // Fekete mag
                                Color.Transparent                      // Lágyan eltűnő szél
                            ),
                            radius = shadowRadius
                        ),
                        radius = shadowRadius
                    )
                }
            }
        }

        // --- LAYER 1 & 2: HOVERING PIECE (A lebegő konténer) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = hoverY // Ez emeli fel a bábut a talajról (árnyékról)!
                    rotationZ = hoverRotation
                },
            contentAlignment = Alignment.Center
        ) {
            // AURA
            Box(
                modifier = Modifier.fillMaxSize().drawBehind {
                    val currentRadius = size.minDimension * (0.25f + (0.3f * auraPhase))
                    val currentAlpha = 0.7f - (0.5f * auraPhase)
                    val brush = Brush.radialGradient(
                        0.0f to auraColor.copy(alpha = currentAlpha),
                        0.6f to auraColor.copy(alpha = currentAlpha * 0.7f),
                        1.0f to Color.Transparent,
                        radius = currentRadius
                    )
                    drawCircle(brush = brush, radius = currentRadius)
                }
            )

            // PIECE & HIGHLIGHT
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

                        val lightBrush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = finalLightAlpha),
                                Color.Transparent
                            ),
                            center = lightCenter,
                            radius = r * 1.3f
                        )

                        drawCircle(brush = lightBrush, blendMode = BlendMode.Overlay)
                    }
            )
        }
    }
}