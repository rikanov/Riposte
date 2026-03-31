package hu.riposte.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// --- ALAP BEÁLLÍTÁSOK ADATOSZTÁLYA ---
data class AppSettings(
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val hapticEnabled: Boolean = true
)

// --- KÖZÖS SZÍNPALETTA A DIALÓGUSOKHOZ ---
private val DialogSurfaceColor = Color(0xFF2F3640) // Sötét palaszürke
private val DialogAccentColor = Color(0xFF00E5FF) // Elektromos Cián
private val DialogContentColor = Color.White
private val DialogDarkTextColor = Color(0xFF1E272E)

@Composable
fun SetupDialog(onStart: (GameSettings) -> Unit, onCancel: () -> Unit) {
    var diff by remember { mutableFloatStateOf(5f) }
    var riposte by remember { mutableStateOf(true) }
    var starter by remember { mutableStateOf(StartingPlayer.PLAYER) }
    var selectedMode by remember { mutableStateOf(GameMode.VS_AI) }

    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DialogSurfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NEW MATCH", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = DialogAccentColor, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 24.dp))

                Text("MODE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameMode.entries.forEach { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (isSelected) DialogAccentColor.copy(alpha = 0.15f) else Color(0xFF1E272E)).border(1.dp, if (isSelected) DialogAccentColor else Color.Transparent, RoundedCornerShape(12.dp)).clickable { selectedMode = mode }.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (mode == GameMode.VS_AI) "VS AI" else "2 PLAYER", color = if (isSelected) DialogAccentColor else DialogContentColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                AnimatedVisibility(visible = selectedMode == GameMode.VS_AI) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("AI DIFFICULTY", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${diff.toInt()}", color = DialogAccentColor, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = diff, onValueChange = { diff = it }, valueRange = 5f..10f, steps = 4, colors = SliderDefaults.colors(thumbColor = DialogAccentColor, activeTrackColor = DialogAccentColor, inactiveTrackColor = Color(0xFF1E272E)))
                    }
                }

                AnimatedVisibility(visible = selectedMode == GameMode.VS_AI) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Text("FIRST MOVE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StartingPlayer.entries.forEach { op ->
                                val isSelected = starter == op
                                Box(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) DialogAccentColor.copy(alpha = 0.15f) else Color(0xFF1E272E)).border(1.dp, if (isSelected) DialogAccentColor else Color.Transparent, RoundedCornerShape(8.dp)).clickable { starter = op }.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(op.name, color = if (isSelected) DialogAccentColor else DialogContentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { riposte = !riposte }.background(Color(0xFF1E272E)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Riposte Rule", color = DialogContentColor, fontWeight = FontWeight.Medium)
                    Switch(checked = riposte, onCheckedChange = { riposte = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DialogAccentColor, uncheckedTrackColor = DialogSurfaceColor))
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { onStart(GameSettings(selectedMode, starter, diff.toInt(), riposte)) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = DialogAccentColor)) {
                    Text("START MATCH", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DialogDarkTextColor)
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(winnerName: String, onRestart: () -> Unit) {
    val isWin = winnerName.contains("You won", ignoreCase = true) || winnerName.contains("Player", ignoreCase = true)
    val accentColor = if (isWin) DialogAccentColor else Color(0xFFE53935)

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = DialogSurfaceColor), elevation = CardDefaults.cardElevation(defaultElevation = 24.dp), modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = if (isWin) "VICTORY!" else "DEFEAT", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = accentColor, letterSpacing = 4.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = winnerName, color = DialogContentColor, fontSize = 16.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onRestart, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                    Text("PLAY AGAIN", fontWeight = FontWeight.Bold, color = if (isWin) DialogDarkTextColor else Color.White)
                }
            }
        }
    }
}

@Composable
fun PauseMenuDialog(
    onResume: () -> Unit,
    onNewGame: () -> Unit,
    onOptions: () -> Unit, // <--- ÚJ GOMB ESEMÉNYE
    onUndo: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = DialogSurfaceColor), elevation = CardDefaults.cardElevation(defaultElevation = 24.dp), modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PAUSE MENU", color = DialogAccentColor, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(32.dp))

                MenuButton(text = "RESUME", onClick = onResume, isPrimary = true)
                Spacer(modifier = Modifier.height(16.dp))
                MenuButton(text = "NEW GAME", onClick = onNewGame)
                Spacer(modifier = Modifier.height(16.dp))
                // --- ÚJ OPTIONS GOMB ---
                MenuButton(text = "OPTIONS", onClick = onOptions)
                Spacer(modifier = Modifier.height(16.dp))
                MenuButton(text = "UNDO LAST MOVE", onClick = onUndo)
                Spacer(modifier = Modifier.height(32.dp))
                MenuButton(text = "EXIT GAME", onClick = onExit, isDanger = true)
            }
        }
    }
}

