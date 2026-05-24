package hu.riposte.game.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import riposte.app.generated.resources.*
import hu.riposte.game.engine.logic.AppSettings
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.components.RiposteSystemButton
import hu.riposte.game.ui.components.RiposteSystemToggleButton

private val SystemAccent = Color(0xFFD4AF37)

@Composable
fun OptionsDialog(
    appSettings: AppSettings,
    soundManager: SoundManager,
    onSettingsChanged: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var showAbout by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    if (showAbout) {
        AboutDialog(soundManager = soundManager, onDismiss = { showAbout = false })
        return
    }

    if (showResetConfirm) {
        ResetConfirmationDialog(
            soundManager = soundManager,
            onConfirm = {
                onSettingsChanged(appSettings.copy(
                    tournamentRank = 20,
                    tournamentHighest = 20,
                    tournamentDefending = false,
                    tournamentMatchHistory = "",
                    tournamentScoreHistory = "",
                    highestRating = 1000,
                    hasSavedTournamentMatch = false
                ))
                showResetConfirm = false
                onDismiss()
            },
            onCancel = { showResetConfirm = false }
        )
        return
    }

    GlassDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.options_title),
                color = SystemAccent, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- AUDIO & HAPTICS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RiposteSystemToggleButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.options_music),
                    isActive = appSettings.musicEnabled,
                    onClick = {
                        soundManager.playClick()
                        onSettingsChanged(appSettings.copy(musicEnabled = !appSettings.musicEnabled))
                    }
                )

                RiposteSystemToggleButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.options_sfx),
                    isActive = appSettings.sfxEnabled,
                    onClick = {
                        soundManager.playToggle(!appSettings.sfxEnabled)
                        onSettingsChanged(appSettings.copy(sfxEnabled = !appSettings.sfxEnabled))
                    }
                )

                RiposteSystemToggleButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.options_haptic),
                    isActive = appSettings.hapticEnabled,
                    onClick = {
                        soundManager.playClick()
                        onSettingsChanged(appSettings.copy(hapticEnabled = !appSettings.hapticEnabled))
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- LORE & RESET TOURNAMENT ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RiposteSystemToggleButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.options_unlock_profiles),
                    isActive = appSettings.loreUnlocked,
                    onClick = {
                        soundManager.playClick()
                        onSettingsChanged(appSettings.copy(loreUnlocked = !appSettings.loreUnlocked))
                    }
                )

                RiposteSystemButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.options_reset_tournament),
                    onClick = {
                        soundManager.playClick()
                        showResetConfirm = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.options_about),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { soundManager.playClick(); showAbout = true }.padding(8.dp)
                )

                RiposteSystemButton(
                    text = stringResource(Res.string.btn_ok),
                    isHanging = true,
                    onClick = { soundManager.playClick(); onDismiss() }
                )
            }
        }
    }
}

@Composable
fun ResetConfirmationDialog(
    soundManager: SoundManager,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    GlassDialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.options_reset_warning_title),
                color = Color(0xFFFF5555),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.options_reset_warning_msg),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RiposteSystemButton(
                    text = stringResource(Res.string.btn_back),
                    modifier = Modifier.weight(1f),
                    onClick = { soundManager.playClick(); onCancel() }
                )

                RiposteSystemButton(
                    text = stringResource(Res.string.options_btn_erase),
                    modifier = Modifier.weight(1f),
                    isHanging = true,
                    onClick = { soundManager.playClick(); onConfirm() }
                )
            }
        }
    }
}

@Composable
fun AboutDialog(
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    GlassDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.app_name_title),
                color = SystemAccent, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp
            )

            Text(
                text = stringResource(Res.string.app_version),
                color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
                text = stringResource(Res.string.about_description),
                color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp, modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = stringResource(Res.string.about_developer),
                color = SystemAccent.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp)
            )

            RiposteSystemButton(
                text = stringResource(Res.string.btn_close),
                modifier = Modifier.fillMaxWidth(0.6f),
                onClick = { soundManager.playClick(); onDismiss() }
            )
        }
    }
}