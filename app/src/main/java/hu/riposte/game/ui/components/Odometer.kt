package hu.riposte.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun RiposteOdometer(
    value: Int,
    digitCount: Int,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val valueString = value.toString().padStart(digitCount, '0')

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        modifier = modifier
    ) {
        valueString.forEachIndexed { index, char ->
            DigitDrum(
                targetDigit = char.digitToInt(),
                index = index,
                color = color,
                fontSize = fontSize
            )
        }
    }
}

@Composable
private fun DigitDrum(targetDigit: Int, index: Int, color: Color, fontSize: TextUnit) {
    val drumHeight = 42.dp

    var targetValue by remember { mutableFloatStateOf(0f) }
    var isFirstLaunch by remember { mutableStateOf(true) }

    LaunchedEffect(targetDigit) {
        if (isFirstLaunch) {
            isFirstLaunch = false
            var diff = targetDigit - 0f
            if (diff > 5f) diff -= 10f
            else if (diff < -5f) diff += 10f
            targetValue = diff
        } else {
            val currentMod = ((targetValue % 10) + 10) % 10f
            var diff = targetDigit - currentMod
            if (diff > 5f) diff -= 10f
            else if (diff < -5f) diff += 10f
            targetValue += diff
        }
    }

    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = index * 100,
            easing = FastOutSlowInEasing
        ),
        label = "DigitScroll"
    )

    Box(
        modifier = Modifier
            .width(22.dp)
            .height(drumHeight)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFFE0E5EC)),
        contentAlignment = Alignment.Center
    ) {
        val v = animatedValue
        val minD = floor(v).toInt() - 1
        val maxD = ceil(v).toInt() + 1

        for (d in minD..maxD) {
            val digitToShow = ((d % 10) + 10) % 10
            val yOffset = (d - v) * drumHeight.value

            Box(
                modifier = Modifier
                    .offset(y = yOffset.dp)
                    .height(drumHeight)
                    .width(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = digitToShow.toString(),
                    color = color,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.5f),
                        0.5f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.5f)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(0.5.dp)
                .background(Color.Black.copy(alpha = 0.15f))
        )
    }
}