package hu.riposte.game.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import hu.riposte.game.ui.theme.LocalGameTheme
import hu.riposte.game.engine.data.GameWaitingFor
import hu.riposte.game.engine.data.SoundType
import hu.riposte.game.engine.data.TournamentRoster
import hu.riposte.game.engine.data.TutorialPhase
import hu.riposte.game.engine.logic.AppSettings
import hu.riposte.game.engine.logic.GameViewModel
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.engine.logic.ThemeSFX
import hu.riposte.game.engine.logic.createSettingsManager
import hu.riposte.game.engine.utils.RiposteBoardArea
import hu.riposte.game.engine.utils.rememberDeviceTilt
import hu.riposte.game.ui.theme.ThemeRegistry
import hu.riposte.game.ui.dialogs.OpponentCardOverlay
import hu.riposte.game.ui.components.RiposteBottomDock
import hu.riposte.game.ui.dialogs.ThemeSelectorDialog
import hu.riposte.game.ui.dialogs.TutorialCompleteOverlay
import hu.riposte.game.ui.dialogs.TutorialDefeatOverlay
import hu.riposte.game.ui.dialogs.TutorialWelcomeOverlay
import hu.riposte.game.ui.dialogs.GameOverOverlay
import hu.riposte.game.ui.dialogs.InfoSheetsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import org.jetbrains.compose.resources.*
import riposte.app.generated.resources.*

// --- PARTICLE MODEL ---
private data class DustParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)

