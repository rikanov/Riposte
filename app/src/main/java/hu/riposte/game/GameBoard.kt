package hu.riposte.game

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch

@Composable
fun RiposteGameBoard(
    gameViewModel: GameViewModel,
    onBackToMenu: () -> Unit // NAVIGÁCIÓS PARAMÉTER
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val soundManager = remember { SoundManager(context) }
    val settingsManager = remember { SettingsManager(context) }

    val appSettingsState = settingsManager.settingsFlow.collectAsState(initial = null)
    val appSettings = appSettingsState.value

    if (appSettings == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F141E)))
        return
    }

    var showMenu by remember { mutableStateOf(false) }
    var showTutorialComplete by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var previewThemeId by remember { mutableStateOf<String?>(null) }
    var localSavedThemeId by remember(appSettings.themeId) { mutableStateOf(appSettings.themeId) }
    var isGridVisible by remember { mutableStateOf(true) }

    val activeThemeId = previewThemeId ?: localSavedThemeId
    val activeTheme = remember(activeThemeId) { ThemeRegistry.getThemeById(activeThemeId) }

    // A Tutorial auto-indítást kivehetjük, mert a MainScreen-ből indul a gombbal,
    // de meghagyjuk biztonsági hálóként.

    LaunchedEffect(appSettings.musicEnabled, activeTheme.id) {
        soundManager.loadThemeSFX(activeTheme)
        soundManager.isMusicEnabled = appSettings.musicEnabled
        if (appSettings.musicEnabled) {
            soundManager.playThemeMusic(activeTheme.bgMusicRes)
        } else {
            soundManager.pauseMusic()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> soundManager.resumeMusic()
                Lifecycle.Event.ON_PAUSE -> soundManager.pauseMusic()
                Lifecycle.Event.ON_DESTROY -> soundManager.release()
                else -> {}
            }
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
                }
                SoundType.WIN ->  { if (appSettings.sfxEnabled) soundManager.playWin() }
                SoundType.LOSE -> { if (appSettings.sfxEnabled) soundManager.playLose() }
            }
        }
    }

    AnimatedContent(
        targetState = activeTheme,
        transitionSpec = { fadeIn(animationSpec = tween(800)) togetherWith fadeOut(animationSpec = tween(800)) },
        label = "ThemeTransition"
    ) { currentTheme ->

        CompositionLocalProvider(LocalGameTheme provides currentTheme) {

            // --- FULL SCREEN CONTAINER ---
            Box(modifier = Modifier.fillMaxSize()) {

                // 1. TELJES KÉPERNYŐS HÁTTÉR
                Image(
                    painter = painterResource(id = currentTheme.backgroundRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Sötétítő réteg a háttér felett
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)))

                // 2. JÁTÉKTÁBLA
                Box(modifier = Modifier.align(Alignment.Center)) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val targetAspect = 5f / 8f
                        val screenAspect = maxWidth / maxHeight

                        val containerWidth: androidx.compose.ui.unit.Dp
                        val containerHeight: androidx.compose.ui.unit.Dp

                        if (screenAspect > targetAspect) {
                            containerHeight = maxHeight
                            containerWidth = maxHeight * targetAspect
                        } else {
                            containerWidth = maxWidth
                            containerHeight = maxWidth / targetAspect
                        }

                        Box(modifier = Modifier.size(containerWidth, containerHeight)) {
                            RiposteBoardArea(
                                gameViewModel = gameViewModel,
                                isGridVisible = isGridVisible,
                                isAnimationEnabled = true,
                                isVisualAssistEnabled = appSettings.visualAssistsEnabled,
                                isNightModeEnabled = appSettings.nightModeEnabled
                            )
                        }
                    }
                }

                // 3. BOTTOM DOCK
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 16.dp)
                ) {
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
                        onThemeClick = { soundManager.playClick(); showThemeDialog = true },
                        onMenuClick = { soundManager.playClick(); showMenu = true },
                        onSkipTutorial = {
                            soundManager.playClick()
                            coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(hasSeenTutorial = true)) }
                            showTutorialComplete = true // Ez meghívja az új kilépő dialogot
                        },
                        onUndoClick = {
                            soundManager.playClick()
                            gameViewModel.undo()
                        },
                        onHintClick = {
                            soundManager.playClick()
                            gameViewModel.requestHint()
                        }
                    )
                }
            }

            // --- DIALOGS ---
            if (showMenu) {
                PauseMenuDialog(
                    soundManager = soundManager,
                    onResume = { showMenu = false },
                    onNewGame = {
                        // Mivel az új játékot már a Főmenüből indítjuk, ide küldjük a játékost
                        showMenu = false
                        onBackToMenu()
                    },
                    onOptions = { showMenu = false; showOptions = true },
                    onUndo = { gameViewModel.undo(); showMenu = false },
                    onExit = {
                        showMenu = false
                        onBackToMenu()
                    }
                )
            }

            if (showOptions) {
                OptionsDialog(soundManager = soundManager, settings = appSettings, onSettingsChange = { newSettings -> coroutineScope.launch { settingsManager.updateSettings(newSettings) } }, onClose = { showOptions = false })
            }

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

            if (gameViewModel.gamePhase == GameWaitingFor.GAME_OVER) {
                val isWin = gameViewModel.winner?.contains("You", ignoreCase = true) == true || gameViewModel.winner?.contains("Player", ignoreCase = true) == true
                if (isWin) { FireworksOverlay() }

                if (gameViewModel.isTutorialMode && isWin) {
                    TutorialCompleteDialog(
                        soundManager = soundManager,
                        onBackToMenu = {
                            gameViewModel.isTutorialMode = false
                            gameViewModel.tutorialPhase = TutorialPhase.NOT_ACTIVE
                            onBackToMenu()
                        }
                    )
                } else {
                    GameOverDialog(soundManager = soundManager, winnerName = gameViewModel.winner ?: "", onRestart = { gameViewModel.restartGame() })
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
                    },
                    onThemePreview = { viewedId -> previewThemeId = viewedId },
                    onDismiss = { previewThemeId = null; showThemeDialog = false },
                    soundManager = soundManager
                )
            }
        }
    }
}