package hu.riposte.game

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class MainMenuItem(
    val label: String,
    val isEnabled: Boolean = true,
    val hasSwipeAction: Boolean = false,
    val action: () -> Unit = {}
)

// Eredeti főmenü alakzat (balra ferde)
class SlantedShape(private val slantPx: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(slantPx, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

// ÚJ: Párhuzamos alakzat az almenünek (a jobb alsó sarka van levágva)
class ParallelSubMenuShape(private val slantPx: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f) // Jobb felső sarok végigér
            lineTo(size.width - slantPx, size.height) // Jobb alsó sarok van behúzva!
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun MainScreen(
    isInterruptedGame: Boolean,
    gameViewModel: GameViewModel,
    onResumeGame: () -> Unit,
    onNavigateToTournament: () -> Unit,
    onNavigateToTutorial: () -> Unit,
    onNavigateToAiTraining: () -> Unit,
    onNavigateToLocal: () -> Unit,
    onNavigateToOnline: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onExitGame: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val soundManager = remember { SoundManager(context) }
    val settingsManager = remember { SettingsManager(context) }
    val appSettingsState = settingsManager.settingsFlow.collectAsState(initial = null)
    val appSettings = appSettingsState.value

    LaunchedEffect(appSettings?.musicEnabled) {
        appSettings?.let { settings ->
            soundManager.isMusicEnabled = settings.musicEnabled
            if (settings.musicEnabled) soundManager.playThemeMusic(R.raw.main_menu_music)
            else soundManager.pauseMusic()
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

    val menuItems = listOf(
        MainMenuItem(stringResource(R.string.menu_resume), isEnabled = isInterruptedGame, action = onResumeGame),
        MainMenuItem(stringResource(R.string.menu_quick_tutorial), action = onNavigateToTutorial),
        MainMenuItem(stringResource(R.string.menu_training_ai), hasSwipeAction = true, action = {}),
        MainMenuItem(stringResource(R.string.menu_local_players), action = onNavigateToLocal),
        MainMenuItem(stringResource(R.string.menu_tournament), isEnabled = false),
        MainMenuItem(stringResource(R.string.menu_online), isEnabled = false),
        MainMenuItem(stringResource(R.string.menu_settings), action = onNavigateToSettings),
        MainMenuItem(stringResource(R.string.menu_exit), action = onExitGame)
    )

    val aiDifficultyItems = listOf(
        MainMenuItem("APPRENTICE") { gameViewModel.startNewGame(GameSettings(difficulty = 3)); onNavigateToAiTraining() },
        MainMenuItem("SWORDSMAN") { gameViewModel.startNewGame(GameSettings(difficulty = 5)); onNavigateToAiTraining() },
        MainMenuItem("DUELIST") { gameViewModel.startNewGame(GameSettings(difficulty = 7)); onNavigateToAiTraining() },
        MainMenuItem("MASTER") { gameViewModel.startNewGame(GameSettings(difficulty = 9)); onNavigateToAiTraining() },
        MainMenuItem("GRANDMASTER") { gameViewModel.startNewGame(GameSettings(difficulty = 10)); onNavigateToAiTraining() },
        MainMenuItem("STYGIAN") { gameViewModel.startNewGame(GameSettings(difficulty = 11)); onNavigateToAiTraining() }
    )

    // --- HARMONIKUS ARÁNYOK MATEMATIKÁJA ---
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val screenWidthFloat = configuration.screenWidthDp.toFloat()
    val slopeRatio = 0.7349f

    val targetMenuHeight = screenHeight / 3f
    val count = menuItems.size
    val spacingRatio = 0.2f
    val heightMultiplier = count + (count - 1) * spacingRatio

    val itemHeight = targetMenuHeight / heightMultiplier
    val itemSpacing = itemHeight * spacingRatio
    val menuFontSize = (itemHeight.value * 0.35f).sp

    val widthStep = (itemHeight + itemSpacing) * slopeRatio
    val slantAmountDp = itemHeight * slopeRatio

    val bottomWidth = screenWidth * 0.667f
    val topWidth = bottomWidth - (widthStep * (count - 1))

    // --- PÁRHUZAMOSÍTÁS MATEMATIKÁJA ---
    val maxCombinedMenuWidth = screenWidth * 0.9f
    val menuGap = 16.dp

    // --- ANIMÁCIÓK ÉS ÁLLAPOTOK ---
    val infiniteTransition = rememberInfiniteTransition(label = "MenuWobble")
    val phaseState = infiniteTransition.animateFloat(initialValue = 0f, targetValue = (2 * PI).toFloat(), animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart), label = "Phase")

    var touchX by remember { mutableStateOf<Float?>(null) }
    var touchY by remember { mutableStateOf<Float?>(null) }
    var isTouching by remember { mutableStateOf(false) }
    var showAiSubMenu by remember { mutableStateOf(false) }

    val itemTotalHeightPx = with(density) { (itemHeight + itemSpacing).toPx() }
    val subMenuYOffsetPx = 0f

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.main_menu_diorama_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Image(
            painter = painterResource(id = R.drawable.main_typography),
            contentDescription = "La Riposte Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 48.dp, start = 32.dp).width(screenWidth * 0.5f).graphicsLayer { alpha = 0.99f }.drawWithContent { drawContent(); drawRect(brush = Brush.verticalGradient(0.0f to Color.Black, 0.66f to Color.Black, 1.0f to Color.Black.copy(alpha = 0.33f), startY = 0f, endY = size.height), blendMode = BlendMode.DstIn) }
        )

        // GESZTUS ÉRZÉKELŐ RÉTEG
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.9f)
                .height(targetMenuHeight)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        isTouching = true
                        touchX = down.position.x
                        touchY = down.position.y
                        showAiSubMenu = false
                        down.consume()

                        val startX = touchX!!

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                touchX = change.position.x
                                touchY = change.position.y

                                val currentMainIdx = (touchY!! / itemTotalHeightPx).toInt().coerceIn(0, menuItems.size - 1)

                                if (!showAiSubMenu && currentMainIdx == 2 && startX - touchX!! > 50f) {
                                    showAiSubMenu = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                if (showAiSubMenu && touchX!! - startX > -20f) {
                                    showAiSubMenu = false
                                }

                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })

                        // KIÉRTÉKELÉS
                        val releaseY = touchY
                        if (releaseY != null) {
                            val finalMainIndex = (releaseY / itemTotalHeightPx).toInt().coerceIn(0, menuItems.size - 1)
                            val finalSubIndex = ((releaseY - subMenuYOffsetPx) / itemTotalHeightPx).toInt().coerceIn(0, aiDifficultyItems.size - 1)

                            if (showAiSubMenu) {
                                val selectedItem = aiDifficultyItems[finalSubIndex]
                                soundManager.playClick()
                                selectedItem.action()
                            } else {
                                val selectedItem = menuItems[finalMainIndex]
                                if (selectedItem.isEnabled && finalMainIndex != 2) {
                                    soundManager.playClick()
                                    selectedItem.action()
                                }
                            }
                        }

                        isTouching = false
                        showAiSubMenu = false
                        touchX = null
                        touchY = null
                    }
                }
        ) {

            val hoveredMainIndex = touchY?.let { y -> (y / itemTotalHeightPx).toInt().coerceIn(0, menuItems.size - 1) } ?: -1
            val hoveredSubIndex = if (showAiSubMenu && touchY != null) { ((touchY!! - subMenuYOffsetPx) / itemTotalHeightPx).toInt().coerceIn(0, aiDifficultyItems.size - 1) } else -1

            // --- ALMENÜ (Tükrözött, párhuzamosra vágva) ---
            val subMenuAlpha by animateFloatAsState(targetValue = if (showAiSubMenu) 1f else 0f, animationSpec = tween(200), label = "")
            val subMenuOffsetX by animateFloatAsState(targetValue = if (showAiSubMenu) 0f else 50f, animationSpec = spring(), label = "")

            if (subMenuAlpha > 0f) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = (itemHeight + itemSpacing) * 2f)
                        .width(maxCombinedMenuWidth)
                        .graphicsLayer { alpha = subMenuAlpha; translationX = subMenuOffsetX },
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(itemSpacing)
                ) {
                    aiDifficultyItems.forEachIndexed { index, item ->
                        val dist = if (hoveredSubIndex != -1) kotlin.math.abs(hoveredSubIndex - index) else 10
                        val targetScale = if (showAiSubMenu && isTouching && dist == 0) 1.25f else 1f
                        val scale by animateFloatAsState(targetValue = targetScale, label = "SubScaleAnim")

                        val isStygian = index == 5

                        // PÁRHUZAMOSÍTÁS
                        val correspondingMainIndex = index
                        val correspondingMainWidth = topWidth + (widthStep * correspondingMainIndex)
                        val subButtonWidth = maxCombinedMenuWidth - correspondingMainWidth - menuGap

                        ParallelMenuButton(
                            item = item,
                            buttonWidth = subButtonWidth,
                            buttonHeight = itemHeight,
                            slantAmountDp = slantAmountDp,
                            fontSize = menuFontSize,
                            index = index,
                            phaseState = phaseState,
                            scale = scale,
                            isHovered = (showAiSubMenu && isTouching && dist == 0),
                            isStygian = isStygian
                        )
                    }
                }
            }

            // --- FŐMENÜ (Eredeti) ---
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).graphicsLayer {
                    alpha = if (showAiSubMenu) 0.6f else 1f
                },
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                menuItems.forEachIndexed { index, item ->
                    val buttonWidth = topWidth + (widthStep * index)

                    val isThisMainHovered = (!showAiSubMenu && isTouching && hoveredMainIndex == index)
                    val forceHover = (showAiSubMenu && index == 2)

                    val targetScale = if (isThisMainHovered || forceHover) 1.4f else 1f
                    val scale by animateFloatAsState(targetValue = targetScale, label = "MainScaleAnim")

                    SlantedMenuButton(
                        item = item,
                        buttonWidth = buttonWidth,
                        buttonHeight = itemHeight,
                        slantAmountDp = slantAmountDp,
                        fontSize = menuFontSize,
                        index = index,
                        phaseState = phaseState,
                        scale = scale,
                        isHovered = isThisMainHovered || forceHover
                    )
                }
            }
        }
    }
}

