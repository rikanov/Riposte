package hu.riposte.game.engine.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.engine.data.Coord
import hu.riposte.game.engine.data.GameWaitingFor
import hu.riposte.game.engine.data.PieceState
import hu.riposte.game.engine.data.TutorialPhase
import hu.riposte.game.engine.logic.MoveLogic
import hu.riposte.game.engine.logic.GameViewModel
import hu.riposte.game.ui.components.PieceDesign
import hu.riposte.game.ui.theme.LocalGameTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sin

@Composable
fun RiposteBoardArea(
    gameViewModel: GameViewModel,
    isGridVisible: Boolean,
    isAnimationEnabled: Boolean,
    isVisualAssistEnabled: Boolean,
    isNightModeEnabled: Boolean,
    isHapticEnabled: Boolean, // A mágneses célzáshoz
    deviceTilt: Offset        // A giroszkópos árnyékhoz
) {
    val currentTheme = LocalGameTheme.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current // A rezgésekhez

    var dragSourceIdx by remember { mutableIntStateOf(-1) }
    var ghostTargetIdx by remember { mutableIntStateOf(-1) }
    var isFadingOut by remember { mutableStateOf(false) }

    val ghostAlpha by animateFloatAsState(
        targetValue = if (isFadingOut) 0f else if (dragSourceIdx != -1) 0.5f else 0f,
        animationSpec = tween(durationMillis = if (isFadingOut) 500 else 0),
        label = "ghostAlpha"
    )

    val isAiThinking = gameViewModel.gamePhase == GameWaitingFor.AI_MOVE
    val thinkingTransition = rememberInfiniteTransition(label = "AiThinking")

    val aiPulseAlpha by thinkingTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "AiPulse"
    )

    val currentBorderColor = if (isAiThinking) {
        currentTheme.auraP1Color.copy(alpha = aiPulseAlpha)
    } else {
        Color.White.copy(alpha = 0.2f)
    }
    // -----------------------------------

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(5f / 7f)
            .shadow(24.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .border(if (isAiThinking) 2.dp else 1.5.dp, currentBorderColor, RoundedCornerShape(16.dp))
    ){
        // 1. BACKGROUND (A tábla saját háttere, pl. a kiterített pergamen)
        Image(
            painter = painterResource(id = currentTheme.boardBackgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // ÉJSZAKAI SÖTÉTÍTÉS
        val dimAlpha by animateFloatAsState(targetValue = if (isNightModeEnabled) 0.5f else 0.0f, animationSpec = tween(800), label = "DimAlpha")
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = dimAlpha)))

        // ÜVEGES RÉTEG
        Box(
            modifier = Modifier
                .fillMaxSize()
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

        // 2. GRID
        val gridAlpha by animateFloatAsState(targetValue = if (isGridVisible) 1f else 0f, animationSpec = tween(500), label = "grid_alpha")
        Column(modifier = Modifier.fillMaxSize().padding(4.dp).graphicsLayer { alpha = gridAlpha }) {
            for (y in 0 until 7) {
                Row(modifier = Modifier.weight(1f)) {
                    for (x in 0 until 5) {
                        val isDark = (x + y) % 2 == 0
                        val tileGradient = if (isDark) Brush.linearGradient(listOf(currentTheme.boardCellDark, Color.Transparent))
                        else Brush.linearGradient(listOf(currentTheme.boardCellLight, currentTheme.boardCellLight.copy(alpha = 0.3f)))

                        Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(3.dp).background(tileGradient, RoundedCornerShape(8.dp)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp)))
                    }
                }
            }
        }

        // 3. VIZUÁLIS SEGÉDLET ÉS HINT (A vonal és a szellembábu)
        val activeHint = gameViewModel.activeHint
        val isDragging = (dragSourceIdx != -1 || isFadingOut) && ghostTargetIdx != -1 && ghostTargetIdx != dragSourceIdx
        val showHint = activeHint != null && !isDragging

        if ((isVisualAssistEnabled && isDragging) || showHint) {
            val sIdx = if (isDragging) dragSourceIdx else activeHint!!.from
            val tIdx = if (isDragging) ghostTargetIdx else activeHint!!.to
            val sourcePos = Coord.fromIndex(sIdx)
            val targetPos = Coord.fromIndex(tIdx)

            val startOff = Offset(sourcePos.x * cellWidth + cellWidth/2, sourcePos.y * cellHeight + cellHeight/2)
            val endOff = Offset(targetPos.x * cellWidth + cellWidth/2, targetPos.y * cellHeight + cellHeight/2)

            val pathColor = if (isDragging) { if (gameViewModel.currentPlayerId == 1) currentTheme.auraP1Color else currentTheme.auraP2Color } else Color(0xFF00E5FF)
            val currentAlpha = if (isDragging) ghostAlpha else 0.6f

            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = currentAlpha }) {
                drawLine(color = pathColor, start = startOff, end = endOff, strokeWidth = 6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f))
            }

            Box(
                modifier = Modifier
                    .size(cellWidthDp, cellHeightDp)
                    .offset { IntOffset(targetPos.x * cellWidth.roundToInt(), targetPos.y * cellHeight.roundToInt()) }
                    .graphicsLayer { alpha = currentAlpha }
            ) {
                // SZELLEMBÁBU
                PieceDesign(
                    owner = gameViewModel.currentPlayerId,
                    pieceId = -1,
                    isAnimationEnabled = false,
                    deviceTilt = deviceTilt
                )
            }
        }

        // 4. TOUCHE POINT
        val toucheIdx = gameViewModel.board.indexOf(4)
        val infiniteTransitionTouche = rememberInfiniteTransition(label = "TouchePulseAnim")

        val animGoldScale by infiniteTransitionTouche.animateFloat(initialValue = currentTheme.toucheScaleMin, targetValue = currentTheme.toucheScaleMax, animationSpec = infiniteRepeatable(tween(currentTheme.touchePulseDuration, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "GoldPulse")
        val animGoldRotation by infiniteTransitionTouche.animateFloat(initialValue = 360f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(currentTheme.toucheRotationDuration, easing = LinearEasing), RepeatMode.Restart), label = "GoldRotate")

        val finalGoldScale = if (isNightModeEnabled) 1f else animGoldScale
        val finalGoldRotation = if (isNightModeEnabled) 0f else animGoldRotation

        if (toucheIdx != -1) {
            val tPos = Coord.fromIndex(toucheIdx)
            val starX = tPos.x * cellWidth
            val starY = tPos.y * cellHeight

            val shimmerIntensity = if (isNightModeEnabled) 0f else ((finalGoldScale - 0.8f) * 5f).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .size(cellWidthDp, cellHeightDp)
                    .offset { IntOffset(starX.roundToInt(), starY.roundToInt()) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(1.8f).drawBehind {
                        if (!isNightModeEnabled) {
                            val glowRadius = (size.minDimension / 2f) * (0.8f + 0.4f * shimmerIntensity)
                            val glowAlpha = 0.2f + 0.5f * shimmerIntensity
                            val glowBrush = Brush.radialGradient(colors = listOf(currentTheme.auraP1Color.copy(alpha = glowAlpha), currentTheme.auraP1Color.copy(alpha = glowAlpha * 0.4f), Color.Transparent), radius = glowRadius)
                            drawCircle(brush = glowBrush, radius = glowRadius)
                        }
                    }
                )
                Image(painter = painterResource(id = currentTheme.toucheStarRes), contentDescription = null, modifier = Modifier.fillMaxSize(0.85f).graphicsLayer { scaleX = finalGoldScale; scaleY = finalGoldScale; rotationZ = finalGoldRotation })
            }
        }

        // 5. PIECE RENDERING (Élő bábuk)
        gameViewModel.pieces.filter { it.state != PieceState.CAPTURED }.forEach { piece ->
            key(piece.id) {
                val isDying = (piece.state == PieceState.BEING_CAPTURED)
                val targetX = piece.pos.x * cellWidth
                val targetY = piece.pos.y * cellHeight

                // RUGÓS (SPRING) ANIMÁCIÓ
                val animOffset by animateIntOffsetAsState(
                    targetValue = IntOffset(targetX.roundToInt(), targetY.roundToInt()),
                    animationSpec = spring(
                        dampingRatio = 0.65f, // Fa/kő kopogás érzet
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "PieceSlide"
                )

                val captureRotation by animateFloatAsState(targetValue = if (isDying) 720f else 0f, animationSpec = tween(1200, easing = LinearOutSlowInEasing), label = "")
                val captureScale by animateFloatAsState(targetValue = if (isDying) 0f else 1f, animationSpec = tween(800, easing = FastOutSlowInEasing), label = "")
                val captureAlpha by animateFloatAsState(targetValue = if (isDying) 0f else 1f, animationSpec = tween(300), label = "")

                val shouldPulseForCapture = (gameViewModel.gamePhase == GameWaitingFor.TAKE_PIECE) && (piece.owner != gameViewModel.currentPlayerId) && (piece.state == PieceState.IN_PLAY)
                val isBeingDragged = (dragSourceIdx == piece.pos.toIndex())

                Box(
                    modifier = Modifier
                        .size(cellWidthDp, cellHeightDp)
                        .offset { animOffset }
                        .graphicsLayer { rotationZ = captureRotation; scaleX = captureScale; scaleY = captureScale; alpha = captureAlpha },
                    contentAlignment = Alignment.Center
                ) {

                    // HALÁLHULLÁM (RIPPLE)
                    if (isDying) {
                        val rippleScale by animateFloatAsState(
                            targetValue = 2.5f,
                            animationSpec = tween(600, easing = LinearOutSlowInEasing),
                            label = "RippleScale"
                        )
                        val rippleAlpha by animateFloatAsState(
                            targetValue = 0f,
                            animationSpec = tween(600, easing = LinearEasing),
                            label = "RippleAlpha"
                        )

                        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = rippleScale
                            scaleY = rippleScale
                            alpha = rippleAlpha
                        }) {
                            drawCircle(
                                color = if (piece.owner == 1) currentTheme.auraP1Color else currentTheme.auraP2Color,
                                radius = size.minDimension / 3f,
                                style = Stroke(width = 8.dp.toPx())
                            )
                        }
                    }

                    // ÉLŐ BÁBU
                    PieceDesign(
                        owner = piece.owner,
                        pieceId = piece.id,
                        isPulsing = shouldPulseForCapture,
                        isDragged = isBeingDragged,
                        isAnimationEnabled = isAnimationEnabled,
                        deviceTilt = deviceTilt
                    )
                }
            }
        }

        // 6. INPUT OVERLAY (A tökéletes mágneses érzékelés)
        Column(modifier = Modifier.fillMaxSize()) {
            for (y in 0 until 7) {
                Row(modifier = Modifier.weight(1f)) {
                    for (x in 0 until 5) {
                        val idx = y * 5 + x

                        // JAVÍTÁS 1: A változót kitesszük ide, és az 'idx'-hez kötjük a memóriát!
                        val interactionSource = remember(idx) { MutableInteractionSource() }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(Unit) {
                                    var dragAccumulator = Offset.Zero

                                    detectDragGestures(
                                        onDragStart = {
                                            if (gameViewModel.gamePhase == GameWaitingFor.MOVE_PIECE && gameViewModel.board[idx] == gameViewModel.currentPlayerId) {
                                                dragSourceIdx = idx
                                                isFadingOut = false
                                                dragAccumulator = Offset.Zero
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            if (dragSourceIdx == -1) return@detectDragGestures
                                            change.consume()
                                            dragAccumulator += dragAmount

                                            val angle = atan2(dragAccumulator.y, dragAccumulator.x) * 180 / PI
                                            val offset = gameViewModel.getOffsetFromAngle(angle)
                                            if (offset != null) {
                                                val newTarget = MoveLogic.calculateTargetIndex(gameViewModel.board, dragSourceIdx, offset)
                                                // MÁGNESES CÉLZÁS
                                                if (newTarget != ghostTargetIdx) {
                                                    ghostTargetIdx = newTarget
                                                    if (ghostTargetIdx != -1 && ghostTargetIdx != dragSourceIdx && isHapticEnabled) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                }
                                            } else {
                                                ghostTargetIdx = -1
                                            }
                                        },
                                        onDragEnd = {
                                            if (dragSourceIdx == -1) return@detectDragGestures
                                            val distance = sqrt(dragAccumulator.x.pow(2) + dragAccumulator.y.pow(2))

                                            if (distance >= 60f) {
                                                val simulatedDrag = dragAccumulator * 10f
                                                gameViewModel.handleSwipe(dragSourceIdx, simulatedDrag)
                                            }
                                            isFadingOut = true
                                            coroutineScope.launch { delay(500); dragSourceIdx = -1; ghostTargetIdx = -1; isFadingOut = false }
                                        },
                                        onDragCancel = {
                                            isFadingOut = true
                                            coroutineScope.launch { delay(300); dragSourceIdx = -1; ghostTargetIdx = -1; isFadingOut = false }
                                        }
                                    )
                                }
                                // JAVÍTÁS 2: Beépített kattintás-effekt (ripple) kinyírása
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (gameViewModel.gamePhase == GameWaitingFor.MOVE_PIECE || gameViewModel.gamePhase == GameWaitingFor.TAKE_PIECE) {
                                        gameViewModel.onCellClick(idx)
                                    }
                                }
                        )
                    }
                }
            }
        }
        // 7. TUTORIAL OVERLAY
        val showTutorialOverlay = gameViewModel.isTutorialMode && (gameViewModel.tutorialPhase == TutorialPhase.SHOW_TOUCHE || gameViewModel.tutorialPhase == TutorialPhase.SHOW_CAPTURE || gameViewModel.tutorialPhase == TutorialPhase.SHOW_WIN_COND)

        AnimatedVisibility(visible = showTutorialOverlay, enter = fadeIn(animationSpec = tween(500)), exit = fadeOut(animationSpec = tween(300))) {
            val highlightIdx = if (gameViewModel.tutorialPhase == TutorialPhase.SHOW_TOUCHE) gameViewModel.board.indexOf(4) else -1
            val tutText = when(gameViewModel.tutorialPhase) {
                TutorialPhase.SHOW_TOUCHE -> "This is the Touché point.\nTry to stop here!"
                TutorialPhase.SHOW_CAPTURE -> "Excellent!\nChoose an opponent's piece to capture."
                TutorialPhase.SHOW_WIN_COND -> "Take one more piece to win!\nBut beware: if the opponent takes two of yours, you lose!"
                else -> ""
            }

            Box(
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures {
                        when (gameViewModel.tutorialPhase) {
                            TutorialPhase.SHOW_TOUCHE, TutorialPhase.SHOW_CAPTURE -> gameViewModel.tutorialPhase = TutorialPhase.WAIT_FOR_TOUCH
                            TutorialPhase.SHOW_WIN_COND -> { gameViewModel.tutorialPhase = TutorialPhase.FINISHED; gameViewModel.resumeTutorialTurn() }
                            else -> {}
                        }
                    }
                }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val overlayPath = Path().apply {
                        val rectPath = Path().apply { addRect(Rect(0f, 0f, size.width, size.height)) }
                        if (highlightIdx != -1) {
                            val tPos = Coord.fromIndex(highlightIdx)
                            val cx = tPos.x * cellWidth + cellWidth / 2
                            val cy = tPos.y * cellHeight + cellHeight / 2
                            val radius = cellWidth * 0.7f
                            val circlePath = Path().apply { addOval(Rect(cx - radius, cy - radius, cx + radius, cy + radius)) }
                            op(rectPath, circlePath, PathOperation.Difference)
                        } else { addPath(rectPath) }
                    }
                    drawPath(overlayPath, color = Color.Black.copy(alpha = 0.75f))
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                ) {
                    Text(text = tutText, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.background(currentTheme.containerColor.copy(alpha=0.9f), RoundedCornerShape(12.dp)).border(1.dp, currentTheme.uiAccentColor, RoundedCornerShape(12.dp)).padding(16.dp))
                    Spacer(modifier = Modifier.height(32.dp))
                    val infinitePulse = rememberInfiniteTransition(label="TutPulse")
                    val alphaPulse by infinitePulse.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label="")
                    Text(text = "- TAP ANYWHERE TO CONTINUE -", color = currentTheme.uiAccentColor.copy(alpha = alphaPulse), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                }
            }
        }

    }
}

