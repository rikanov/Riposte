package hu.riposte.game

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import hu.riposte.game.R // Biztosítjuk, hogy a mi strings.xml-ünket húzza be

// =========================================================================================
// 1. FŐ KONTÉNER (Virtuális Kijelző és Állapotkezelés)
// =========================================================================================
@Composable
fun RiposteGameBoard(gameViewModel: GameViewModel) {
    val context = LocalContext.current
    val activity = (context as? Activity)
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

    // UI State
    var showMenu by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var previewThemeId by remember { mutableStateOf<String?>(null) }
    var localSavedThemeId by remember(appSettings.themeId) { mutableStateOf(appSettings.themeId) }
    var isGridVisible by remember { mutableStateOf(true) }
    var isAnimationEnabled by remember { mutableStateOf(true) }

    val activeThemeId = previewThemeId ?: localSavedThemeId
    val activeTheme = remember(activeThemeId) { ThemeRegistry.getThemeById(activeThemeId) }

    // Életciklus és Hang logika
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

            // --- A VIRTUÁLIS KIJELZŐ (SCALING) LOGIKA ---
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().background(Color(0xFF0F141E)),
                contentAlignment = Alignment.Center
            ) {
                // Cél arány 5:8
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

                Box(
                    modifier = Modifier
                        .size(containerWidth, containerHeight)
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // 1. TOP BAR KOMPONENS
                        RiposteTopBar(
                            appSettings = appSettings,
                            isGridVisible = isGridVisible,
                            isAnimationEnabled = isAnimationEnabled,
                            onGridToggle = { soundManager.playClick(); isGridVisible = it },
                            onAnimationToggle = { soundManager.playClick(); isAnimationEnabled = !isAnimationEnabled },
                            onMusicToggle = {
                                soundManager.playClick()
                                coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(musicEnabled = !appSettings.musicEnabled)) }
                            },
                            onSfxToggle = {
                                soundManager.playToggle(!appSettings.sfxEnabled)
                                coroutineScope.launch { settingsManager.updateSettings(appSettings.copy(sfxEnabled = !appSettings.sfxEnabled)) }
                            },
                            onThemeClick = { soundManager.playClick(); showThemeDialog = true },
                            onMenuClick = { soundManager.playClick(); showMenu = true }
                        )

                        // 2. JÁTÉKTÁBLA KOMPONENS
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            RiposteBoardArea(
                                gameViewModel = gameViewModel,
                                isGridVisible = isGridVisible,
                                isAnimationEnabled = isAnimationEnabled
                            )
                        }

                        // 3. STATUS KOMPONENS
                        RiposteStatusIndicator(gameViewModel = gameViewModel)
                    }
                }
            }

            // --- DIALÓGUSOK ---
            if (showMenu) { PauseMenuDialog(soundManager = soundManager, onResume = { showMenu = false }, onNewGame = { gameViewModel.gamePhase = GameWaitingFor.SETUP; showMenu = false }, onOptions = { showMenu = false; showOptions = true }, onUndo = { gameViewModel.undo(); showMenu = false }, onExit = { activity?.finish(); showMenu = false }) }
            if (showOptions) { OptionsDialog(soundManager = soundManager, settings = appSettings, onSettingsChange = { newSettings -> coroutineScope.launch { settingsManager.updateSettings(newSettings) } }, onClose = { showOptions = false }) }
            if (gameViewModel.gamePhase == GameWaitingFor.SETUP) { MainMenuDialog(soundManager = soundManager, appSettings = appSettings, onStart = { settings -> gameViewModel.startNewGame(settings) }, onCancel = { gameViewModel.gamePhase = GameWaitingFor.MOVE_PIECE }) }
            if (gameViewModel.gamePhase == GameWaitingFor.GAME_OVER) {
                val isWin = gameViewModel.winner?.contains("You", ignoreCase = true) == true || gameViewModel.winner?.contains("Player", ignoreCase = true) == true
                if (isWin) { FireworksOverlay() }
                GameOverDialog(soundManager = soundManager, winnerName = gameViewModel.winner ?: "", onRestart = { gameViewModel.restartGame() })
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

// =========================================================================================
// 2. TOP BAR KOMPONENS
// =========================================================================================
@Composable
fun RiposteTopBar(
    appSettings: AppSettings,
    isGridVisible: Boolean,
    isAnimationEnabled: Boolean,
    onGridToggle: (Boolean) -> Unit,
    onAnimationToggle: () -> Unit,
    onMusicToggle: () -> Unit,
    onSfxToggle: () -> Unit,
    onThemeClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val currentTheme = LocalGameTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        currentTheme.containerColor.copy(alpha = 0.95f),
                        currentTheme.containerColor.copy(alpha = 0.70f)
                    )
                )
            )
            .border(1.dp, currentTheme.textColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ){
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onAnimationToggle, modifier = Modifier.size(44.dp)) {
            val animIcon = if (isAnimationEnabled) Icons.Default.PlayCircle else Icons.Default.PauseCircle
            val iconAlpha = if (isAnimationEnabled) 1f else 0.4f
            Icon(animIcon, contentDescription = stringResource(R.string.cd_toggle_animation), tint = currentTheme.textColor.copy(alpha = iconAlpha), modifier = Modifier.size(34.dp))
        }

        IconButton(onClick = onMusicToggle, modifier = Modifier.size(44.dp)) {
            val musicIcon = if (appSettings.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff
            val iconAlpha = if (appSettings.musicEnabled) 1f else 0.4f
            Icon(musicIcon, contentDescription = stringResource(R.string.cd_toggle_music), tint = currentTheme.textColor.copy(alpha = iconAlpha), modifier = Modifier.size(34.dp))
        }

        IconButton(onClick = onSfxToggle, modifier = Modifier.size(44.dp)) {
            val sfxIcon = if (appSettings.sfxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff
            val iconAlpha = if (appSettings.sfxEnabled) 1f else 0.4f
            Icon(sfxIcon, contentDescription = stringResource(R.string.cd_toggle_sfx), tint = currentTheme.textColor.copy(alpha = iconAlpha), modifier = Modifier.size(34.dp))
        }

        Checkbox(
            checked = isGridVisible,
            onCheckedChange = onGridToggle,
            colors = CheckboxDefaults.colors(
                checkedColor = currentTheme.textColor,
                checkmarkColor = Color(0xFF0F141E),
                uncheckedColor = currentTheme.textColor.copy(alpha = 0.5f)
            ),
            modifier = Modifier.scale(1.2f).padding(horizontal = 4.dp)
        )

        IconButton(onClick = onThemeClick, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Default.Star, contentDescription = stringResource(R.string.cd_themes), tint = currentTheme.textColor, modifier = Modifier.size(34.dp))
        }

        IconButton(onClick = onMenuClick, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_menu), tint = currentTheme.textColor, modifier = Modifier.size(34.dp))
        }
    }
}

