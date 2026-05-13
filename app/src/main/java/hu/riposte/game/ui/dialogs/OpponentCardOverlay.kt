package hu.riposte.game.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.ui.theme.LocalGameTheme
import hu.riposte.game.engine.data.TournamentOpponent

@Composable
fun OpponentCardOverlay(
    opponent: TournamentOpponent?,
    isVisible: Boolean,
    onClose: () -> Unit
) {
    if (opponent == null) return

    val currentTheme = LocalGameTheme.current
    val rotationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 180f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "CardRotation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.4f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "CardScale"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 400.dp,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "CardOffset"
    )
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(500)),
        exit = fadeOut(tween(500)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        )
    }
    if (scale > 0.45f) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset(y = offsetY)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.rotationY = rotationY
                        cameraDistance = 16f * density
                    }
                    .width(300.dp)
                    .fillMaxHeight(0.65f)
                    .shadow(24.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A1C23), Color(0xFF0D0F14))))
                    .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    )
            ) {
                if (rotationY <= 90f) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFFD4AF37).copy(alpha = 0.1f), RoundedCornerShape(30.dp))
                                .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(30.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚔️", fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(id = opponent.nameRes).uppercase(),
                            color = Color(0xFFD4AF37),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = currentTheme.fontFamily
                        )
                        Text(
                            text = stringResource(id = opponent.titleRes),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontFamily = currentTheme.fontFamily
                        )
                        Text(
                            text = stringResource(id = opponent.eraRes),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            fontFamily = currentTheme.fontFamily
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .padding(vertical = 16.dp)
                                .height(1.dp)
                                .background(Color(0xFFD4AF37).copy(alpha = 0.3f))
                        )
                        Text(
                            text = stringResource(id = opponent.descriptionRes),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                            fontFamily = currentTheme.fontFamily
                        )
                        Text(
                            text = stringResource(id = opponent.quoteRes),
                            color = Color(0xFFD4AF37),
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp),
                            fontFamily = currentTheme.fontFamily
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "R I P O S T E",
                            color = Color(0xFFD4AF37).copy(alpha = 0.3f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 8.sp,
                            modifier = Modifier.graphicsLayer { rotationZ = -90f },
                            fontFamily = currentTheme.fontFamily
                        )
                    }
                }
            }
        }
    }
}
