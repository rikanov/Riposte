package hu.riposte.game

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.roundToInt

@Composable
fun RiposteGameBoard(gameViewModel: GameViewModel) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = (context as? Activity)

    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager(context) }

// Az új beállítások állapota!
    var appSettings by remember { mutableStateOf(AppSettings()) }
    var showOptions by remember { mutableStateOf(false) }

    // Hang és rezgés figyelő (ami MOST MÁR FIGYELI A BEÁLLÍTÁSOKAT IS!)
    LaunchedEffect(gameViewModel.soundEvent) {
        gameViewModel.soundEvent?.let { event ->
            when (event.type) {
                SoundType.MOVE -> {
                    // Csak akkor szól/rezeg, ha engedélyezték!
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

    // FŐ KONTÉNER
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- 1. PRÉMIUM FEJLÉC ÉS MENÜ ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "R I P O S T E",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
        }

        // --- 2. FLIP + SHIMMER STÁTUSZ BÁR (FÉMES PLAKETT) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            var displayPhase by remember { mutableStateOf(gameViewModel.gamePhase) }
            val flipRotation = remember { Animatable(0f) }
            val shimmerProgress = remember { Animatable(-1f) }

            // Pörgés és csillogás animáció vezérlése
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
                else -> Color(0xFFE3F2FD)
            }
            val contentColor = when (displayPhase) {
                GameWaitingFor.AI_MOVE -> Color(0xFFD32F2F)
                GameWaitingFor.TAKE_PIECE -> Color(0xFFF57F17)
                GameWaitingFor.GAME_OVER -> Color(0xFF2E7D32)
                else -> Color(0xFF1976D2)
            }
            val statusText = when (displayPhase) {
                GameWaitingFor.MOVE_PIECE -> "Player's Turn"
                GameWaitingFor.AI_MOVE -> "AI's thinking"
                GameWaitingFor.TAKE_PIECE -> "Touché! Select enemy to capture"
                GameWaitingFor.GAME_OVER -> "Game Over - ${gameViewModel.winner ?: ""}"
                else -> "Setting up..."
            }

            val metalShape = CutCornerShape(10.dp)
            val metallicBrush = androidx.compose.ui.graphics.Brush.linearGradient(
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
                        cameraDistance = 12f * density
                    }
                    .shadow(6.dp, metalShape)
                    .clip(metalShape)
                    .background(metallicBrush)
                    .drawBehind {
                        val lineColor = Color.Black.copy(alpha = 0.05f)
                        val strokeWidth = 1.dp.toPx()
                        var yPos = 0f
                        while (yPos < size.height) {
                            drawLine(
                                color = lineColor,
                                start = androidx.compose.ui.geometry.Offset(0f, yPos),
                                end = androidx.compose.ui.geometry.Offset(size.width, yPos),
                                strokeWidth = strokeWidth
                            )
                            yPos += 3.dp.toPx()
                        }
                    }
                    .border(
                        width = 1.5.dp,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.3f))
                        ),
                        shape = metalShape
                    )
                    .drawWithContent {
                        drawContent()
                        if (shimmerProgress.value > -0.5f && shimmerProgress.value < 1.5f) {
                            val w = size.width
                            val h = size.height
                            val xOffset = w * shimmerProgress.value
                            val shimmerBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.9f), Color.Transparent),
                                start = androidx.compose.ui.geometry.Offset(xOffset, 0f),
                                end = androidx.compose.ui.geometry.Offset(xOffset + 80.dp.toPx(), h)
                            )
                            drawRect(brush = shimmerBrush, blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop)
                        }
                    }
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = statusText.uppercase(),
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                    fontSize = 15.sp,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // --- 3. A JÁTÉKTÉR ---
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(5f / 7f)
                .padding(horizontal = 8.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E272E))
                .border(2.dp, Color(0xFF485460), RoundedCornerShape(16.dp))
        ) {
            val boardWidth = constraints.maxWidth.toFloat()
            val boardHeight = constraints.maxHeight.toFloat()
            val cellWidth = boardWidth / 5
            val cellHeight = boardHeight / 7
            val density = LocalDensity.current
            val cellWidthDp = with(density) { cellWidth.toDp() }
            val cellHeightDp = with(density) { cellHeight.toDp() }

            // --- A: PRÉMIUM RÁCS HÁTTÉR ---
            Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                for (y in 0 until 7) {
                    Row(modifier = Modifier.weight(1f)) {
                        for (x in 0 until 5) {
                            val isDark = (x + y) % 2 == 0
                            val tileGradient = if (isDark) {
                                androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF2F3640), Color(0xFF1E272E)))
                            } else {
                                androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF3D4554), Color(0xFF2F3640)))
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(3.dp)
                                    .shadow(2.dp, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tileGradient)
                                    .drawBehind {
                                        val lineColor = Color.Black.copy(alpha = 0.04f)
                                        val strokeWidth = 1.dp.toPx()
                                        val gap = 3.dp.toPx()
                                        var xPos = -size.height
                                        while (xPos < size.width) {
                                            drawLine(
                                                color = lineColor,
                                                start = androidx.compose.ui.geometry.Offset(xPos, 0f),
                                                end = androidx.compose.ui.geometry.Offset(xPos + size.height, size.height),
                                                strokeWidth = strokeWidth
                                            )
                                            xPos += gap
                                        }
                                    }
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            ) {
                                Box(modifier = Modifier.size(6.dp).align(Alignment.Center).background(Color.White.copy(alpha = 0.05f), CircleShape))
                            }
                        }
                    }
                }
            }

            // --- B: TOUCHE PONT (Arany csillag) ---
            val toucheIdx = gameViewModel.board.indexOf(4)
            if (toucheIdx != -1) {
                val tPos = Coord.fromIndex(toucheIdx)
                val goldX = tPos.x * cellWidth
                val goldY = tPos.y * cellHeight

                val infiniteTransition = rememberInfiniteTransition(label = "TouchePulseAnim")
                val goldScale by infiniteTransition.animateFloat(
                    initialValue = 0.8f, targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "GoldPulse"
                )
                val goldRotationZ by infiniteTransition.animateFloat(
                    initialValue = 360f, targetValue = 0f,
                    animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
                    label = "GoldRotate"
                )

                Box(
                    modifier = Modifier
                        .size(cellWidthDp, cellHeightDp)
                        .offset { IntOffset(goldX.roundToInt(), goldY.roundToInt()) }
                        .graphicsLayer {
                            scaleX = goldScale
                            scaleY = goldScale
                            rotationZ = goldRotationZ
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_touche_star),
                        contentDescription = "Touche Point",
                        modifier = Modifier.fillMaxSize(0.85f)
                    )
                }
            }

            // --- C: BÁBUK ---
            gameViewModel.pieces.filter { it.state != PieceState.CAPTURED }.forEach { piece ->
                key(piece.id) {
                    val isDying = (piece.state == PieceState.BEING_CAPTURED)
                    val captureRotation by animateFloatAsState(targetValue = if (isDying) 720f else 0f, animationSpec = tween(1200, easing = LinearOutSlowInEasing), label = "")
                    val targetX = piece.pos.x * cellWidth
                    val targetY = piece.pos.y * cellHeight
                    val animOffset by animateIntOffsetAsState(targetValue = IntOffset(targetX.roundToInt(), targetY.roundToInt()), animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing), label = "PieceSlide")
                    val captureScale by animateFloatAsState(targetValue = if (isDying) 0f else 1f, animationSpec = tween(800, easing = FastOutSlowInEasing), label = "")
                    val captureAlpha by animateFloatAsState(targetValue = if (isDying) 0f else 1f, animationSpec = tween(300), label = "")
                    val shouldPulse = (gameViewModel.gamePhase == GameWaitingFor.TAKE_PIECE) && (piece.owner != gameViewModel.currentPlayerId) && (piece.state == PieceState.IN_PLAY)

                    Box(
                        modifier = Modifier
                            .size(cellWidthDp, cellHeightDp)
                            .offset { animOffset }
                            .graphicsLayer {
                                rotationZ = captureRotation
                                scaleX = captureScale
                                scaleY = captureScale
                                alpha = captureAlpha
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        PieceDesign(owner = piece.owner, isPulsing = shouldPulse)
                    }
                }
            }

            // --- D: INPUT OVERLAY ---
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
                                        detectDragGestures(
                                            onDragStart = { },
                                            onDragEnd = { },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                gameViewModel.handleSwipe(idx, dragAmount)
                                            }
                                        )
                                    }
                                    .clickable {
                                        gameViewModel.onCellClick(idx)
                                    }
                            )
                        }
                    }
                }
            }
        } // ITT VÉGE A BoxWithConstraints-nek!

        // --- Dialógusok Hívása ---
        if (showMenu) {
            PauseMenuDialog(
                onResume = { showMenu = false },
                onNewGame = {
                    gameViewModel.gamePhase = GameWaitingFor.SETUP
                    showMenu = false
                },
                onOptions = {
                    showMenu = false // Menü elrejtése
                    showOptions = true // Beállítások megnyitása
                },
                onUndo = {
                    gameViewModel.undo()
                    showMenu = false
                },
                onExit = {
                    activity?.finish()
                    showMenu = false
                }
            )
        }

        // AZ ÚJ BEÁLLÍTÁSOK ABLAK MEGJELENÍTÉSE
        if (showOptions) {
            OptionsDialog(
                settings = appSettings,
                onSettingsChange = { appSettings = it }, // Itt mentjük el, ha valamit átkapcsolsz
                onClose = { showOptions = false }
            )
        }
        if (gameViewModel.gamePhase == GameWaitingFor.SETUP) {
            SetupDialog(
                onStart = { settings -> gameViewModel.startNewGame(settings) },
                onCancel = { gameViewModel.gamePhase = GameWaitingFor.MOVE_PIECE }
            )
        }
        if (gameViewModel.gamePhase == GameWaitingFor.GAME_OVER) {
            // ÚJ: Teljes képernyős tűzijáték a háttérben, ha nyertél!
            val isWin = gameViewModel.winner?.contains("You", ignoreCase = true) == true || gameViewModel.winner?.contains("Player", ignoreCase = true) == true
            if (isWin) {
                FireworksOverlay()
            }

            GameOverDialog(
                winnerName = gameViewModel.winner ?: "",
                onRestart = { gameViewModel.restartGame() }
            )
        }
    } // ITT VÉGE A FŐ Column-nak!
}
@Composable
fun PieceDesign(owner: Int, isPulsing: Boolean = false) {
    val imageRes = if (owner == 1) R.drawable.ic_piece_p1 else R.drawable.ic_piece_p2
    val infiniteTransition = rememberInfiniteTransition(label = "PieceAnim")

    // 1. LÜKTETÉS ANIMÁCIÓ (Ha épp üthető a bábu)
    val scale by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = 0.9f, targetValue = 1.1f,
            animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    // 2. CSILLOGÁS (Shimmer) ANIMÁCIÓ
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 3f,
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "shimmer"
    )

    // 3. ÚJ: LÉLEGZŐ AURA ANIMÁCIÓ
    // Az EaseInOutSine a legtermészetesebb, "tüdőszerű" lélegzési görbe
    val auraPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
        label = "aura"
    )

    // Játékosonként eltérő aura szín (P1 = Cián, P2 = Piros)
    val auraColor = if (owner == 1) Color(0xFF00E5FF) else Color(0xFFE53935)

    // FŐ KONTÉNER: A teljes dobozt pulzáltatjuk, ha üthető
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {

        // --- ALSÓ RÉTEG: A LÉLEGZŐ AURA ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val currentRadius = size.minDimension * (0.25f + (0.3f * auraPhase))

                    val currentAlpha = 0.7f - (0.5f * auraPhase)
                    val brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        0.0f to auraColor.copy(alpha = currentAlpha),
                        0.6f to auraColor.copy(alpha = currentAlpha * 0.7f),
                        1.0f to Color.Transparent,
                        radius = currentRadius
                    )

                    drawCircle(brush = brush, radius = currentRadius)
                }
        )

        // --- FELSŐ RÉTEG: A BÁBU ÉS A SHIMMER ---
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Game Piece",
            modifier = Modifier
                .fillMaxSize(0.85f)
                .graphicsLayer {
                    alpha = 0.99f // A Shimmer maszk miatt kell!
                }
                .drawWithContent {
                    drawContent()
                    if (shimmerProgress in -0.5f..1.5f) {
                        val w = size.width
                        val h = size.height
                        val xOffset = w * shimmerProgress
                        val yOffset = h * shimmerProgress
                        val shimmerBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.5f), Color.Transparent),
                            start = androidx.compose.ui.geometry.Offset(xOffset, yOffset),
                            end = androidx.compose.ui.geometry.Offset(xOffset + w * 0.4f, yOffset + h * 0.4f)
                        )
                        drawRect(brush = shimmerBrush, blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop)
                    }
                }
        )
    }
}