// =========================================================================================
// 3. STATUS INDICATOR KOMPONENS
// =========================================================================================
@Composable
fun RiposteStatusIndicator(gameViewModel: GameViewModel) {
    val currentTheme = LocalGameTheme.current
    val density = LocalDensity.current

    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        var displayPhase by remember { mutableStateOf(gameViewModel.gamePhase) }
        val flipRotation = remember { Animatable(0f) }
        val shimmerProgress = remember { Animatable(-1f) }

        LaunchedEffect(gameViewModel.gamePhase) {
            if (displayPhase != gameViewModel.gamePhase) {
                flipRotation.animateTo(90f, animationSpec = tween(300, easing = LinearEasing))
                displayPhase = gameViewModel.gamePhase
                flipRotation.snapTo(-90f)
                flipRotation.animateTo(0f, animationSpec = tween(350, easing = LinearOutSlowInEasing))
                shimmerProgress.snapTo(-0.5f)
                shimmerProgress.animateTo(1.5f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
            }
        }

        val containerColor = when (displayPhase) {
            GameWaitingFor.AI_MOVE -> Color(0xFFFFEBEE)
            GameWaitingFor.TAKE_PIECE -> Color(0xFFFFF8E1)
            GameWaitingFor.GAME_OVER -> Color(0xFFE8F5E9)
            else -> currentTheme.containerColor
        }
        val contentColor = when (displayPhase) {
            GameWaitingFor.AI_MOVE -> Color(0xFFD32F2F)
            GameWaitingFor.TAKE_PIECE -> Color(0xFFF57F17)
            GameWaitingFor.GAME_OVER -> Color(0xFF2E7D32)
            else -> currentTheme.textColor
        }

        // JAVÍTÁS: Dinamikus hivatkozások a szótárból
        val statusText = when (displayPhase) {
            GameWaitingFor.MOVE_PIECE -> stringResource(R.string.status_players_turn)
            GameWaitingFor.AI_MOVE -> stringResource(R.string.status_ai_thinking)
            GameWaitingFor.TAKE_PIECE -> stringResource(R.string.status_select_capture)
            GameWaitingFor.ANIMATION -> stringResource(R.string.status_wait)
            GameWaitingFor.GAME_OVER -> stringResource(R.string.status_game_over, gameViewModel.winner ?: "")
            else -> stringResource(R.string.status_setting_up)
        }

        val metalShape = CutCornerShape(10.dp)
        val metallicBrush = Brush.linearGradient(
            colors = listOf(
                containerColor.copy(alpha = 0.8f),
                Color.White.copy(alpha = 0.9f),
                containerColor,
                containerColor.copy(alpha = 0.6f),
                containerColor
            )
        )

        Row(
            modifier = Modifier
                .graphicsLayer {
                    rotationX = flipRotation.value
                    cameraDistance = 12f * density.density
                }
                .shadow(6.dp, metalShape)
                .clip(metalShape)
                .background(metallicBrush)
                .drawBehind {
                    val lineColor = Color.Black.copy(alpha = 0.05f)
                    val strokeWidth = 1.dp.toPx()
                    var yPos = 0f
                    while (yPos < size.height) {
                        drawLine(color = lineColor, start = Offset(0f, yPos), end = Offset(size.width, yPos), strokeWidth = strokeWidth)
                        yPos += 3.dp.toPx()
                    }
                }
                .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.3f))), shape = metalShape)
                .drawWithContent {
                    drawContent()
                    if (shimmerProgress.value > -0.5f && shimmerProgress.value < 1.5f) {
                        val w = size.width
                        val h = size.height
                        val xOffset = w * shimmerProgress.value
                        val shimmerBrush = Brush.linearGradient(colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.9f), Color.Transparent), start = Offset(xOffset, 0f), end = Offset(xOffset + 80.dp.toPx(), h))
                        drawRect(brush = shimmerBrush, blendMode = BlendMode.SrcAtop)
                    }
                }
                .padding(horizontal = 32.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = statusText.uppercase(), fontWeight = FontWeight.Black, color = contentColor, fontSize = 15.sp, letterSpacing = 1.5.sp)
        }
    }
}

