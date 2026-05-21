package hu.riposte.game.ui.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import riposte.app.generated.resources.*
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.LocalGameTheme

@Composable
fun TutorialWelcomeOverlay(
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    val theme = LocalGameTheme.current
    val accentColor = theme.uiAccentColor

    var currentPhase by remember { mutableIntStateOf(1) }
    val infiniteTransition = rememberInfiniteTransition(label = "TutAnim")

    val swipeX1 by infiniteTransition.animateFloat(
        initialValue = -60f, targetValue = 60f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing, delayMillis = 600), RepeatMode.Restart), label = "s1"
    )
    val touchScale1 by infiniteTransition.animateFloat(
        initialValue = 1.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            keyframes { durationMillis = 1400; 1.3f at 0; 0.9f at 300; 0.9f at 1100; 1.3f at 1400 }, RepeatMode.Restart
        ), label = "ts1"
    )
    val swipeX2 by infiniteTransition.animateFloat(
        initialValue = -60f, targetValue = 60f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing, delayMillis = 600), RepeatMode.Restart), label = "s2"
    )
    val touchScale2 by infiniteTransition.animateFloat(
        initialValue = 1.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            keyframes { durationMillis = 3100; 1.3f at 0; 0.9f at 300; 0.9f at 2800; 1.3f at 3100 }, RepeatMode.Restart
        ), label = "ts2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable {
                soundManager.playClick()
                if (currentPhase == 1) {
                    currentPhase = 2
                } else {
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (currentPhase == 1) stringResource(Res.string.tut_how_to_move) else stringResource(Res.string.tut_pro_tip),
                color = accentColor,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 20.sp,
                fontFamily = theme.fontFamily
            )
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.08f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                val currentSwipeX = if (currentPhase == 1) swipeX1 else swipeX2
                val currentTouchScale = if (currentPhase == 1) touchScale1 else touchScale2
                val currentTouchAlpha = if (currentTouchScale <= 1.0f) 1f else 0f

                if (currentPhase == 1) {
                    Box(modifier = Modifier.width(100.dp).height(40.dp).background(
                        Brush.horizontalGradient(colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.2f), accentColor.copy(alpha = 0.6f))),
                        shape = RoundedCornerShape(20.dp)
                    ).graphicsLayer { alpha = currentTouchAlpha * 0.8f }
                    )
                } else {
                    if (currentTouchAlpha > 0f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(color = accentColor, start = Offset(0f, size.height/2), end = Offset(size.width, size.height/2), strokeWidth = 4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f))
                        }
                        Image(painter = painterResource(theme.pieceP2Res), contentDescription = null, modifier = Modifier.size(40.dp).offset(x = 40.dp).graphicsLayer { alpha = 0.3f })
                    }
                }
                val pieceOffset = if (currentTouchScale <= 0.9f) currentSwipeX.coerceAtLeast(-40f) else -40f

                Image(
                    painter = painterResource(theme.pieceP2Res), contentDescription = "Tutorial Piece",
                    modifier = Modifier.size(40.dp).offset(x = pieceOffset.dp)
                )
                Icon(
                    imageVector = Icons.Rounded.TouchApp, contentDescription = "Swipe", tint = Color.White,
                    modifier = Modifier.size(56.dp).offset(x = currentSwipeX.dp, y = 20.dp).graphicsLayer { scaleX = currentTouchScale; scaleY = currentTouchScale; alpha = currentTouchAlpha }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (currentPhase == 1) stringResource(Res.string.tut_swipe_desc) else stringResource(Res.string.tut_aim_desc),
                color = accentColor.copy(alpha = 0.85f),
                fontFamily = theme.fontFamily,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            val infinitePulse = rememberInfiniteTransition(label = "pulse")
            val textAlpha by infinitePulse.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "")

            Text(
                text = if (currentPhase == 1) stringResource(Res.string.tut_tap_pro_tip) else stringResource(Res.string.tut_tap_play),
                color = accentColor.copy(alpha = textAlpha),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                fontFamily = theme.fontFamily
            )
        }
    }
}

@Composable
fun TutorialCompleteOverlay(
    soundManager: SoundManager,
    onBackToMenu: () -> Unit
) {
    val theme = LocalGameTheme.current
    val accentColor = theme.uiAccentColor

    val infinitePulse = rememberInfiniteTransition(label = "pulse")
    val textAlpha by infinitePulse.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "PulseAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable {
                soundManager.playClick()
                onBackToMenu()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.tut_completed_title),
                color = accentColor,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 1.sp,
                fontFamily = theme.fontFamily
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.tut_completed_desc),
                color = accentColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontFamily = theme.fontFamily
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(Res.string.tut_tap_return),
                color = accentColor.copy(alpha = textAlpha),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                fontFamily = theme.fontFamily
            )
        }
    }
}

@Composable
fun TutorialDefeatOverlay(
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    val theme = LocalGameTheme.current
    val accentColor = theme.uiAccentColor

    val infinitePulse = rememberInfiniteTransition(label = "pulse")
    val textAlpha by infinitePulse.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "PulseAnim"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable {
                soundManager.playClick()
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        val iconSize = maxWidth * 0.1f

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.tut_defeat_title),
                color = accentColor,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                fontFamily = theme.fontFamily
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.tut_defeat_desc),
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontFamily = theme.fontFamily
            )
            Spacer(Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DockToolExplanation(iconRes = Res.drawable.ic_dock_undo, label = stringResource(Res.string.tut_dock_undo), color = accentColor, iconSize = iconSize)
                DockToolExplanation(iconRes = Res.drawable.ic_dock_hint, label = stringResource(Res.string.tut_dock_hint), color = accentColor, iconSize = iconSize)
                DockToolExplanation(iconRes = Res.drawable.ic_dock_info, label = stringResource(Res.string.tut_dock_info), color = accentColor, iconSize = iconSize)
                DockToolExplanation(iconRes = Res.drawable.ic_dock_menu, label = stringResource(Res.string.tut_dock_menu), color = accentColor, iconSize = iconSize)
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.tut_tap_continue),
                color = accentColor.copy(alpha = textAlpha),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                fontFamily = theme.fontFamily
            )
        }
    }
}

@Composable
private fun DockToolExplanation(iconRes: DrawableResource, label: String, color: Color, iconSize: Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .background(color, CircleShape)
                .padding(iconSize * 0.2f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = Color.Black,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}