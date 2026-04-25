package hu.riposte.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import hu.riposte.game.ui.theme.LocalGameTheme

@Composable
fun PieceDesign(
    owner: Int,
    pieceId: Int,
    isPulsing: Boolean = false,
    isDragged: Boolean = false,
    isAnimationEnabled: Boolean = true,
    deviceTilt: Offset = Offset.Zero // ÚJ: Giroszkóp adatok!
) {
    val currentTheme = LocalGameTheme.current

    val imageRes = if (owner == 1) currentTheme.pieceP1Res else currentTheme.pieceP2Res
    val auraColor = if (owner == 1) currentTheme.auraP1Color else currentTheme.auraP2Color

    val infiniteTransition = rememberInfiniteTransition(label = "PieceAnim")
    val timeOffset = pieceId * 400

    // 1. ALAP MÉRET ÉS PULZÁLÁS
    val baseScale by if (isPulsing && isAnimationEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0.75f, targetValue = 0.95f,
            animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(0.85f) }
    }

    // 2. FELVEVÉS (DRAG) EFFEKT
    val dragScale by animateFloatAsState(
        targetValue = if (isDragged) 1.25f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "dragScale"
    )

    val dragLiftY by animateFloatAsState(
        targetValue = if (isDragged) -30f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "dragLift"
    )

    // 3. AURA
    val auraPhase by if (isAnimationEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse, initialStartOffset = StartOffset(timeOffset)),
            label = "aura"
        )
    } else {
        remember { mutableFloatStateOf(0.5f) }
    }

    // Kiszámoljuk az árnyék eltolódását a giroszkóp alapján
    // Ha a tábla balra dől (X pozitív), az árnyéknak jobbra kell csúsznia, hogy a bábu kiemelkedőnek hasson
    val shadowOffsetX = -deviceTilt.x * 6f
    val shadowOffsetY = deviceTilt.y * 6f

    // MAIN CONTAINER
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = baseScale * dragScale
                scaleY = baseScale * dragScale
                translationY = dragLiftY
            },
        contentAlignment = Alignment.Center
    ) {

        // --- 1. RÉTEG: A 2.5D GIROSZKÓPOS ÁRNYÉK ---
        if (isAnimationEnabled) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Az árnyék elcsúszik a dőlés hatására
                        translationX = shadowOffsetX
                        translationY = shadowOffsetY
                    }
            ) {
                // Fekete, elhalványuló kör a bábu alatt
                val shadowRadius = size.width * 0.35f
                val shadowCenter = Offset(size.width / 2f, size.height / 2f + size.height * 0.05f) // Kicsit lejjebb van alapból

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent),
                        center = shadowCenter,
                        radius = shadowRadius
                    ),
                    center = shadowCenter,
                    radius = shadowRadius
                )
            }
        }

        // --- 2. RÉTEG: AURA ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val currentRadius = size.minDimension * (0.25f + (0.15f * auraPhase))
                    val currentAlpha = 0.4f - (0.2f * auraPhase)
                    val brush = Brush.radialGradient(
                        0.0f to auraColor.copy(alpha = currentAlpha),
                        0.5f to auraColor.copy(alpha = currentAlpha * 0.5f),
                        1.0f to Color.Transparent,
                        radius = currentRadius
                    )
                    drawCircle(brush = brush, radius = currentRadius)
                }
        )

        // --- 3. RÉTEG: MAGA A BÁBU KÉPE ---
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Game Piece",
            modifier = Modifier.fillMaxSize(0.85f)
        )
    }
}