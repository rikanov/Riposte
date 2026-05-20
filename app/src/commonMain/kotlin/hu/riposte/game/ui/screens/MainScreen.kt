package hu.riposte.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.ui.theme.LocalGameTheme
import riposte.app.generated.resources.*
import org.jetbrains.compose.resources.*
import hu.riposte.game.engine.logic.AppSettings
import hu.riposte.game.engine.logic.createSettingsManager
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.ThemeRegistry
import hu.riposte.game.engine.data.GameMode
import hu.riposte.game.engine.data.GameSettings
import hu.riposte.game.engine.data.StartingPlayer
import hu.riposte.game.engine.logic.GameViewModel
import hu.riposte.game.ui.components.InteractiveMainMenu
import hu.riposte.game.ui.components.MainMenuItem
import hu.riposte.game.ui.dialogs.GlassDialog
import hu.riposte.game.ui.dialogs.OptionsDialog
import hu.riposte.game.ui.dialogs.PremiumUnlockDialog
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    isInterruptedGame: Boolean,
    gameViewModel: GameViewModel,
    soundManager: SoundManager,
    onResumeGame: () -> Unit,
    onNavigateToTournament: () -> Unit,
    onNavigateToTutorial: () -> Unit,
    onNavigateToAiTraining: () -> Unit,
    onNavigateToLocal: () -> Unit,
    onExitGame: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val settingsManager = remember { createSettingsManager() }
    val appSettingsState = settingsManager.settingsFlow.collectAsState(initial = null)
    val appSettings = appSettingsState.value

    var showPremiumDialog by remember { mutableStateOf(false) }
    var showForfeitWarningDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var pendingAction: (() -> Unit)? by remember { mutableStateOf(null) }

    val hasSavedTourney = appSettings?.hasSavedTournamentMatch == true

    LaunchedEffect(appSettings?.musicEnabled) {
        appSettings?.let { settings ->
            soundManager.isMusicEnabled = settings.musicEnabled
            if (settings.musicEnabled) soundManager.playThemeMusic("main_menu_music.ogg")
            else soundManager.pauseMusic()
        }
    }

    val menuItems = listOf(
        MainMenuItem(stringResource(Res.string.menu_resume), isEnabled = isInterruptedGame, action = onResumeGame),
        MainMenuItem(stringResource(Res.string.menu_quick_tutorial), needsAttention = appSettings?.hasSeenTutorial == false) {
            gameViewModel.startTutorial()
            onNavigateToTutorial()
        },
        MainMenuItem(stringResource(Res.string.menu_training_ai), hasSwipeAction = true, action = {}),
        MainMenuItem(stringResource(Res.string.menu_local_players)) {
            val startLocal = {
                gameViewModel.startNewGame(
                    GameSettings(
                        gameMode = GameMode.LOCAL_MULTIPLAYER,
                        startingPlayer = StartingPlayer.ALTERNATING,
                        difficulty = 5,
                        riposteAllowed = true
                    ),
                    isTournament = false
                )
                onNavigateToLocal()
            }
            if (hasSavedTourney) {
                pendingAction = startLocal
                showForfeitWarningDialog = true
            } else {
                startLocal()
            }
        },
        MainMenuItem(stringResource(Res.string.menu_tournament), action = onNavigateToTournament),
        MainMenuItem(stringResource(Res.string.menu_options), action = { showOptionsDialog = true }),
        MainMenuItem(stringResource(Res.string.menu_exit), action = onExitGame)
    )

    val handleAiClick: (Int, Int, Int, Boolean) -> Unit = { difficulty, offW, defW, isPremium ->
        if (isPremium && !gameViewModel.isPremiumVersion) {
            showPremiumDialog = true
        } else if (hasSavedTourney) {
            pendingAction = {
                gameViewModel.startNewGame(GameSettings(difficulty = difficulty, offensiveWeight = offW, defensiveWeight = defW), isTournament = false)
                onNavigateToAiTraining()
            }
            showForfeitWarningDialog = true
        } else {
            gameViewModel.startNewGame(GameSettings(difficulty = difficulty))
            onNavigateToAiTraining()
        }
    }

    val aiDifficultyItems = listOf(
        MainMenuItem(stringResource(Res.string.ai_apprentice)) { handleAiClick(3, 0, 0, false) },
        MainMenuItem(stringResource(Res.string.ai_swordsman)) { handleAiClick(5, 0, 0, false) },
        MainMenuItem(stringResource(Res.string.ai_duelist), isPremiumOnly = true) { handleAiClick(3, 10, 5, true) },
        MainMenuItem(stringResource(Res.string.ai_master), isPremiumOnly = true) { handleAiClick(5, 10, 10, true) },
        MainMenuItem(stringResource(Res.string.ai_grandmaster), isPremiumOnly = true) { handleAiClick(7, 10, 10, true) },
        MainMenuItem(stringResource(Res.string.ai_stygian), isPremiumOnly = true) { handleAiClick(8, 15, 5, true) }
    )

    val activeThemeId = appSettings?.themeId ?: "abstract_sunrise"
    val activeTheme = remember(activeThemeId) { ThemeRegistry.getThemeById(activeThemeId) }

    CompositionLocalProvider(LocalGameTheme provides activeTheme) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            // 1. BACKGROUND
            Image(
                painter = painterResource(Res.drawable.main_menu_diorama_bg),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 2. LOGO
            Image(
                painter = painterResource(Res.drawable.main_typography),
                contentDescription = "La Riposte Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 8.dp)
                    .width(screenWidth * 0.75f)
                    .graphicsLayer { alpha = 0.99f }
            )

            // 3. MAIN MENU COMPONENT
            InteractiveMainMenu(
                modifier = Modifier.align(Alignment.BottomEnd),
                menuItems = menuItems,
                aiDifficultyItems = aiDifficultyItems,
                isPremiumVersion = gameViewModel.isPremiumVersion,
                soundManager = soundManager,
                haptic = haptic,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            // 4. DIALOGS
            if (showPremiumDialog) {
                PremiumUnlockDialog(
                    soundManager = soundManager,
                    onDismiss = { showPremiumDialog = false }
                )
            }

            if (showForfeitWarningDialog) {
                GlassDialog(onDismissRequest = { showForfeitWarningDialog = false }) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.warning_title),
                            color = Color(0xFFFF5555), fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(Res.string.warning_forfeit_tournament),
                            color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center, fontSize = 14.sp
                        )
                        Spacer(Modifier.height(32.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(Res.string.btn_cancel),
                                color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    soundManager.playClick(); showForfeitWarningDialog = false
                                }.padding(8.dp)
                            )
                            Text(
                                text = stringResource(Res.string.btn_forfeit_match),
                                color = Color(0xFFFF5555), fontWeight = FontWeight.Black,
                                modifier = Modifier.clickable {
                                    soundManager.playClick()
                                    showForfeitWarningDialog = false
                                    gameViewModel.forfeitTournamentMatch()
                                    pendingAction?.invoke()
                                }.padding(8.dp)
                            )
                        }
                    }
                }
            }

            if (showOptionsDialog && appSettings != null) {
                OptionsDialog(
                    appSettings = appSettings,
                    soundManager = soundManager,
                    onSettingsChanged = { newSettings ->
                        coroutineScope.launch {
                            settingsManager.updateSettings(newSettings)
                        }
                    },
                    onDismiss = {
                        soundManager.playClick()
                        showOptionsDialog = false
                    }
                )
            }
        }
    }
}
