package hu.riposte.game.engine.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import hu.riposte.game.ui.theme.LocalGameTheme
import hu.riposte.game.R
import hu.riposte.game.engine.data.GameWaitingFor
import hu.riposte.game.engine.data.SoundType
import hu.riposte.game.engine.data.TournamentRoster
import hu.riposte.game.engine.data.TutorialPhase
import hu.riposte.game.engine.logic.GameViewModel
import hu.riposte.game.engine.logic.SettingsManager
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.ThemeRegistry
import hu.riposte.game.ui.dialogs.OpponentCardOverlay
import hu.riposte.game.ui.components.RiposteBottomDock
import hu.riposte.game.ui.dialogs.DailyTipDialog
import hu.riposte.game.ui.dialogs.ThemeSelectorDialog
import hu.riposte.game.ui.dialogs.TutorialCompleteDialog
import hu.riposte.game.ui.dialogs.TutorialWelcomeDialog
import hu.riposte.game.ui.dialogs.GameOverOverlay
import kotlinx.coroutines.launch
import kotlin.math.max

// Segédfüggvény az idő formázására (mm:ss)
fun formatTime(ms: Long): String {
    val totalSeconds = max(0, ms / 1000)
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return java.lang.String.format("%02d:%02d", m, s)
}

@Composable
fun RiposteGameBoard(
    gameViewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density

    val soundManager = remember { SoundManager(context) }
    val settingsManager = remember { SettingsManager(context) }
    val appSettingsState = settingsManager.settingsFlow.collectAsState(initial = null)
    val appSettings = appSettingsState.value

    if (appSettings == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F141E)))
        return
    }

    var showTutorialComplete by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showOpponentCard by remember { mutableStateOf(false) }
    var previewThemeId by remember { mutableStateOf<String?>(null) }
    var localSavedThemeId by remember(appSettings.themeId) { mutableStateOf(appSettings.themeId) }
    var isGridVisible by remember { mutableStateOf(true) }

    val activeThemeId = previewThemeId ?: localSavedThemeId
    val activeTheme = remember(activeThemeId) { ThemeRegistry.getThemeById(activeThemeId) }
    val deviceTilt by rememberDeviceTilt()
    val shakeOffset = remember { Animatable(0f) }

    var showDailyTip by remember { mutableStateOf(false) }

    LaunchedEffect(appSettings.lastTipTime, gameViewModel.isTutorialMode, appSettings.showDailyTips) {
        if (!gameViewModel.isTutorialMode && appSettings.showDailyTips) {
            val now = System.currentTimeMillis()
            val twentyFourHoursInMillis = 60 * 1000L // 1 perc a teszteléshez

            if (now - appSettings.lastTipTime > twentyFourHoursInMillis) {
                showDailyTip = true
                gameViewModel.isGamePaused = true
            }
        }
    }

    LaunchedEffect(appSettings.musicEnabled, activeTheme.id) {
        soundManager.loadThemeSFX(activeTheme)
        soundManager.isMusicEnabled = appSettings.musicEnabled
        soundManager.playThemeMusic(activeTheme.bgMusicRes)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) soundManager.resumeMusic()
            if (event == Lifecycle.Event.ON_PAUSE) soundManager.pauseMusic()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(gameViewModel.soundEvent) {
        gameViewModel.soundEvent?.let { event ->
            when (event.type) {
                SoundType.MOVE -> {
                    if (appSettings.sfxEnabled) soundManager.playMove(event.playerId)
                    if (appSettings.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                SoundType.TOUCHE -> {
                    if (appSettings.sfxEnabled) soundManager.playTouche()
                    if (appSettings.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    launch {
                        shakeOffset.snapTo(12f)
                        shakeOffset.animateTo(
                            0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
                        )
                    }
                }
                SoundType.WIN -> if (appSettings.sfxEnabled) soundManager.playWin()
                SoundType.LOSE -> if (appSettings.sfxEnabled) soundManager.playLose()
            }
        }
    }

    AnimatedContent(
        targetState = activeTheme,
        transitionSpec = {
            fadeIn(animationSpec = tween(800)) togetherWith fadeOut(
                animationSpec = tween(800)
            )
        },
        label = "ThemeTransition"
    ) { currentTheme ->

        CompositionLocalProvider(LocalGameTheme provides currentTheme) {

            Box(modifier = Modifier.fillMaxSize()) {

                // 1. HÁTTÉR ÉS GIROSZKÓP
                Image(
                    painter = painterResource(id = currentTheme.backgroundRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        scaleX = 1.15f; scaleY = 1.15f
                        val maxBgShift = 30f * density
                        translationX = (deviceTilt.x * 4.8f * density).coerceIn(-maxBgShift, maxBgShift)
                        translationY = (-deviceTilt.y * 6f * density).coerceIn(-maxBgShift, maxBgShift)
                    }
                )

                // 2. JÁTÉKTÁBLA
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, bottom = 180.dp)
                        .graphicsLayer {
                            scaleX = 0.95f; scaleY = 0.95f
                            val maxShiftX = 30f * density
                            val maxShiftY = 60f * density
                            translationX = (-deviceTilt.x * 2.4f * density).coerceIn(-maxShiftX, maxShiftX) + shakeOffset.value
                            translationY = (deviceTilt.y * 3f * density).coerceIn(-maxShiftY, maxShiftY) + (shakeOffset.value * 0.5f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val targetAspect = 5f / 7f
                        val screenAspect = maxWidth / maxHeight
                        val containerWidth = if (screenAspect > targetAspect) maxHeight * targetAspect else maxWidth
                        val containerHeight = if (screenAspect > targetAspect) maxHeight else maxWidth / targetAspect

                        Box(modifier = Modifier.size(containerWidth, containerHeight)) {
                            RiposteBoardArea(
                                gameViewModel = gameViewModel,
                                isGridVisible = isGridVisible,
                                isAnimationEnabled = true,
                                isVisualAssistEnabled = appSettings.visualAssistsEnabled,
                                isNightModeEnabled = appSettings.nightModeEnabled,
                                isHapticEnabled = appSettings.hapticEnabled,
                                deviceTilt = deviceTilt
                            )
                        }
                    }
                }

                // --- 3. TOURNAMENT FEJLÉC (CHESS CLOCK) ---
                if (gameViewModel.isTournamentMode && gameViewModel.tournamentOpponentNameRes != null) {
                    val oppName = stringResource(id = gameViewModel.tournamentOpponentNameRes!!).uppercase()
                    val targetRank = gameViewModel.tournamentTargetRank ?: 20

                    val playerTime = gameViewModel.playerTimeMs
                    val oppTime = gameViewModel.opponentTimeMs

                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            tween(800, easing = EaseInOutSine),
                            RepeatMode.Reverse
                        ), label = "clockPulse"
                    )

                    val isPlayerActive = gameViewModel.currentPlayerId == 2 && gameViewModel.gamePhase == GameWaitingFor.MOVE_PIECE && !gameViewModel.isGamePaused
                    val isOppActive = gameViewModel.currentPlayerId == 1 && gameViewModel.gamePhase == GameWaitingFor.AI_MOVE && !gameViewModel.isGamePaused

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, start = 8.dp, end = 8.dp)
                            .align(Alignment.TopCenter)
                            .background(Color(0xFF0A0C10).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        // BAL OLDAL (Játékos)
                        Column(modifier = Modifier.align(Alignment.CenterStart).width(100.dp)) {
                            Text(
                                text = gameViewModel.tournamentChallengerName,
                                color = Color(0xFFD4AF37),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatTime(playerTime),
                                color = if (playerTime < 30000) Color(0xFFFF5555) else Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.graphicsLayer { alpha = if (isPlayerActive) pulseAlpha else 0.4f }
                            )
                        }

                        // KÖZÉP (TÉT)
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.tournament_rank_label, targetRank),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(id = R.string.tournament_vs),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // JOBB OLDAL (Ellenfél)
                        Column(
                            modifier = Modifier.align(Alignment.CenterEnd).width(100.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = oppName,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = formatTime(oppTime),
                                color = if (oppTime < 30000) Color(0xFFFF5555) else Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.graphicsLayer { alpha = if (isOppActive) pulseAlpha else 0.4f }
                            )
                        }
                    }
                }

                // --- 4. GAME OVER SÖTÉTÍTŐ RÉTEG ---
                AnimatedVisibility(
                    visible = gameViewModel.gamePhase == GameWaitingFor.GAME_OVER && !gameViewModel.isTutorialMode,
                    enter = fadeIn(animationSpec = tween(1500)),
                    exit = fadeOut(animationSpec = tween(800)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val isWin = gameViewModel.winner?.contains("You", ignoreCase = true) == true || gameViewModel.winner?.contains("Player", ignoreCase = true) == true
                    val isTimeOut = gameViewModel.winner?.contains("Time", ignoreCase = true) == true

                    val fallbackSubText = if (isWin) stringResource(id = R.string.game_over_bout_yours) else stringResource(id = R.string.game_over_bout_opponent)
                    val subText = gameViewModel.winner ?: fallbackSubText

                    // ITT HÍVJUK MEG AZ ÚJ KOMPONENST!
                    GameOverOverlay(
                        isWin = isWin,
                        isTimeOut = isTimeOut,
                        subText = subText,
                        accentColor = currentTheme.uiAccentColor
                    )
                }

                // --- 5. BOTTOM DOCK ---
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp).padding(horizontal = 16.dp)
                ) {
                    AnimatedContent(
                        targetState = gameViewModel.gamePhase == GameWaitingFor.GAME_OVER && !gameViewModel.isTutorialMode,
                        label = "DockTransition"
                    ) { isGameOver ->
                        if (isGameOver) {
                            if (gameViewModel.isTournamentMode) {
                                // BAJNOKSÁG GAME OVER GOMB
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                                        .background(currentTheme.uiAccentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .border(1.dp, currentTheme.uiAccentColor, RoundedCornerShape(8.dp))
                                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                            soundManager.playClick()
                                            val isWin = gameViewModel.winner?.contains("You", ignoreCase = true) == true || gameViewModel.winner?.contains("Time's Up! You", ignoreCase = true) == true

                                            val historyStr = gameViewModel.processTournamentMatchEnd(isWin)
                                            gameViewModel.isInterruptedGame = false

                                            coroutineScope.launch {
                                                settingsManager.updateSettings(
                                                    appSettings.copy(
                                                        tournamentRank = gameViewModel.tournamentManager.currentRank,
                                                        tournamentHighest = gameViewModel.tournamentManager.highestRank,
                                                        tournamentDefending = gameViewModel.tournamentManager.isDefending,
                                                        tournamentMatchHistory = historyStr,
                                                        hasSavedTournamentMatch = false
                                                    )
                                                )
                                            }
                                            onBackToMenu()
                                        }
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.btn_continue_tournament),
                                        color = currentTheme.uiAccentColor,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    )
                                }
                            } else {
                                // NORMÁL GAME OVER GOMBOK
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.btn_main_menu),
                                        color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
                                        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                            soundManager.playClick(); onBackToMenu()
                                        }.padding(16.dp)
                                    )
                                    Box(
                                        modifier = Modifier.background(currentTheme.uiAccentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .border(1.dp, currentTheme.uiAccentColor, RoundedCornerShape(8.dp))
                                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                                soundManager.playClick(); gameViewModel.restartGame()
                                            }
                                            .padding(horizontal = 24.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.btn_rematch),
                                            color = currentTheme.uiAccentColor,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 2.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            // NORMÁL DOKK
                            RiposteBottomDock(
                                appSettings = appSettings,
                                isTutorialMode = gameViewModel.isTutorialMode,
                                isGridVisible = isGridVisible,
                                onGridToggle = { soundManager.playClick(); isGridVisible = it },
                                onMusicToggle = {
                                    soundManager.playClick()
                                    coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(musicEnabled = !appSettings.musicEnabled)) }
                                },
                                onSfxToggle = {
                                    soundManager.playToggle(!appSettings.sfxEnabled)
                                    coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(sfxEnabled = !appSettings.sfxEnabled)) }
                                },
                                onNightModeToggle = {
                                    soundManager.playClick()
                                    coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(nightModeEnabled = !appSettings.nightModeEnabled)) }
                                },
                                onThemeClick = {
                                    soundManager.playClick()
                                    showThemeDialog = true
                                    gameViewModel.isGamePaused = true
                                },
                                onMenuClick = {
                                    soundManager.playClick()
                                    gameViewModel.isInterruptedGame = true
                                    gameViewModel.isGamePaused = true
                                    if (gameViewModel.isTournamentMode) {
                                        coroutineScope.launch { gameViewModel.saveTournamentStateToDisk() }
                                    }
                                    onBackToMenu()
                                },
                                onSkipTutorial = {
                                    soundManager.playClick()
                                    coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(hasSeenTutorial = true)) }
                                    showTutorialComplete = true
                                },
                                onUndoClick = { soundManager.playClick(); gameViewModel.undo() },
                                onHintClick = {
                                    soundManager.playClick()
                                    if (gameViewModel.isTournamentMode) {
                                        showOpponentCard = true
                                        gameViewModel.isGamePaused = true
                                    } else {
                                        gameViewModel.requestHint()
                                    }
                                },
                                isTournamentMode = gameViewModel.isTournamentMode
                            )
                        }
                    }
                }

                // --- 6. ELLENFÉL KÁRTYA OVERLAY ---
                if (gameViewModel.isTournamentMode) {
                    val opponent = TournamentRoster.opponents.values.find { it.nameRes == gameViewModel.tournamentOpponentNameRes }
                    OpponentCardOverlay(
                        opponent = opponent,
                        isVisible = showOpponentCard,
                        onClose = {
                            soundManager.playClick()
                            showOpponentCard = false
                            gameViewModel.isGamePaused = false
                        }
                    )
                }
            } // Box vége

            // --- 7. DIALOGOK ---
            if (gameViewModel.gamePhase == GameWaitingFor.TUTORIAL_WELCOME) {
                TutorialWelcomeDialog(
                    soundManager = soundManager,
                    onDismiss = {
                        soundManager.playClick()
                        coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(hasSeenTutorial = true)) }
                        gameViewModel.startTutorial()
                    }
                )
            }
            if (showTutorialComplete) {
                TutorialCompleteDialog(
                    soundManager = soundManager,
                    onBackToMenu = {
                        showTutorialComplete = false
                        gameViewModel.isTutorialMode = false
                        gameViewModel.tutorialPhase = TutorialPhase.NOT_ACTIVE
                        onBackToMenu()
                    }
                )
            }

            if (gameViewModel.gamePhase == GameWaitingFor.GAME_OVER && gameViewModel.isTutorialMode) {
                val isWin = gameViewModel.winner?.contains("You", ignoreCase = true) == true || gameViewModel.winner?.contains("Player", ignoreCase = true) == true
                if (isWin) {
                    TutorialCompleteDialog(
                        soundManager = soundManager,
                        onBackToMenu = {
                            gameViewModel.isTutorialMode = false
                            gameViewModel.tutorialPhase = TutorialPhase.NOT_ACTIVE
                            onBackToMenu()
                        }
                    )
                }
            }

            if (showThemeDialog) {
                ThemeSelectorDialog(
                    currentThemeId = activeThemeId,
                    onThemeSelected = { finalId ->
                        localSavedThemeId = finalId
                        previewThemeId = null
                        coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(themeId = finalId)) }
                        showThemeDialog = false
                        gameViewModel.isGamePaused = false
                    },
                    onThemePreview = { viewedId -> previewThemeId = viewedId },
                    onDismiss = {
                        previewThemeId = null
                        showThemeDialog = false
                        gameViewModel.isGamePaused = false
                    },
                    soundManager = soundManager
                )
            }

            // --- 8. DAILY TIP DIALOG ---
            if (showDailyTip) {
                DailyTipDialog(
                    soundManager = soundManager,
                    onDismiss = {
                        showDailyTip = false
                        gameViewModel.isGamePaused = false
                        coroutineScope.launch {
                            settingsManager.updateSettings(appSettings.copy(lastTipTime = System.currentTimeMillis()))
                        }
                    },
                    onTurnOff = {
                        coroutineScope.launch {
                            settingsManager.updateSettings(
                                appSettings.copy(
                                    showDailyTips = false,
                                    lastTipTime = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                )
            }
        } // CompositionLocalProvider vége
    } // AnimatedContent vége
}