@Composable
fun ParallaxDustVFX(deviceTilt: Offset, accentColor: Color) {
    val particles = remember {
        List(45) {
            DustParticle(
                x = (Math.random() * 2000).toFloat(),
                y = (Math.random() * 2000).toFloat(),
                speed = (Math.random() * 0.4f + 0.1f).toFloat(),
                size = (Math.random() * 6f + 2f).toFloat(),
                alpha = (Math.random() * 0.5f + 0.1f).toFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dust_move")
    val fallingAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Restart),
        label = "falling"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val currentY = (p.y + fallingAnim * p.speed) % size.height
            val parallaxX = deviceTilt.x * p.size * 3f
            val parallaxY = -deviceTilt.y * p.size * 3f

            val finalX = (p.x + parallaxX) % size.width
            val finalY = (currentY + parallaxY) % size.height

            val drawX = if (finalX < 0) finalX + size.width else finalX
            val drawY = if (finalY < 0) finalY + size.height else finalY

            drawCircle(
                color = accentColor.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(drawX, drawY)
            )
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = max(0, ms / 1000)
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    val mStr = if (m < 10) "0$m" else "$m"
    val sStr = if (s < 10) "0$s" else "$s"
    return "$mStr:$sStr"
}

@Composable
fun RiposteGameBoard(
    gameViewModel: GameViewModel,
    soundManager: SoundManager,
    onBackToMenu: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density

    val settingsManager = remember { createSettingsManager() }
    val appSettingsState = settingsManager.settingsFlow.collectAsState(initial = null)
    val appSettings = appSettingsState.value

    if (appSettings == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F141E)))
        return
    }

    var showTutorialComplete by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showInfoSheetsDialog by remember { mutableStateOf(false) }
    var showOpponentCard by remember { mutableStateOf(false) }
    var previewThemeId by remember { mutableStateOf<String?>(null) }
    var localSavedThemeId by remember(appSettings.themeId) { mutableStateOf(appSettings.themeId) }
    var isGridVisible by remember { mutableStateOf(true) }
    var showHaltePopup by remember { mutableStateOf(false) }
    var isReviewingGame by remember { mutableStateOf(false) }

    LaunchedEffect(gameViewModel.separationStepsLeft) {
        if (gameViewModel.separationStepsLeft == 2) {
            showHaltePopup = true
            delay(1500)
            showHaltePopup = false
        }
    }

    val activeThemeId = previewThemeId ?: localSavedThemeId
    val activeTheme = remember(activeThemeId) { ThemeRegistry.getThemeById(activeThemeId) }
    val deviceTilt by rememberDeviceTilt()
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(appSettings.musicEnabled, activeTheme.id) {
        soundManager.loadThemeSFX(
            ThemeSFX(
                moveSoundP1 = activeTheme.moveSoundP1Res,
                moveSoundP2 = activeTheme.moveSoundP2Res,
                toucheSound = activeTheme.toucheSoundRes
            )
        )
        soundManager.isMusicEnabled = appSettings.musicEnabled
        soundManager.playThemeMusic(activeTheme.bgMusicRes)
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
                        shakeOffset.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy))
                    }
                }
                SoundType.WIN -> if (appSettings.sfxEnabled) soundManager.playWin()
                SoundType.LOSE -> if (appSettings.sfxEnabled) soundManager.playLose()
            }
            gameViewModel.clearSoundEvent()
        }
    }

    // ROOT CONTAINER
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. ANIMATED GAME UI LAYER
        AnimatedContent(
            targetState = activeTheme,
            transitionSpec = { fadeIn(animationSpec = tween(800)) togetherWith fadeOut(animationSpec = tween(800)) },
            label = "ThemeTransition"
        ) { currentTheme ->

            CompositionLocalProvider(LocalGameTheme provides currentTheme) {

                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(currentTheme.backgroundRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = 1.15f; scaleY = 1.15f
                            val maxBgShift = 30f * density
                            translationX = (deviceTilt.x * 4.8f * density).coerceIn(-maxBgShift, maxBgShift)
                            translationY = (-deviceTilt.y * 6f * density).coerceIn(-maxBgShift, maxBgShift)
                        }
                    )
                    ParallaxDustVFX(deviceTilt = deviceTilt, accentColor = currentTheme.uiAccentColor)

                    // 2. GAME BOARD AND OVERLAYS
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
                                    deviceTilt = deviceTilt,
                                    isReviewMode = isReviewingGame
                                )

                                // --- TUTORIAL OVERLAYS ---
                                if (gameViewModel.gamePhase == GameWaitingFor.TUTORIAL_WELCOME) {
                                    TutorialWelcomeOverlay(
                                        soundManager = soundManager,
                                        onDismiss = {
                                            soundManager.playClick()
                                            coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(hasSeenTutorial = true)) }
                                            gameViewModel.startTutorial()
                                        }
                                    )
                                }

                                val isWin = gameViewModel.winner?.contains("You", ignoreCase = true) == true || gameViewModel.winner?.contains("Player", ignoreCase = true) == true
                                val isTutorialGameOver = gameViewModel.gamePhase == GameWaitingFor.GAME_OVER && gameViewModel.isTutorialMode

                                if (showTutorialComplete || (isTutorialGameOver && isWin)) {
                                    TutorialCompleteOverlay(
                                        soundManager = soundManager,
                                        onBackToMenu = {
                                            soundManager.playClick()
                                            showTutorialComplete = false
                                            gameViewModel.isTutorialMode = false
                                            gameViewModel.tutorialPhase = TutorialPhase.NOT_ACTIVE
                                            onBackToMenu()
                                        }
                                    )
                                }

                                if (isTutorialGameOver && !isWin) {
                                    TutorialDefeatOverlay(
                                        soundManager = soundManager,
                                        onDismiss = {
                                            soundManager.playClick()
                                            gameViewModel.gamePhase = GameWaitingFor.MOVE_PIECE
                                            gameViewModel.undo()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // --- 3. TOURNAMENT HEADLINE ---
                    if (gameViewModel.isTournamentMode && gameViewModel.tournamentOpponentNameRes != null) {
                        val targetRank = gameViewModel.tournamentTargetRank ?: 20
                        val playerTime = gameViewModel.playerTimeMs
                        val oppTime = gameViewModel.opponentTimeMs

                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.5f, targetValue = 1f,
                            animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse), label = "clockPulse"
                        )

                        val isPlayerActive = gameViewModel.currentPlayerId == 2 && gameViewModel.gamePhase == GameWaitingFor.MOVE_PIECE && !gameViewModel.isGamePaused
                        val isOppActive = gameViewModel.currentPlayerId == 1 && gameViewModel.gamePhase == GameWaitingFor.AI_MOVE && !gameViewModel.isGamePaused

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .background(Brush.verticalGradient(listOf(Color(0xFF0A0C10).copy(alpha = 0.8f), Color.Transparent), startY = 0f, endY = Float.POSITIVE_INFINITY))
                                .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 24.dp)
                        ) {
                            Column(modifier = Modifier.align(Alignment.TopStart).width(110.dp)) {
                                Text(
                                    text = gameViewModel.tournamentChallengerName,
                                    color = Color(0xFFD4AF37),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = currentTheme.fontFamily
                                )
                                Text(
                                    text = formatTime(playerTime),
                                    color = if (playerTime < 30000) Color(0xFFFF5555) else Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = currentTheme.fontFamily,
                                    modifier = Modifier.graphicsLayer { alpha = if (isPlayerActive) pulseAlpha else 0.4f }
                                )
                            }
                            Column(modifier = Modifier.align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(Res.string.tournament_rank_label, targetRank),
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = currentTheme.fontFamily
                                )
                                Text(
                                    text = stringResource(Res.string.tournament_vs),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = currentTheme.fontFamily
                                )
                            }
                            Column(modifier = Modifier.align(Alignment.TopEnd).width(110.dp), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(gameViewModel.tournamentOpponentNameRes!!),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.End,
                                    fontFamily = currentTheme.fontFamily
                                )
                                Text(
                                    text = formatTime(oppTime),
                                    color = if (oppTime < 30000) Color(0xFFFF5555) else Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = currentTheme.fontFamily,
                                    modifier = Modifier.graphicsLayer { alpha = if (isOppActive) pulseAlpha else 0.4f }
                                )
                            }
                        }
                    }

                    // --- 3.5 HALTE! POPUP ---
                    AnimatedVisibility(
                        visible = showHaltePopup,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 0.8f),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "HALTE!",
                                color = currentTheme.uiAccentColor,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = currentTheme.fontFamily,
                                modifier = Modifier.shadow(8.dp, spotColor = Color.Black)
                            )
                            Text(
                                text = "Corps à corps",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 18.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                fontFamily = currentTheme.fontFamily
                            )
                        }
                    }

                    // --- 4. GAME OVER DARKENING LAYER  ---
                    AnimatedVisibility(
                        visible = gameViewModel.gamePhase == GameWaitingFor.GAME_OVER && !gameViewModel.isTutorialMode,
                        enter = fadeIn(animationSpec = tween(1500)),
                        exit = fadeOut(animationSpec = tween(800)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val isWin = gameViewModel.winner?.contains("You", ignoreCase = true) == true || gameViewModel.winner?.contains("Player", ignoreCase = true) == true
                        val isTimeOut = gameViewModel.winner?.contains("Time", ignoreCase = true) == true

                        GameOverOverlay(
                            isWin = isWin,
                            isTimeOut = isTimeOut,
                            isTournamentMode = gameViewModel.tournamentOpponentNameRes != null,
                            isReviewingGame = isReviewingGame,
                            onStartReview = {
                                soundManager.playClick()
                                isReviewingGame = true
                            },
                            onStopReview = {
                                soundManager.playClick()
                                isReviewingGame = false
                                gameViewModel.endReviewMode()
                            },
                            onRematch = {
                                soundManager.playClick()
                                gameViewModel.restartGame()
                            },
                            onMainMenu = {
                                soundManager.playClick()
                                onBackToMenu()
                            },
                            onContinueTournament = {
                                soundManager.playClick()
                                onBackToMenu()
                            }
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
                            if (!isGameOver) {
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
                                    onInfoClick = {
                                        soundManager.playClick()
                                        showInfoSheetsDialog = true
                                        gameViewModel.isGamePaused = true
                                    },
                                    isTournamentMode = gameViewModel.isTournamentMode
                                )
                            }
                        }
                    }

                    // --- 6. OPPONENT'S CARD OVERLAY ---
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
                }
            }
        }

        // 2. PERSISTENT DIALOGS LAYER
        CompositionLocalProvider(LocalGameTheme provides activeTheme) {

            if (showThemeDialog) {
                ThemeSelectorDialog(
                    currentThemeId = localSavedThemeId,
                    onThemeSelected = { finalId ->
                        localSavedThemeId = finalId
                        coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(themeId = finalId)) }
                        previewThemeId = null
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

            if (showInfoSheetsDialog) {
                InfoSheetsDialog(
                    appSettings = appSettings,
                    soundManager = soundManager,
                    onDismiss = {
                        showInfoSheetsDialog = false
                        gameViewModel.isGamePaused = false
                    },
                    onSettingsUpdate = { newSettings ->
                        coroutineScope.launch { settingsManager.updateSettings(newSettings) }
                    }
                )
            }
        }
    }
}
