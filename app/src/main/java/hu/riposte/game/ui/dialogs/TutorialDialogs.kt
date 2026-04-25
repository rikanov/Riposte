package hu.riposte.game.ui.dialogs

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.LocalGameTheme

@Composable
fun TutorialWelcomeDialog(
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    // --- ÚJ: Fázis kezelés ---
    var currentPhase by remember { mutableIntStateOf(1) } // 1: Swipe, 2: Hold & Select

    val infiniteTransition = rememberInfiniteTransition(label = "TutAnim")

    // Animációk az 1. Fázishoz (Gyors Swipe)
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

    // Animációk a 2. Fázishoz (Lassú Hold & Select)
    val swipeX2 by infiniteTransition.animateFloat(
        initialValue = -60f, targetValue = 60f,
        // Sokkal lassabb, "húzós" mozdulat
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing, delayMillis = 600), RepeatMode.Restart), label = "s2"
    )
    val touchScale2 by infiniteTransition.animateFloat(
        initialValue = 1.3f, targetValue = 0.9f,
        // Sokáig lent marad az ujj
        animationSpec = infiniteRepeatable(
            keyframes { durationMillis = 3100; 1.3f at 0; 0.9f at 300; 0.9f at 2800; 1.3f at 3100 }, RepeatMode.Restart
        ), label = "ts2"
    )

    GlassDialog(onDismissRequest = { }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    soundManager.playClick()
                    if (currentPhase == 1) {
                        currentPhase = 2 // Első kattintásra átvált a "lövész" módra
                    } else {
                        onDismiss() // Második kattintásra bezár és indul a játék
                    }
                }
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (currentPhase == 1) "HOW TO MOVE" else "PRO TIP: AIMING",
                color = accentColor, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            // --- AZ ANIMÁLT VIZUÁLIS BEMUTATÓ ---
            Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {

                // Aktuális fázis animációinak kiválasztása
                val currentSwipeX = if (currentPhase == 1) swipeX1 else swipeX2
                val currentTouchScale = if (currentPhase == 1) touchScale1 else touchScale2
                val currentTouchAlpha = if (currentTouchScale <= 1.0f) 1f else 0f

                if (currentPhase == 1) {
                    // 1. Fázis: Elmosódó csík (Trail)
                    Box(modifier = Modifier.width(100.dp).height(40.dp).background(
                        Brush.horizontalGradient(colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.3f), accentColor.copy(alpha = 0.8f))),
                        shape = RoundedCornerShape(20.dp)
                    ).graphicsLayer { alpha = currentTouchAlpha * 0.8f }
                    )
                } else {
                    // 2. Fázis: "Ghost Piece" és Vonal imitálása (Ahogy kérted!)
                    if (currentTouchAlpha > 0f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(color = accentColor, start = Offset(0f, size.height/2), end = Offset(size.width, size.height/2), strokeWidth = 4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f))
                        }
                        // Szellembábu a falnál
                        Image(painter = painterResource(id = LocalGameTheme.current.pieceP1Res), contentDescription = null, modifier = Modifier.size(40.dp).offset(x = 40.dp).graphicsLayer { alpha = 0.4f })
                    }
                }

                // A toló Bábu
                val pieceOffset = if (currentTouchScale <= 0.9f) currentSwipeX.coerceAtLeast(-40f) else -40f
                Image(
                    painter = painterResource(id = LocalGameTheme.current.pieceP1Res), contentDescription = "Tutorial Piece",
                    modifier = Modifier.size(40.dp).offset(x = pieceOffset.dp)
                )

                // Az Ujj
                Icon(
                    imageVector = Icons.Rounded.TouchApp, contentDescription = "Swipe", tint = Color.White,
                    modifier = Modifier.size(56.dp).offset(x = currentSwipeX.dp, y = 20.dp).graphicsLayer { scaleX = currentTouchScale; scaleY = currentTouchScale; alpha = currentTouchAlpha }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = if (currentPhase == 1) "Swipe any piece to slide it.\nIt will only stop when it hits a wall or another piece."
                else "Hold your finger on the screen and move it slowly to select your target visually before releasing.",
                color = DialogContentColor, textAlign = TextAlign.Center, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            val infinitePulse = rememberInfiniteTransition()
            val textAlpha by infinitePulse.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "")

            Text(
                text = if (currentPhase == 1) "- TAP TO SEE PRO TIP -" else "- TAP TO PLAY -",
                color = accentColor.copy(alpha = textAlpha), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp
            )
        }
    }
}
@Composable
fun TutorialCompleteDialog(
    soundManager: SoundManager,
    onBackToMenu: () -> Unit // Most már a Menübe térünk vissza!
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    // Villogó szöveg animációja a "TAP TO CONTINUE"-hoz
    val infinitePulse = rememberInfiniteTransition()
    val textAlpha by infinitePulse.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "PulseAnim"
    )

    GlassDialog(onDismissRequest = { /* Csak a belső felületre kattintva zárható be */ }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    soundManager.playClick()
                    onBackToMenu()
                } // Bárhova kattintva kilép a menübe
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Text("TUTORIAL COMPLETED", color = accentColor, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(16.dp))

            Text(
                "Great job! You now know all the rules.\nYou are ready to test your skills in the real matches!",
                color = Color.White, textAlign = TextAlign.Center, fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            // Villogó "TAP TO EXIT" instrukció
            Text(
                text = "- TAP TO RETURN TO MENU -",
                color = accentColor.copy(alpha = textAlpha),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}