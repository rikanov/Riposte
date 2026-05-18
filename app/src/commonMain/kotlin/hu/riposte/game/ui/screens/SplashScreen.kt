package hu.riposte.game.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import riposte.app.generated.resources.*
import org.jetbrains.compose.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // --- ANIMATION VALUES ---
    val textAlpha = remember { Animatable(0f) }
    val shimmerProgress = remember { Animatable(0f) }
    val globalAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // 1. Initial wait to bridge from the OS splash
        delay(300)

        // 2. PARALLEL ANIMATION: Sweep the light while revealing the text
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = EaseOutSine)
            )
        }
        launch {
            shimmerProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = LinearEasing)
            )
        }

        // 3. Reading pause
        delay(800)

        // 4. Final fade out
        globalAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 600, easing = EaseOutSine)
        )

        // 5. Hand over to main menu
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F141E))
            .graphicsLayer { alpha = globalAlpha.value },
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient motion
        AmbientDustVFX()

        // --- CENTERED CONTAINER ---
        Box(
            modifier = Modifier
                .graphicsLayer {
                    // Necessary for SrcAtop blend mode to work on the whole layer
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    
                    // --- CINEMATIC LIGHT SWEEP REVEAL ---
                    if (shimmerProgress.value > 0f && shimmerProgress.value < 1f) {
                        val width = size.width
                        val height = size.height
                        
                        // Sweeping movement from left to right
                        val sweepX = (shimmerProgress.value * (width * 2)) - (width / 2)
                        
                        val shimmerBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.7f),
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            start = Offset(sweepX, 0f),
                            end = Offset(sweepX + 180.dp.toPx(), height)
                        )
                        
                        // Mask the shimmer to our logo and text vectors
                        drawRect(
                            brush = shimmerBrush,
                            blendMode = BlendMode.SrcAtop
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // THE LOGO: Dead center of the screen
            Image(
                painter = painterResource(Res.drawable.ic_letter_r),
                contentDescription = null,
                modifier = Modifier.size(160.dp)
            )

            // THE TEXT: Offset below the logo without moving the logo's center
            Image(
                painter = painterResource(Res.drawable.ic_la_riposte_text),
                contentDescription = "La Riposte",
                modifier = Modifier
                    .width(200.dp)
                    .offset(y = 120.dp)
                    .graphicsLayer { alpha = textAlpha.value }
            )
        }
    }
}