// --- ÚJ: BEÁLLÍTÁSOK ABLAK ---
@Composable
fun OptionsDialog(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DialogSurfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SETTINGS", color = DialogAccentColor, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(32.dp))

                SettingsToggleRow("Music", settings.musicEnabled) { onSettingsChange(settings.copy(musicEnabled = it)) }
                Spacer(modifier = Modifier.height(8.dp))
                SettingsToggleRow("Sound Effects (SFX)", settings.sfxEnabled) { onSettingsChange(settings.copy(sfxEnabled = it)) }
                Spacer(modifier = Modifier.height(8.dp))
                SettingsToggleRow("Haptic Feedback", settings.hapticEnabled) { onSettingsChange(settings.copy(hapticEnabled = it)) }

                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = DialogAccentColor)) {
                    Text("DONE", fontWeight = FontWeight.Bold, color = DialogDarkTextColor)
                }
            }
        }
    }
}

// Segéd komponens a kapcsolóknak, hogy ne ismételjük a kódot
@Composable
private fun SettingsToggleRow(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onCheckedChange(!isChecked) }.background(Color(0xFF1E272E)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, color = DialogContentColor, fontWeight = FontWeight.Medium)
        Switch(checked = isChecked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DialogAccentColor, uncheckedTrackColor = DialogSurfaceColor))
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit, isPrimary: Boolean = false, isDanger: Boolean = false) {
    val bgColor = when {
        isPrimary -> DialogAccentColor
        isDanger -> Color(0xFFE53935).copy(alpha = 0.1f)
        else -> Color(0xFF1E272E)
    }
    val textColor = when {
        isPrimary -> DialogDarkTextColor
        isDanger -> Color(0xFFE53935)
        else -> DialogContentColor
    }
    val borderColor = when {
        isPrimary -> Color.Transparent
        isDanger -> Color(0xFFE53935).copy(alpha = 0.5f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(bgColor).border(1.dp, borderColor, RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, fontSize = 14.sp)
    }
}
// --- ÚJ: TELJES KÉPERNYŐS TŰZIJÁTÉK ---
data class Particle(val vx: Float, val vy: Float, val color: Color, val size: Float)

@Composable
fun FireworksOverlay() {
    val particles = remember {
        List(150) { // 150 részecske a gigantikus robbanáshoz!
            Particle(
                vx = (Math.random() * 60 - 30).toFloat(), // Széles szórás oldalra
                vy = (Math.random() * -60 - 20).toFloat(), // Magasra fellövés
                color = Color(
                    red = (100..255).random() / 255f,
                    green = (100..255).random() / 255f,
                    blue = (100..255).random() / 255f,
                    alpha = 1f
                ),
                size = (Math.random() * 12 + 6).toFloat() // Véletlenszerű pötty-méretek
            )
        }
    }

    val progress = remember { Animatable(0f) }

    // Az animáció 2.5 másodpercig tart
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2500, easing = FastOutSlowInEasing)
        )
    }

    // Teljes képernyőt kitöltő vászon, ami még el is halványul a végén
    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 1f - (progress.value * 0.5f) }) {
        // A robbanás központja: X középen, Y a képernyő felső harmadában
        val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.35f)

        particles.forEach { p ->
            // A sebesség megszorozva az idővel adja meg a távolságot (plusz nehézségi gyorsulás az Y tengelyen)
            val currentX = centerOffset.x + (p.vx * progress.value * 50)
            val currentY = centerOffset.y + (p.vy * progress.value * 50 + 60f * progress.value * progress.value * 50)

            drawCircle(
                color = p.color.copy(alpha = 1f - progress.value), // Elhalványul menet közben
                radius = p.size * (1f - progress.value), // Zsugorodik
                center = androidx.compose.ui.geometry.Offset(currentX, currentY)
            )
        }
    }
}
