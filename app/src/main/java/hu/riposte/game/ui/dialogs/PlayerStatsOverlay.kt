package hu.riposte.game.ui.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import hu.riposte.game.ui.components.RiposteOdometer
import hu.riposte.game.ui.theme.LocalGameTheme

@Composable
fun RecentFormDisplay(cleanHistory: String) {
    val items = remember(cleanHistory) {
        val last10 = cleanHistory.takeLast(10).reversed()
        List(10) { i -> if (i < last10.length) last10[i] else null }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(2) { rowIndex ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(5) { colIndex ->
                    val index = rowIndex * 5 + colIndex
                    val matchChar = items[index]

                    val isWin = matchChar?.let { it == 'W' }

                    val bgColor = when (isWin) {
                        null -> Color.White.copy(alpha = 0.15f)
                        true -> Color(0xFF00C853)
                        false -> Color(0xFFFF4444)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isWin != null) {
                            Canvas(modifier = Modifier.fillMaxSize(0.5f)) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val r = size.minDimension / 2f

                                val path = Path()
                                if (isWin) {
                                    path.moveTo(cx, cy - r)
                                    path.lineTo(cx + r * 0.866f, cy + r * 0.5f)
                                    path.lineTo(cx - r * 0.866f, cy + r * 0.5f)
                                } else {
                                    path.moveTo(cx - r * 0.866f, cy - r * 0.5f)
                                    path.lineTo(cx + r * 0.866f, cy - r * 0.5f)
                                    path.lineTo(cx, cy + r)
                                }
                                path.close()
                                drawPath(path, Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: Int,
    digitCount: Int,
    accentColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = accentColor,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        RiposteOdometer(
            value = value,
            digitCount = digitCount,
            color = Color(0xFF1E272E),
            fontSize = fontSize
        )
    }
}

@Composable
fun PlayerStatsOverlay(
    appSettings: AppSettings,
    isFirstLaunch: Boolean,
    soundManager: SoundManager,
    onSaveProfile: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalGameTheme.current
    val accentColor = theme.uiAccentColor

    var editName by remember(appSettings.playerName) { mutableStateOf(appSettings.playerName) }
    var editTitle by remember(appSettings.playerTitle) { mutableStateOf(appSettings.playerTitle) }
    var isEditing by remember { mutableStateOf(isFirstLaunch) }

    val rawHistory = appSettings.tournamentMatchHistory
    val cleanHistory = rawHistory.filter { it == 'W' || it == 'L' }

    val totalMatches = cleanHistory.length
    val wins = cleanHistory.count { it == 'W' }
    val winRate = if (totalMatches > 0) ((wins.toFloat() / totalMatches) * 100).toInt() else 0

    val baseRating = (21 - appSettings.tournamentRank) * 100
    val dynamicRating = baseRating + (winRate * 2) + totalMatches
    val displayRatingValue = if (totalMatches == 0) 0 else dynamicRating

    val threats = appSettings.recentThreats.split(",").filter { it.isNotEmpty() }
    var totalHs1 = 0; var totalHs2 = 0
    threats.forEach { pair ->
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
        playerOffenseRatio >= 0.55f -> Color(0xFFFF4444)
        playerOffenseRatio <= 0.45f -> Color(0xFF44AAFF)
        else -> accentColor
    }

    val animatedRatio by animateFloatAsState(targetValue = playerOffenseRatio, animationSpec = tween(1500), label = "")

    GlassDialog(onDismissRequest = { if (!isFirstLaunch) onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isEditing) {
                BasicTextField(
                    value = editName,
                    onValueChange = { if (it.length <= 16) editName = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, fontFamily = theme.fontFamily),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier.fillMaxWidth().border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                BasicTextField(
                    value = editTitle,
                    onValueChange = { if (it.length <= 20) editTitle = it },
                    textStyle = TextStyle(color = accentColor, fontSize = 12.sp, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center, fontFamily = theme.fontFamily),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(6.dp)
                )
            } else {
                Text(text = editName.uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = theme.fontFamily, modifier = Modifier.clickable { soundManager.playClick(); isEditing = true })
                Text(text = editTitle, color = accentColor, fontSize = 12.sp, fontStyle = FontStyle.Italic, fontFamily = theme.fontFamily, modifier = Modifier.clickable { soundManager.playClick(); isEditing = true })
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatColumn(label = "CURRENT\nRANK", value = appSettings.tournamentRank, digitCount = 2, accentColor = Color.White.copy(alpha = 0.6f), fontSize = 20.sp)
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)))
                StatColumn(label = "RIPOSTE\nRATING", value = displayRatingValue, digitCount = 4, accentColor = accentColor, fontSize = 24.sp)
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)))
                StatColumn(label = "PEAK\nRANK", value = appSettings.tournamentHighest, digitCount = 2, accentColor = Color.White.copy(alpha = 0.6f), fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("WIN\nRATE", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$winRate%", color = if (winRate >= 50) Color(0xFF00C853) else Color(0xFFFF4444), fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = theme.fontFamily)
                    Text("$totalMatches MATCHES", color = Color.White.copy(alpha = 0.3f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }

                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.1f)))

                Column(
                    modifier = Modifier.weight(1.5f).padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("RECENT FORM", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    RecentFormDisplay(cleanHistory)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "COMBAT PERSONA", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(text = personaTitle, color = personaColor, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontFamily = theme.fontFamily)
                Spacer(modifier = Modifier.height(12.dp))
                if (totalThreats == 0) {
                    Text("PLAY MATCHES TO REVEAL", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else {
                    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("DEFENSE", color = Color(0xFF44AAFF), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            Text("OFFENSE", color = Color(0xFFFF4444), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF44AAFF).copy(alpha = 0.8f))) {
                            Box(modifier = Modifier.fillMaxWidth(animatedRatio).fillMaxHeight().align(Alignment.CenterEnd).clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)).background(Color(0xFFFF4444).copy(alpha = 0.8f)))
                            Box(modifier = Modifier.width(2.dp).fillMaxHeight().align(Alignment.Center).background(Color.White))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(accentColor).clickable { soundManager.playClick(); isEditing = false; onSaveProfile(editName.ifBlank { "CHALLENGER" }, editTitle.ifBlank { "Unranked" }) }.padding(12.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.btn_save_profile), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontFamily = theme.fontFamily)
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth(0.6f).clip(RoundedCornerShape(8.dp)).border(1.dp, accentColor, RoundedCornerShape(8.dp)).clickable { soundManager.playClick(); onDismiss() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.btn_close), color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontFamily = theme.fontFamily)
                }
            }
        }
    }
}