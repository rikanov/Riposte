package hu.riposte.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SlantedMenuButton(
    item: MainMenuItem,
    buttonWidth: Dp,
    buttonHeight: Dp,
    slantAmountDp: Dp,
    fontSize: TextUnit,
    index: Int,
    phaseState: State<Float>,
    scale: Float,
    isHovered: Boolean
) {
    val density = LocalDensity.current
    val slantPx = with(density) { slantAmountDp.toPx() }
    val hPx = with(density) { buttonHeight.toPx() }

    // --- PREMIUM COLORS AND OPACITY ---
    val alphaColor = if (isHovered) 0.85f else 0.45f
    val gradientColors = if (item.isEnabled) {
        if (isHovered) listOf(Color(0xFF4A5570), Color(0xFF262C3A))
        else listOf(Color(0xFF2A3040), Color(0xFF1A1E28))
    } else {
        listOf(Color(0xFF1E1E24), Color(0xFF121216))
    }
    val textColor = if (item.isEnabled) Color.White else Color.White.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight)
            .graphicsLayer {
                transformOrigin = TransformOrigin(1f, 0.5f)
                scaleX = scale
                scaleY = scale

                val hoverIntensity = if (isHovered) 0.2f else 1f
                // FELEZETT AMPLITÚDÓ: 10f -> 5f, 5f -> 2.5f
                translationX = sin(phaseState.value + index * 0.8f) * 5f * hoverIntensity
                translationY = cos(phaseState.value + index * 0.8f) * 2.5f * hoverIntensity
            }
            .clip(SlantedShape(slantPx = slantPx))
            .background(Brush.horizontalGradient(gradientColors), alpha = alphaColor)
            .border(
                width = if (isHovered) 1.5.dp else 0.8.dp,
                color = Color.White.copy(alpha = if (isHovered) 0.7f else 0.15f),
                shape = SlantedShape(slantPx = slantPx)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.matchParentSize().background(Brush.horizontalGradient(0.0f to Color.White.copy(alpha = 0.1f), 0.3f to Color.Transparent)))

        if (item.needsAttention && !isHovered) {
            val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
            val shimmerTranslate by shimmerTransition.animateFloat(
                initialValue = -100f,
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(tween(2500, delayMillis = 1500, easing = FastOutSlowInEasing), RepeatMode.Restart),
                label = "shimmerTranslation"
            )

            // SZINUSZOS INTENZITÁS: 0-tól indul, középen a legerősebb (0.6f), majd elhalványul
            val progress = (shimmerTranslate + 100f) / 1100f
            val shimmerIntensity = (sin(progress * PI).toFloat() * 0.6f).coerceIn(0f, 1f)

            // PÁRHUZAMOS GRADIENS VEKTOR: A vágás irányára merőleges gradiens vonalat húzunk
            val gradDx = 150f
            val gradDy = 150f * (slantPx / hPx)

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = shimmerIntensity), Color.Transparent),
                            start = Offset(shimmerTranslate, 0f),
                            end = Offset(shimmerTranslate + gradDx, gradDy)
                        )
                    )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = slantAmountDp / 2)
        ) {
            if (item.hasSwipeAction) {
                val infinitePulse = rememberInfiniteTransition(label = "SwipeHint")
                val hintOffset by infinitePulse.animateFloat(
                    initialValue = 0f,
                    targetValue = -6f,
                    animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "arrowOffset"
                )
                val hintAlpha by infinitePulse.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "arrowAlpha"
                )

                Text(
                    text = "« ",
                    color = Color.White.copy(alpha = hintAlpha),
                    fontSize = fontSize,
                    modifier = Modifier.offset(x = hintOffset.dp)
                )
            }
            Text(
                text = item.label,
                color = textColor,
                fontWeight = if (isHovered) FontWeight.Black else FontWeight.Bold,
                fontSize = fontSize,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ParallelMenuButton(
    item: MainMenuItem,
    isPremiumVersion: Boolean,
    buttonWidth: Dp,
    buttonHeight: Dp,
    slantAmountDp: Dp,
    fontSize: TextUnit,
    index: Int,
    phaseState: State<Float>,
    scale: Float,
    isHovered: Boolean,
    isStygian: Boolean
) {
    val density = LocalDensity.current
    val isLocked = item.isPremiumOnly && !isPremiumVersion
    val slantPx = with(density) { slantAmountDp.toPx() }

    // --- TRANSPARENCY AND COLORS ---
    val alphaColor = if (isLocked) 0.3f else if (isHovered) 0.85f else 0.45f
    val gradientColors = if (isStygian) {
        if (isHovered) listOf(Color(0xFF7A2424), Color(0xFF3A1212))
        else listOf(Color(0xFF401A1A), Color(0xFF281111))
    } else {
        if (isHovered) listOf(Color(0xFF4A5570), Color(0xFF262C3A))
        else listOf(Color(0xFF2A3040), Color(0xFF1A1E28))
    }

    val textColor = if (isLocked) Color.White.copy(alpha = 0.3f)
    else if (isStygian) Color(0xFFFF7777)
    else Color.White

    val displayText = if (isLocked) "${item.label} 🔒" else item.label

    Box(
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight)
            .graphicsLayer {
                transformOrigin = TransformOrigin(0f, 0.5f)
                scaleX = scale
                scaleY = scale

                val hoverIntensity = if (isHovered) 0.2f else 1f
                // FELEZETT AMPLITÚDÓ: 8f -> 4f, 4f -> 2f
                translationX = sin(phaseState.value + index * 0.8f + 2f) * 4f * hoverIntensity
                translationY = cos(phaseState.value + index * 0.8f + 2f) * 2f * hoverIntensity
            }
            .clip(ParallelSubMenuShape(slantPx = slantPx))
            .background(Brush.horizontalGradient(gradientColors.reversed()), alpha = alphaColor)
            .border(
                width = if (isHovered) 1.5.dp else 0.8.dp,
                color = if (isStygian && !isLocked) Color.Red.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f),
                shape = ParallelSubMenuShape(slantPx = slantPx)
            ),
        contentAlignment = Alignment.BottomStart
    ) {
        Box(modifier = Modifier.matchParentSize().background(Brush.horizontalGradient(0.0f to Color.Transparent, 0.8f to Color.White.copy(alpha = 0.1f))))

        Text(
            text = displayText,
            color = textColor,
            fontWeight = if (isHovered) FontWeight.Black else FontWeight.Bold,
            fontSize = fontSize * 0.9f,
            letterSpacing = 1.2.sp,
            textAlign = TextAlign.Start,
            maxLines = 1,
            modifier = Modifier.padding(start = 12.dp )
        )
    }
}