// =========================================================================================
// 4. BOARD AREA KOMPONENS
// =========================================================================================
@Composable
fun RiposteBoardArea(
    gameViewModel: GameViewModel,
    isGridVisible: Boolean,
    isAnimationEnabled: Boolean
) {
    val currentTheme = LocalGameTheme.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(5f / 7f)
            .shadow(24.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = painterResource(id = currentTheme.backgroundRes),
            contentDescription = stringResource(R.string.cd_board_background),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        )

        val boardWidth = constraints.maxWidth.toFloat()
        val boardHeight = constraints.maxHeight.toFloat()
        val cellWidth = boardWidth / 5
        val cellHeight = boardHeight / 7
        val density = LocalDensity.current
        val cellWidthDp = with(density) { cellWidth.toDp() }
        val cellHeightDp = with(density) { cellHeight.toDp() }

        val gridAlpha by animateFloatAsState(targetValue = if (isGridVisible) 1f else 0f, animationSpec = tween(500), label = "grid_alpha")
        Column(modifier = Modifier.fillMaxSize().padding(4.dp).graphicsLayer { alpha = gridAlpha }) {
            for (y in 0 until 7) {
                Row(modifier = Modifier.weight(1f)) {
                    for (x in 0 until 5) {
                        val isDark = (x + y) % 2 == 0
                        val tileGradient = if (isDark) Brush.linearGradient(listOf(currentTheme.boardCellDark, Color.Transparent))
                        else Brush.linearGradient(listOf(currentTheme.boardCellLight, currentTheme.boardCellLight.copy(alpha = 0.3f)))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(3.dp)
                                .background(tileGradient, RoundedCornerShape(8.dp))
                                .drawBehind {
                                    val lineColor = Color.White.copy(alpha = 0.03f)
                                    val strokeWidth = 1.dp.toPx()
                                    var xPos = -size.height
                                    while (xPos < size.width) {
                                        drawLine(color = lineColor, start = Offset(xPos, 0f), end = Offset(xPos + size.height, size.height), strokeWidth = strokeWidth)
                                        xPos += 4.dp.toPx()
                                    }
                                }
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
        val toucheIdx = gameViewModel.board.indexOf(4)
        val infiniteTransitionTouche = rememberInfiniteTransition(label = "TouchePulseAnim")

        val goldScale by infiniteTransitionTouche.animateFloat(
            initialValue = currentTheme.toucheScaleMin,
            targetValue = currentTheme.toucheScaleMax,
            animationSpec = infiniteRepeatable(
                tween(currentTheme.touchePulseDuration, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = "GoldPulse"
        )

        val goldRotationZ by infiniteTransitionTouche.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                tween(currentTheme.toucheRotationDuration, easing = LinearEasing),
                RepeatMode.Restart
            ),
            label = "GoldRotate"
        )
        var starX: Float? = null
        var starY: Float? = null

        if (toucheIdx != -1) {
            val tPos = Coord.fromIndex(toucheIdx)
            starX = tPos.x * cellWidth
            starY = tPos.y * cellHeight
            val shimmerIntensity = ((goldScale - 0.8f) * 5f).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .size(cellWidthDp, cellHeightDp)
                    .offset { IntOffset(starX!!.roundToInt(), starY!!.roundToInt()) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(1.8f).drawBehind {
                        val glowRadius = (size.minDimension / 2f) * (0.8f + 0.4f * shimmerIntensity)
                        val glowAlpha = 0.2f + 0.5f * shimmerIntensity
                        val glowBrush = Brush.radialGradient(colors = listOf(currentTheme.auraP1Color.copy(alpha = glowAlpha), currentTheme.auraP1Color.copy(alpha = glowAlpha * 0.4f), Color.Transparent), radius = glowRadius)
                        drawCircle(brush = glowBrush, radius = glowRadius)
                    }
                )
                Image(
                    painter = painterResource(id = currentTheme.toucheStarRes),
                    contentDescription = stringResource(R.string.cd_touche_point),
                    modifier = Modifier.fillMaxSize(0.85f).graphicsLayer { scaleX = goldScale; scaleY = goldScale; rotationZ = goldRotationZ }
                )
            }
        }

        gameViewModel.pieces.filter { it.state != PieceState.CAPTURED }.forEach { piece ->
            key(piece.id) {
                val isDying = (piece.state == PieceState.BEING_CAPTURED)
                val captureRotation by animateFloatAsState(targetValue = if (isDying) 720f else 0f, animationSpec = tween(1200, easing = LinearOutSlowInEasing), label = "")
                val targetX = piece.pos.x * cellWidth
                val targetY = piece.pos.y * cellHeight
                val animOffset by animateIntOffsetAsState(targetValue = IntOffset(targetX.roundToInt(), targetY.roundToInt()), animationSpec = tween(durationMillis = gameViewModel.currentMoveDuration, easing = EaseOutQuart), label = "PieceSlide")
                val captureScale by animateFloatAsState(targetValue = if (isDying) 0f else 1f, animationSpec = tween(800, easing = FastOutSlowInEasing), label = "")
                val captureAlpha by animateFloatAsState(targetValue = if (isDying) 0f else 1f, animationSpec = tween(300), label = "")
                val shouldPulse = (gameViewModel.gamePhase == GameWaitingFor.TAKE_PIECE) && (piece.owner != gameViewModel.currentPlayerId) && (piece.state == PieceState.IN_PLAY)

                Box(
                    modifier = Modifier
                        .size(cellWidthDp, cellHeightDp)
                        .offset { animOffset }
                        .graphicsLayer { rotationZ = captureRotation; scaleX = captureScale; scaleY = captureScale; alpha = captureAlpha },
                    contentAlignment = Alignment.Center
                ) {
                    PieceDesign(
                        owner = piece.owner, pieceId = piece.id, isPulsing = shouldPulse, isAnimationEnabled = isAnimationEnabled,
                        pieceX = animOffset.x.toFloat(), pieceY = animOffset.y.toFloat(), starX = starX, starY = starY, boardWidth = boardWidth, boardHeight = boardHeight, starPulseValue = goldScale
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            for (y in 0 until 7) {
                Row(modifier = Modifier.weight(1f)) {
                    for (x in 0 until 5) {
                        val idx = y * 5 + x
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(Unit) {
                                    val velocityTracker = VelocityTracker()
                                    var dragAccumulator = Offset.Zero

                                    detectDragGestures(
                                        onDragStart = {
                                            if (gameViewModel.gamePhase == GameWaitingFor.MOVE_PIECE) {
                                                velocityTracker.resetTracking()
                                                dragAccumulator = Offset.Zero
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            if (gameViewModel.gamePhase != GameWaitingFor.MOVE_PIECE) return@detectDragGestures

                                            change.consume()
                                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                                            dragAccumulator += dragAmount
                                        },
                                        onDragEnd = {
                                            if (gameViewModel.gamePhase != GameWaitingFor.MOVE_PIECE) return@detectDragGestures

                                            val velocity = velocityTracker.calculateVelocity()
                                            val speed = sqrt(velocity.x.pow(2) + velocity.y.pow(2)) / 4f
                                            val distance = sqrt(dragAccumulator.x.pow(2) + dragAccumulator.y.pow(2))

                                            if ((distance >= 60f || speed >= 400f) && gameViewModel.board[idx] == gameViewModel.currentPlayerId) {
                                                val clampedSpeed = speed.coerceIn(400f, 2000f)
                                                val speedFraction = (clampedSpeed - 400) / 1600
                                                val mappedDuration = 100f + (2000f * (1f - speedFraction).pow(2))
                                                val simulatedDrag = dragAccumulator * 10f

                                                gameViewModel.handleSwipe(idx, simulatedDrag, mappedDuration.toInt())
                                            }
                                        }
                                    )
                                }
                                .clickable {
                                    if (gameViewModel.gamePhase == GameWaitingFor.MOVE_PIECE || gameViewModel.gamePhase == GameWaitingFor.TAKE_PIECE) {
                                        gameViewModel.onCellClick(idx)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}