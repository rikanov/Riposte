package hu.riposte.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RiposteStatusIndicator(gameViewModel: GameViewModel) {
    val currentTheme = LocalGameTheme.current
    val density = LocalDensity.current

    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        var displayPhase by remember { mutableStateOf(gameViewModel.gamePhase) }
        val flipRotation = remember { Animatable(0f) }
        val shimmerProgress = remember { Animatable(-1f) }

        LaunchedEffect(gameViewModel.gamePhase) {
            if (displayPhase != gameViewModel.gamePhase) {
                flipRotation.animateTo(90f, animationSpec = tween(300, easing = LinearEasing))
                displayPhase = gameViewModel.gamePhase
                flipRotation.snapTo(-90f)
                flipRotation.animateTo(0f, animationSpec = tween(350, easing = LinearOutSlowInEasing))
                shimmerProgress.snapTo(-0.5f)
                shimmerProgress.animateTo(1.5f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
            }
        }

        val containerColor = when (displayPhase) {
            GameWaitingFor.AI_MOVE -> Color(0xFFFFEBEE)
            GameWaitingFor.TAKE_PIECE -> Color(0xFFFFF8E1)
            GameWaitingFor.GAME_OVER -> Color(0xFFE8F5E9)
            else -> currentTheme.containerColor
        }
        val contentColor = when (displayPhase) {
            GameWaitingFor.AI_MOVE -> Color(0xFFD32F2F)
            GameWaitingFor.TAKE_PIECE -> Color(0xFFF57F17)
            GameWaitingFor.GAME_OVER -> Color(0xFF2E7D32)
            else -> currentTheme.textColor
        }

        val statusText = when (displayPhase) {
            GameWaitingFor.MOVE_PIECE -> stringResource(R.string.status_players_turn)
            GameWaitingFor.AI_MOVE -> stringResource(R.string.status_ai_thinking)
            GameWaitingFor.TAKE_PIECE -> stringResource(R.string.status_select_capture)
            GameWaitingFor.ANIMATION -> stringResource(R.string.status_wait)
            GameWaitingFor.GAME_OVER -> stringResource(R.string.status_game_over, gameViewModel.winner ?: "")
            else -> stringResource(R.string.status_setting_up)
        }

        val metalShape = CutCornerShape(10.dp)
        val metallicBrush = Brush.linearGradient(
            colors = listOf(
                containerColor.copy(alpha = 0.8f),
                Color.White.copy(alpha = 0.9f),
                containerColor,
                containerColor.copy(alpha = 0.6f),
                containerColor
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(44.dp)
                .graphicsLayer {
                    rotationX = flipRotation.value
                    cameraDistance = 12f * density.density
                }
                .shadow(6.dp, metalShape)
                .clip(metalShape)
                .background(metallicBrush)
                .drawBehind {
                    val lineColor = Color.Black.copy(alpha = 0.05f)
                    val strokeWidth = 1.dp.toPx()
                    var yPos = 0f
                    while (yPos < size.height) {
                        drawLine(color = lineColor, start = Offset(0f, yPos), end = Offset(size.width, yPos), strokeWidth = strokeWidth)
                        yPos += 3.dp.toPx()
                    }
                }
                .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.3f))), shape = metalShape)
                .drawWithContent {
                    drawContent()
                    if (shimmerProgress.value > -0.5f && shimmerProgress.value < 1.5f) {
                        val w = size.width
                        val h = size.height
                        val xOffset = w * shimmerProgress.value
                        val shimmerBrush = Brush.linearGradient(colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.9f), Color.Transparent), start = Offset(xOffset, 0f), end = Offset(xOffset + 80.dp.toPx(), h))
                        drawRect(brush = shimmerBrush, blendMode = BlendMode.SrcAtop)
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = statusText.uppercase(), fontWeight = FontWeight.Black, color = contentColor, fontSize = 13.sp, letterSpacing = 1.sp)
        }
    }
}