package hu.riposte.game.ui.dialogs

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.R
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.LocalGameTheme

@Composable
fun DailyTipDialog(
    soundManager: SoundManager,
    onDismiss: () -> Unit,
    onTurnOff: () -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor
    val tips = stringArrayResource(id = R.array.daily_tips)

    var currentTipIndex by remember { mutableIntStateOf(tips.indices.random()) }
    val currentTip = tips[currentTipIndex]

    val parts = currentTip.split("\n")
    val title = parts.getOrNull(0) ?: stringResource(id = R.string.daily_tip_fallback)
    val body = parts.getOrNull(1) ?: currentTip

    // Pulzáló fény a jelzéshez
    val infinitePulse = rememberInfiniteTransition(label = "pulse")
    val textAlpha by infinitePulse.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "alpha"
    )

    GlassDialog(onDismissRequest = { /* Csak gombra zárható */ }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
        ) {
            // --- CÍM ---
            Text(
                text = title.uppercase(),
                color = accentColor,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // --- NÉGYZETES, SCROLLOZHATÓ SZÖVEGMEZŐ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f) // Ez kényszeríti a négyzet alakot
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        soundManager.playClick()
                        onDismiss()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = body,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- "TAP TO CLOSE" JELZÉS ---
            Text(
                text = stringResource(id = R.string.tap_to_continue).uppercase(),
                color = Color.White.copy(alpha = textAlpha * 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(32.dp))

            // --- ALSÓ VEZÉRLŐK ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // KIKAPCSOLÁS (Háttér nélküli kattintható szöveg)
                Text(
                    text = stringResource(id = R.string.turn_off_notifications),
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clickable {
                            soundManager.playClick()
                            onTurnOff()
                            onDismiss()
                        }
                        .padding(8.dp)
                )

                // KÖVETKEZŐ (Háttér nélküli kattintható szöveg, de hangsúlyosabb színnel)
                Text(
                    text = stringResource(id = R.string.next_tip).uppercase(),
                    color = accentColor.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clickable {
                            soundManager.playClick()
                            var nextIndex = tips.indices.random()
                            if (tips.size > 1) {
                                while (nextIndex == currentTipIndex) {
                                    nextIndex = tips.indices.random()
                                }
                            }
                            currentTipIndex = nextIndex
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}