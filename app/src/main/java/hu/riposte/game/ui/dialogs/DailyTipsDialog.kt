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
import hu.riposte.game.engine.logic.AppSettings
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.LocalGameTheme

@Composable
fun DailyTipDialog(
    appSettings: AppSettings,
    soundManager: SoundManager,
    onDismiss: () -> Unit,
    onSettingsUpdate: (AppSettings) -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor
    val usefulTips = stringArrayResource(id = R.array.daily_tips_useful)
    val loreTips = stringArrayResource(id = R.array.daily_tips_lore)

    val showUseful = !appSettings.lastTipWasUseful
    val currentList = if (showUseful) usefulTips else loreTips

    val initialIndex = if (showUseful) {
        appSettings.usefulTipIndex % currentList.size
    } else {
        appSettings.loreTipIndex % currentList.size
    }

    var currentIndex by remember { mutableIntStateOf(initialIndex) }
    val currentTip = currentList[currentIndex]

    val parts = currentTip.split("\n")
    // A cím a stringből is jöhet, de a kategória fejléce felülbírálhatja.
    val tipTitle = parts.getOrNull(0) ?: stringResource(id = R.string.daily_tip_fallback)
    val body = parts.getOrNull(1) ?: currentTip

    // Dinamikus UI elemek
    val headerText = if (showUseful) "RIPOSTE ACADEMY" else "FENCING LORE"
    val buttonText = if (showUseful) "NEXT TIP ➔" else "MORE LORE ➔"

    // Pulzáló fény a "Tap to close" jelzéshez
    val infinitePulse = rememberInfiniteTransition(label = "pulse")
    val textAlpha by infinitePulse.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "alpha"
    )

    GlassDialog(
        onDismissRequest = {
            soundManager.playClick()
            val nextIndex = (currentIndex + 1) % currentList.size
            val updatedSettings = if (showUseful) {
                appSettings.copy(lastTipWasUseful = true, usefulTipIndex = nextIndex)
            } else {
                appSettings.copy(lastTipWasUseful = false, loreTipIndex = nextIndex)
            }
            onSettingsUpdate(updatedSettings)
            onDismiss()
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    soundManager.playClick()
                    val nextIndex = (currentIndex + 1) % currentList.size
                    val updatedSettings = if (showUseful) {
                        appSettings.copy(lastTipWasUseful = true, usefulTipIndex = nextIndex)
                    } else {
                        appSettings.copy(lastTipWasUseful = false, loreTipIndex = nextIndex)
                    }
                    onSettingsUpdate(updatedSettings)
                    onDismiss()
                }
                .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
        ) {
            // --- KATEGÓRIA FEJLÉC (ÚJ!) ---
            Text(
                text = headerText,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // --- TIPP CÍM ---
            Text(
                text = tipTitle.uppercase(),
                color = accentColor,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // --- NÉGYZETES, SCROLLOZHATÓ SZÖVEGMEZŐ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f) // Négyzet arány
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
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

            // --- ALSÓ VEZÉRLŐK (Dinamikus gombbal) ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    color = accentColor.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clickable {
                            soundManager.playClick()
                            // Szekvenciális léptetés és azonnali mentés
                            currentIndex = (currentIndex + 1) % currentList.size
                            if (showUseful) {
                                onSettingsUpdate(appSettings.copy(usefulTipIndex = currentIndex))
                            } else {
                                onSettingsUpdate(appSettings.copy(loreTipIndex = currentIndex))
                            }
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}