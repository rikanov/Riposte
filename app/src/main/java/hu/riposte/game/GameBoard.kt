package hu.riposte.game

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch

@Composable
fun RiposteGameBoard(
    gameViewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
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
    var previewThemeId by remember { mutableStateOf<String?>(null) }
    var localSavedThemeId by remember(appSettings.themeId) { mutableStateOf(appSettings.themeId) }
    var isGridVisible by remember { mutableStateOf(true) }

    val activeThemeId = previewThemeId ?: localSavedThemeId
    val activeTheme = remember(activeThemeId) { ThemeRegistry.getThemeById(activeThemeId) }

    val deviceTilt by rememberDeviceTilt()
    val shakeOffset = remember { Animatable(0f) }

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
                    launch {
                        shakeOffset.snapTo(15f)
                        shakeOffset.animateTo(-15f, animationSpec = tween(30))
                        shakeOffset.animateTo(10f, animationSpec = tween(30))
                        shakeOffset.animateTo(-10f, animationSpec = tween(40))
                        shakeOffset.animateTo(5f, animationSpec = tween(40))
                        shakeOffset.animateTo(0f, animationSpec = tween(50))
                    }
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

            // A FŐ RÉTEG: Itt dől el, mi van felül és alul!
            Box(modifier = Modifier.fillMaxSize()) {

                // 1. TELJES KÉPERNYŐS HÁTTÉR (Legalsó réteg)
                Image(
                    painter = painterResource(id = currentTheme.backgroundRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 1.1f
                            scaleY = 1.1f
                            val maxBgShift = 30f * density
                            translationX = (deviceTilt.x * 6f * density).coerceIn(-maxBgShift, maxBgShift)
                            translationY = (-deviceTilt.y * 6f * density).coerceIn(-maxBgShift, maxBgShift)
                        }
                )

                // 2. JÁTÉKTÁBLA
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, bottom = 180.dp)
                        .graphicsLayer {
                            scaleX = 0.95f
                            scaleY = 0.95f

                            val maxShiftX = 30f * density
                            val maxShiftY = 60f * density

                            translationX = (-deviceTilt.x * 3f * density).coerceIn(-maxShiftX, maxShiftX) + shakeOffset.value
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

                // --- 2.5 FILMES GAME OVER SÖTÉTÍTŐ RÉTEG ÉS SZÖVEG ---
                // Mivel ez a BoardArea és a BottomDock KÖZÖTT van, a dokkot nem fogja letakarni!
                AnimatedVisibility(
                    visible = gameViewModel.gamePhase == GameWaitingFor.GAME_OVER && !gameViewModel.isTutorialMode,
                    enter = fadeIn(animationSpec = tween(1500)),
                    exit = fadeOut(animationSpec = tween(800)),
                    modifier = Modifier.fillMaxSize() // A sötétítés kitölti a képernyőt
                ) {
                    val isWin = gameViewModel.winner?.contains("You", ignoreCase = true) == true || gameViewModel.winner?.contains("Player", ignoreCase = true) == true

                    val mainText = if (isWin) "VICTORY" else "DEFEAT"
                    val subText = if (isWin) "The bout is yours." else "The opponent claims the bout."
                    val textColor = if (isWin) currentTheme.uiAccentColor else Color(0xFFFF5555)

                    val infiniteTransition = rememberInfiniteTransition(label = "")
                    val textScale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
                        label = ""
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isWin) { FireworksOverlay() }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-40).dp)) {
                            Text(
                                text = mainText,
                                color = textColor,
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 8.sp,
                                modifier = Modifier.graphicsLayer { scaleX = textScale; scaleY = textScale }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = subText,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 3. BOTTOM DOCK (Fix - Legfelül!)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    // JAVÍTÁS: Animált átmenet a normál dokk és a Game Over gombok között!
                    AnimatedContent(
                        targetState = gameViewModel.gamePhase == GameWaitingFor.GAME_OVER && !gameViewModel.isTutorialMode,
                        label = "DockTransition"
                    ) { isGameOver ->
                        if (isGameOver) {
                            // --- GAME OVER GOMBOK ---
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Főmenü gomb
                                Text(
                                    text = "MAIN MENU",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null
                                        ) { soundManager.playClick(); onBackToMenu() }
                                        .padding(16.dp)
                                )

                                // Újraindítás (Rematch) gomb
                                Box(
                                    modifier = Modifier
                                        .background(currentTheme.uiAccentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .border(1.dp, currentTheme.uiAccentColor, RoundedCornerShape(8.dp))
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null
                                        ) { soundManager.playClick(); gameViewModel.restartGame() }
                                        .padding(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "REMATCH",
                                        color = currentTheme.uiAccentColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        } else {
                            // --- NORMÁL DOKK ---
                            RiposteBottomDock(
                                appSettings = appSettings,
                                isTutorialMode = gameViewModel.isTutorialMode,
                                isGridVisible = isGridVisible,
                                onGridToggle = { soundManager.playClick(); isGridVisible = it },
                                onMusicToggle = { soundManager.playClick(); coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(musicEnabled = !appSettings.musicEnabled)) } },
                                onSfxToggle = { soundManager.playToggle(!appSettings.sfxEnabled); coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(sfxEnabled = !appSettings.sfxEnabled)) } },
                                onNightModeToggle = { soundManager.playClick(); coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(nightModeEnabled = !appSettings.nightModeEnabled)) } },
                                onThemeClick = { soundManager.playClick(); showThemeDialog = true },
                                onMenuClick = { soundManager.playClick(); gameViewModel.isInterruptedGame = true; onBackToMenu()},
                                onSkipTutorial = { soundManager.playClick(); coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(hasSeenTutorial = true)) }; showTutorialComplete = true },
                                onUndoClick = { soundManager.playClick(); gameViewModel.undo() },
                                onHintClick = { soundManager.playClick(); gameViewModel.requestHint() }
                            )
                        }
                    }
                }
            }

            // --- DIALOGS ---
            if (gameViewModel.gamePhase == GameWaitingFor.TUTORIAL_WELCOME) { TutorialWelcomeDialog(soundManager = soundManager, onDismiss = { soundManager.playClick(); coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(hasSeenTutorial = true)) }; gameViewModel.startTutorial() }) }
            if (showTutorialComplete) { TutorialCompleteDialog(soundManager = soundManager, onBackToMenu = { showTutorialComplete = false; gameViewModel.isTutorialMode = false; gameViewModel.tutorialPhase = TutorialPhase.NOT_ACTIVE; onBackToMenu() }) }

            if (gameViewModel.gamePhase == GameWaitingFor.GAME_OVER && gameViewModel.isTutorialMode) {
                val isWin = gameViewModel.winner?.contains("You", ignoreCase = true) == true || gameViewModel.winner?.contains("Player", ignoreCase = true) == true
                if (isWin) {
                    TutorialCompleteDialog(soundManager = soundManager, onBackToMenu = { gameViewModel.isTutorialMode = false; gameViewModel.tutorialPhase = TutorialPhase.NOT_ACTIVE; onBackToMenu() })
                }
            }
            if (showThemeDialog) {
                ThemeSelectorDialog(currentThemeId = activeThemeId,
                    onThemeSelected = { finalId -> localSavedThemeId = finalId
                        previewThemeId = null; coroutineScope.launch {
                            settingsManager.updateSettings(appSettings.copy(themeId = finalId))
                        }
                        showThemeDialog = false},
                    onThemePreview = {
                            viewedId -> previewThemeId = viewedId
                                     },
                    onDismiss = {
                            previewThemeId = null
                            showThemeDialog = false
                                },
                    soundManager = soundManager)
            }
        }
    }
}