// A régi (jobb oldali) gomb, KIEGÉSZÍTVE a pulzáló animált kacsacsőrrel
@Composable
fun SlantedMenuButton(item: MainMenuItem, buttonWidth: androidx.compose.ui.unit.Dp, buttonHeight: androidx.compose.ui.unit.Dp, slantAmountDp: androidx.compose.ui.unit.Dp, fontSize: TextUnit, index: Int, phaseState: State<Float>, scale: Float, isHovered: Boolean) {
    val density = LocalDensity.current
    val gradientColors = if (item.isEnabled) { if (isHovered) listOf(Color(0xFF3B4459), Color(0xFF262C3A)) else listOf(Color(0xFF2A3040), Color(0xFF1A1E28)) } else { listOf(Color(0xFF1E1E24), Color(0xFF121216)) }
    val alphaColor = if (isHovered) 0.95f else 0.75f
    val textColor = if (item.isEnabled) Color.White else Color.White.copy(alpha = 0.3f)
    val slantPx = with(density) { slantAmountDp.toPx() }

    Box(
        modifier = Modifier.width(buttonWidth).height(buttonHeight).graphicsLayer { transformOrigin = TransformOrigin(1f, 0.5f); scaleX = scale; scaleY = scale; val hoverIntensity = if (isHovered) 0f else 1f; translationX = sin(phaseState.value + index * 0.5f) * 3f * hoverIntensity; translationY = cos(phaseState.value + index * 0.5f) * 1.5f * hoverIntensity }
            .clip(SlantedShape(slantPx = slantPx)).background(Brush.horizontalGradient(gradientColors), alpha = alphaColor)
            .border(width = if (isHovered) 1.5.dp else 0.5.dp, color = Color.White.copy(alpha = if (isHovered) 0.6f else 0.1f), shape = SlantedShape(slantPx = slantPx)),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(modifier = Modifier.matchParentSize().background(Brush.horizontalGradient(0.0f to Color.White.copy(alpha = 0.15f), 0.2f to Color.Transparent)))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            if (item.hasSwipeAction) {
                val infinitePulse = rememberInfiniteTransition(label = "SwipeHint")
                val hintOffset by infinitePulse.animateFloat(initialValue = 0f, targetValue = -12f, animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "arrowOffset")
                val hintAlpha by infinitePulse.animateFloat(initialValue = 0.2f, targetValue = 0.8f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "arrowAlpha")

                Text(
                    text = "« ", // Animált dupla kacsacsőr
                    color = Color.White.copy(alpha = hintAlpha),
                    fontSize = fontSize,
                    modifier = Modifier.offset(x = hintOffset.dp)
                )
            }
            Text(
                text = item.label,
                color = textColor,
                fontWeight = if (isHovered) FontWeight.Black else FontWeight.Bold,
                fontSize = fontSize,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
}

// Párhuzamos (bal oldali) gomb az almenünek
@Composable
fun ParallelMenuButton(item: MainMenuItem, buttonWidth: androidx.compose.ui.unit.Dp, buttonHeight: androidx.compose.ui.unit.Dp, slantAmountDp: androidx.compose.ui.unit.Dp, fontSize: TextUnit, index: Int, phaseState: State<Float>, scale: Float, isHovered: Boolean, isStygian: Boolean) {
    val density = LocalDensity.current

    val gradientColors = if (isStygian) {
        if (isHovered) listOf(Color(0xFF591E1E), Color(0xFF3A1212)) else listOf(Color(0xFF401A1A), Color(0xFF281111))
    } else {
        if (isHovered) listOf(Color(0xFF3B4459), Color(0xFF262C3A)) else listOf(Color(0xFF2A3040), Color(0xFF1A1E28))
    }

    val alphaColor = if (isHovered) 0.95f else 0.75f
    val textColor = if (isStygian) Color(0xFFFF5555) else Color.White
    val slantPx = with(density) { slantAmountDp.toPx() }

    Box(
        modifier = Modifier.width(buttonWidth).height(buttonHeight).graphicsLayer {
            transformOrigin = TransformOrigin(0f, 0.5f)
            scaleX = scale; scaleY = scale
            val hoverIntensity = if (isHovered) 0f else 1f
            translationX = sin(phaseState.value + index * 0.5f + 2f) * 3f * hoverIntensity
            translationY = cos(phaseState.value + index * 0.5f + 2f) * 1.5f * hoverIntensity
        }
            .clip(ParallelSubMenuShape(slantPx = slantPx)).background(Brush.horizontalGradient(gradientColors.reversed()), alpha = alphaColor)
            .border(width = if (isHovered) 1.5.dp else 0.5.dp, color = if (isStygian) Color.Red.copy(alpha = if (isHovered) 0.6f else 0.2f) else Color.White.copy(alpha = if (isHovered) 0.6f else 0.1f), shape = ParallelSubMenuShape(slantPx = slantPx)),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(modifier = Modifier.matchParentSize().background(Brush.horizontalGradient(0.0f to Color.Transparent, 0.8f to Color.White.copy(alpha = 0.15f))))
        Text(text = item.label, color = textColor, fontWeight = if (isHovered) FontWeight.Black else FontWeight.Bold, fontSize = fontSize, letterSpacing = 1.2.sp, textAlign = TextAlign.Start, maxLines = 1, modifier = Modifier.padding(start = 16.dp))
    }
}