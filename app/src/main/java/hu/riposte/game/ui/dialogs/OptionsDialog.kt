package hu.riposte.game.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.R
import hu.riposte.game.engine.logic.AppSettings
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.LocalGameTheme

@Composable
fun OptionsDialog(
    appSettings: AppSettings,
    soundManager: SoundManager,
    onSettingsChanged: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor
    var showAbout by remember { mutableStateOf(false) }

    if (showAbout) {
        AboutDialog(
            soundManager = soundManager,
            onDismiss = { showAbout = false }
        )
        return // Amíg az About nyitva van, az Options-t elrejtjük
    }

    GlassDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.options_title),
                color = accentColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- AUDIO & HAPTICS (Első sor) ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OptionToggleButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.options_music),
                    isActive = appSettings.musicEnabled,
                    accentColor = accentColor
                ) {
                    soundManager.playClick()
                    onSettingsChanged(appSettings.copy(musicEnabled = !appSettings.musicEnabled))
                }

                OptionToggleButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.options_sfx),
                    isActive = appSettings.sfxEnabled,
                    accentColor = accentColor
                ) {
                    soundManager.playToggle(!appSettings.sfxEnabled)
                    onSettingsChanged(appSettings.copy(sfxEnabled = !appSettings.sfxEnabled))
                }

                OptionToggleButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.options_haptic),
                    isActive = appSettings.hapticEnabled,
                    accentColor = accentColor
                ) {
                    soundManager.playClick()
                    onSettingsChanged(appSettings.copy(hapticEnabled = !appSettings.hapticEnabled))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- JÁTÉKMENET (Második sor) ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OptionToggleButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.options_grid),
                    isActive = appSettings.visualAssistsEnabled,
                    accentColor = accentColor
                ) {
                    soundManager.playClick()
                    onSettingsChanged(appSettings.copy(visualAssistsEnabled = !appSettings.visualAssistsEnabled))
                }

                OptionToggleButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.options_notify),
                    isActive = appSettings.showDailyTips,
                    accentColor = accentColor
                ) {
                    soundManager.playClick()
                    onSettingsChanged(appSettings.copy(showDailyTips = !appSettings.showDailyTips))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- ALSÓ SOR: ABOUT ÉS BEZÁRÁS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.options_about),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { soundManager.playClick(); showAbout = true }
                        .padding(8.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor)
                        .clickable { soundManager.playClick(); onDismiss() }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_ok),
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun OptionToggleButton(
    modifier: Modifier = Modifier,
    text: String,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) accentColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                if (isActive) accentColor else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isActive) accentColor else Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AboutDialog(
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    GlassDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.app_name_title),
                color = accentColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp
            )

            Text(
                text = stringResource(id = R.string.app_version),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = stringResource(id = R.string.about_description),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = stringResource(id = R.string.about_developer),
                color = accentColor.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                    .clickable { soundManager.playClick(); onDismiss() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.btn_close),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}