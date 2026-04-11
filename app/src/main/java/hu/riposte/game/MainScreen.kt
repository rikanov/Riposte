package hu.riposte.game

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class MainMenuItem(
    val label: String,
    val isEnabled: Boolean = true,
    val action: () -> Unit = {}
)

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

@Composable
fun MainScreen(
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
            if (settings.musicEnabled) {
                soundManager.playThemeMusic(R.raw.main_menu_music)
            } else {
                soundManager.pauseMusic()
            }
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
        MainMenuItem(stringResource(R.string.menu_resume), isEnabled = false),
        MainMenuItem(stringResource(R.string.menu_quick_tutorial), action = onNavigateToTutorial),
        MainMenuItem(stringResource(R.string.menu_training_ai), action = onNavigateToAiTraining),
        MainMenuItem(stringResource(R.string.menu_local_players), action = onNavigateToLocal),
        MainMenuItem(stringResource(R.string.menu_tournament), isEnabled = false),
        MainMenuItem(stringResource(R.string.menu_online), isEnabled = false),
        MainMenuItem(stringResource(R.string.menu_settings), action = onNavigateToSettings),
        MainMenuItem(stringResource(R.string.menu_exit), action = onExitGame)
    )
    // --- HARMONIKUS ARÁNYOK MATEMATIKÁJA ---

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val screenWidthFloat = configuration.screenWidthDp.toFloat()
    val slopeRatio = 0.7349f

    // 1. Tipográfia (Bal Felső) - A Képernyő szélességének pontosan az 1/32 a fő betűméret!
    val typoWidth = screenWidth / 2f
    val titleFontSize = (screenWidthFloat / 32f).sp

    // 2. Menü Magasság (Jobb Alsó) - Pontosan 1/3
    val targetMenuHeight = screenHeight / 3f
    val count = menuItems.size
    val spacingRatio = 0.2f
    val heightMultiplier = count + (count - 1) * spacingRatio

    val itemHeight = targetMenuHeight / heightMultiplier
    val itemSpacing = itemHeight * spacingRatio
    val menuFontSize = (itemHeight.value * 0.35f).sp

    // 3. Szélesség és Vágás
    val widthStep = (itemHeight + itemSpacing) * slopeRatio
    val slantAmountDp = itemHeight * slopeRatio

    val bottomWidth = screenWidth * 0.667f
    val topWidth = bottomWidth - (widthStep * (count - 1))

    // --- ANIMÁCIÓK ---
    val infiniteTransition = rememberInfiniteTransition(label = "MenuWobble")
    val phaseState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "Phase"
    )

    var touchY by remember { mutableStateOf<Float?>(null) }
    var isTouching by remember { mutableStateOf(false) }
    val itemTotalHeightPx = with(density) { (itemHeight + itemSpacing).toPx() }
    val hoveredIndex = touchY?.let { y -> (y / itemTotalHeightPx).toInt().coerceIn(0, menuItems.size - 1) } ?: -1

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.main_menu_diorama_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // LOGÓ - A PNG Tipográfia (Bal felső sarok)
        Image(
            painter = painterResource(id = R.drawable.main_typography),
            contentDescription = "La Riposte Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 32.dp)
                .width(screenWidth * 0.5f)
                // A graphicsLayer { alpha = 0.99f } izolálja a réteget, ez kell a BlendMode működéséhez!
                .graphicsLayer { alpha = 0.99f }
                .drawWithContent {
                    drawContent() // Kirajzolja a logót
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.0f to Color.Black,             // Teteje: 100% látható
                            0.66f to Color.Black,            // Kétharmadáig: 100% látható marad
                            1.0f to Color.Black.copy(alpha = 0.33f), // Alja: Lefade-el 33%-ra
                            startY = 0f,
                            endY = size.height
                        ),
                        blendMode = BlendMode.DstIn // Ez mondja meg, hogy a gradient áttetszősége "vágja" a képet
                    )
                }
        )
        // MENÜ (Pontosan 1/3 magas, 1/2 szélesre fut ki)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 0.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        isTouching = true
                        touchY = down.position.y
                        down.consume()

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                touchY = change.position.y
                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })

                        val releaseY = touchY
                        if (releaseY != null) {
                            val finalIndex = (releaseY / itemTotalHeightPx).toInt().coerceIn(0, menuItems.size - 1)
                            val selectedItem = menuItems[finalIndex]
                            if (selectedItem.isEnabled) {
                                soundManager.playClick()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedItem.action()
                            }
                        }
                        isTouching = false
                        touchY = null
                    }
                },
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            menuItems.forEachIndexed { index, item ->
                val buttonWidth = topWidth + (widthStep * index)

                val dist = if (hoveredIndex != -1) kotlin.math.abs(hoveredIndex - index) else 10
                val targetScale = if (isTouching && dist == 0) 1.4f
                else if (isTouching && dist == 1) 1.15f
                else 1f
                val scale by animateFloatAsState(targetValue = targetScale, label = "ScaleAnim")

                SlantedMenuButton(
                    item = item,
                    buttonWidth = buttonWidth,
                    buttonHeight = itemHeight,
                    slantAmountDp = slantAmountDp,
                    fontSize = menuFontSize,
                    index = index,
                    phaseState = phaseState,
                    scale = scale,
                    isHovered = (isTouching && dist == 0)
                )
            }
        }
    }
}

@Composable
fun SlantedMenuButton(
    item: MainMenuItem,
    buttonWidth: androidx.compose.ui.unit.Dp,
    buttonHeight: androidx.compose.ui.unit.Dp,
    slantAmountDp: androidx.compose.ui.unit.Dp,
    fontSize: TextUnit,
    index: Int,
    phaseState: State<Float>,
    scale: Float,
    isHovered: Boolean
) {
    val density = LocalDensity.current

    val gradientColors = if (item.isEnabled) {
        if (isHovered) listOf(Color(0xFF3B4459), Color(0xFF262C3A))
        else listOf(Color(0xFF2A3040), Color(0xFF1A1E28))
    } else {
        listOf(Color(0xFF1E1E24), Color(0xFF121216))
    }

    val alphaColor = if (isHovered) 0.95f else 0.75f
    val textColor = if (item.isEnabled) Color.White else Color.White.copy(alpha = 0.3f)

    val slantPx = with(density) { slantAmountDp.toPx() }

    Box(
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight)
            .graphicsLayer {
                transformOrigin = TransformOrigin(1f, 0.5f)
                scaleX = scale
                scaleY = scale

                val phase = phaseState.value
                val hoverIntensity = if (isHovered) 0f else 1f
                translationX = sin(phase + index * 0.5f) * 3f * hoverIntensity
                translationY = cos(phase + index * 0.5f) * 1.5f * hoverIntensity
            }
            .clip(SlantedShape(slantPx = slantPx))
            .background(Brush.horizontalGradient(gradientColors), alpha = alphaColor)
            .border(
                width = if (isHovered) 1.5.dp else 0.5.dp,
                color = Color.White.copy(alpha = if (isHovered) 0.6f else 0.1f),
                shape = SlantedShape(slantPx = slantPx)
            ),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.horizontalGradient(
                    0.0f to Color.White.copy(alpha = 0.15f),
                    0.2f to Color.Transparent
                ))
        )

        Text(
            text = item.label,
            color = textColor,
            fontWeight = if (isHovered) FontWeight.Black else FontWeight.Bold,
            fontSize = fontSize,
            letterSpacing = 1.2.sp,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}