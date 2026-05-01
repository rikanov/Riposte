package hu.riposte.game.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.R
import hu.riposte.game.engine.logic.AppSettings
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.LocalGameTheme

// --- HELPER COMPOSABLES (Visszatettem őket!) ---

@Composable
fun AnimatedOdometer(
    value: Int,
    digitCount: Int,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "OdometerTween"
    )

    val valueString = animatedValue.toString().padStart(digitCount, '0')

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier.padding(horizontal = 0.dp)
    ) {
        valueString.forEach { digit ->
            DigitDrum(digit = digit, color = color, fontSize = fontSize)
        }
    }
}

@Composable
fun DigitDrum(digit: Char, color: Color, fontSize: androidx.compose.ui.unit.TextUnit) {
    Box(
        modifier = Modifier
            .width(18.dp)
            .height(42.dp)
            .background(Color(0xFFE0E5EC)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height } + fadeOut())
            },
            label = "DigitAnim"
        ) { targetDigit ->
            Text(
                text = targetDigit.toString(),
                color = color,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
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
                .background(Color.Black.copy(alpha = 0.1f))
        )
    }
}

// --- MAIN COMPOSABLE ---

@Composable
fun PlayerStatsOverlay(
    appSettings: AppSettings,
    isFirstLaunch: Boolean,
    soundManager: SoundManager,
    onSaveProfile: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    var editName by remember(appSettings.playerName) { mutableStateOf(appSettings.playerName) }
    var editTitle by remember(appSettings.playerTitle) { mutableStateOf(appSettings.playerTitle) }
    var isEditing by remember { mutableStateOf(isFirstLaunch) }

    // Gördülő ablak adatok feldolgozása (Utolsó 20 meccs)
    val matches = appSettings.recentThreats.split(",").filter { it.isNotEmpty() }
    var totalHs1 = 0 // Ellenfél (Defense need)
    var totalHs2 = 0 // Játékos (Offense)

    matches.forEach { pair ->
        val parts = pair.split(":")
        if (parts.size == 2) {
            totalHs1 += parts[0].toIntOrNull() ?: 0
            totalHs2 += parts[1].toIntOrNull() ?: 0
        }
    }

    val totalThreats = totalHs1 + totalHs2
    val playerOffenseRatio = if (totalThreats == 0) 0.5f else totalHs2.toFloat() / totalThreats.toFloat()

    val personaTitle = when {
        totalThreats == 0 -> "UNKNOWN COMBATANT"
        playerOffenseRatio >= 0.55f -> "THE AGGRESSOR"
        playerOffenseRatio <= 0.45f -> "THE TACTICIAN"
        else -> "THE MAESTRO"
    }

    val personaColor = when {
        totalThreats == 0 -> Color.Gray
        playerOffenseRatio >= 0.55f -> Color(0xFFFF4444) // Agresszív Vörös
        playerOffenseRatio <= 0.45f -> Color(0xFF44AAFF) // Taktikus Kék
        else -> accentColor // Maestro Arany/Téma szín
    }

    // Animált százalék sávhoz
    val animatedRatio by animateFloatAsState(
        targetValue = playerOffenseRatio,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "RatioAnim"
    )

    GlassDialog(onDismissRequest = { if (!isFirstLaunch) onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- NÉV ÉS TITULUS ---
            if (isEditing) {
                BasicTextField(
                    value = editName,
                    onValueChange = { if (it.length <= 16) editName = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier.fillMaxWidth().border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                BasicTextField(
                    value = editTitle,
                    onValueChange = { if (it.length <= 20) editTitle = it },
                    textStyle = TextStyle(color = accentColor, fontSize = 12.sp, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(6.dp)
                )
            } else {
                Text(
                    text = editName.uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickable { soundManager.playClick(); isEditing = true }
                )
                Text(
                    text = editTitle,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.clickable { soundManager.playClick(); isEditing = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- COMBAT PERSONA SZEKCIÓ ---
            Text(
                text = "COMBAT PERSONA",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = personaTitle,
                color = personaColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- TUG OF WAR SÁV ---
            if (totalThreats == 0) {
                Text("PLAY TOURNAMENT MATCHES TO REVEAL", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            } else {
                Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                    // Címkék
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("DEFENSE", color = Color(0xFF44AAFF), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("OFFENSE", color = Color(0xFFFF4444), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Sáv maga
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF44AAFF).copy(alpha = 0.8f)) // Alapból Kék (Defense)
                    ) {
                        // Vörös (Offense) ráhúzva
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedRatio)
                                .fillMaxHeight()
                                .align(Alignment.CenterEnd)
                                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                .background(Color(0xFFFF4444).copy(alpha = 0.8f))
                        )
                        // Fehér középvonal (Maestro zóna)
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .align(Alignment.Center)
                                .background(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    // Százalékok
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${( (1f - animatedRatio) * 100).toInt()}%", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${(animatedRatio * 100).toInt()}%", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- RANK ODOMETER (Egyszerűsítve) ---
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CURRENT RANK", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    AnimatedOdometer(value = appSettings.tournamentRank, digitCount = 2, color = Color(0xFF1E272E), fontSize = 20.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PEAK RANK", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    AnimatedOdometer(value = appSettings.tournamentHighest, digitCount = 2, color = Color(0xFF1E272E), fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- GOMBOK ---
            if (isEditing) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(accentColor).clickable {
                        soundManager.playClick()
                        isEditing = false
                        onSaveProfile(editName.ifBlank { "CHALLENGER" }, editTitle.ifBlank { "Unranked" })
                    }.padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.btn_save_profile), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                        .clickable { soundManager.playClick(); onDismiss() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.btn_close), color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}