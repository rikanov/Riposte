package hu.riposte.game.ui.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import hu.riposte.game.ui.theme.GameTheme
import hu.riposte.game.ui.theme.LocalGameTheme
import hu.riposte.game.R
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.ThemeRegistry
import kotlinx.coroutines.launch

val DialogContentColor = Color.White
val DialogDarkTextColor = Color(0xFF1E272E)

// --- UNIVERSAL "JUICY GLASS" CONTAINER ---
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    val accentColor = LocalGameTheme.current.uiAccentColor

    val shimmerProgress = rememberInfiniteTransition(label = "dialog_shimmer").animateFloat(
        initialValue = -0.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer_anim"
    )

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.55f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch { alpha.animateTo(1f, tween(200)) }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E272E).copy(alpha = 0.85f))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .drawWithContent {
                    drawContent()
                    val w = size.width
                    val h = size.height
                    val xOffset = w * shimmerProgress.value
                    val shimmerBrush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.8f), Color.Transparent),
                        start = Offset(xOffset, 0f),
                        end = Offset(xOffset + 200f, h)
                    )
                    drawRoundRect(
                        brush = shimmerBrush,
                        size = size,
                        cornerRadius = CornerRadius(24.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                .padding(24.dp)
        ) {
            content()
        }
    }
}
data class Particle(val vx: Float, val vy: Float, val color: Color, val size: Float)
@Composable
fun FireworksOverlay() {
    val particles = remember {
        List(150) {
            Particle(
                vx = (Math.random() * 60 - 30).toFloat(),
                vy = (Math.random() * -60 - 20).toFloat(),
                color = Color(
                    red = (100..255).random() / 255f,
                    green = (100..255).random() / 255f,
                    blue = (100..255).random() / 255f,
                    alpha = 1f
                ),
                size = (Math.random() * 12 + 6).toFloat()
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2500, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 1f - (progress.value * 0.5f) }) {
        val centerOffset = Offset(size.width / 2, size.height * 0.35f)

        particles.forEach { p ->
            val currentX = centerOffset.x + (p.vx * progress.value * 50)
            val currentY = centerOffset.y + (p.vy * progress.value * 50 + 60f * progress.value * progress.value * 50)

            drawCircle(
                color = p.color.copy(alpha = 1f - progress.value),
                radius = p.size * (1f - progress.value),
                center = Offset(currentX, currentY)
            )
        }
    }
}