@Composable
fun AmbientVfxOverlay(themeId: String) {
    // Csak a megfelelő témáknál jelenjen meg
    val particleColor = when (themeId) {
        "destreza" -> Color(0xFFFFD54F) // Arany/narancs gyertya-szikrák
        "winter_pagoda" -> Color.White // Hóesés
        "mayan_fresco" -> Color(0xFFAED581) // Dzsungel spórák / zöldes por
        "jungle_morning" -> Color(0xFFFFF59D) // Napsütötte por
        else -> return // Ha nem adunk meg színt, ne rajzoljon semmit!
    }

    // Egyetlen végtelenített idő-változó, ami hajtja az egész rendszert
    val infiniteTransition = rememberInfiniteTransition(label = "VfxTimer")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "Time"
    )

    // Generálunk 40 fix "alap" részecskét, amiknek a mozgását a szinusz hullámok adják
    val particles = remember {
        List(40) {
            object {
                val startX = Math.random().toFloat()
                val startY = Math.random().toFloat()
                val speedX = (Math.random() - 0.5f).toFloat() * 1.5f
                val speedY = if (themeId == "winter_pagoda") (Math.random().toFloat() * 2f + 1f) // A hó csak lefelé esik
                else (Math.random() - 0.5f).toFloat() * 1f // A por lebeg
                val size = (Math.random() * 4f + 2f).toFloat()
                val phaseOffset = (Math.random() * PI * 2).toFloat()
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.6f }) {
        particles.forEach { p ->
            // A porszemek lassan úsznak, és az X tengelyen finoman imbolyognak
            val x = (p.startX * size.width + p.speedX * time * 10f + sin(time + p.phaseOffset) * 20f) % size.width
            val y = (p.startY * size.height + p.speedY * time * 10f) % size.height

            // Ha kimegy a képernyőről, térjen vissza a másik oldalon (végtelenítve)
            val finalX = if (x < 0) x + size.width else x
            val finalY = if (y < 0) y + size.height else y

            // Lágyan pulzáló átlátszóság
            val alpha = (0.2f + 0.3f * sin(time * 2f + p.phaseOffset)).coerceIn(0f, 1f)

            drawCircle(
                color = particleColor.copy(alpha = alpha),
                radius = p.size,
                center = Offset(finalX, finalY)
            )
        }
    }
}