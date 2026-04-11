package hu.riposte.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun RiposteBottomDock(
    appSettings: AppSettings,
    isTutorialMode: Boolean,
    isGridVisible: Boolean,
    onGridToggle: (Boolean) -> Unit,
    onMusicToggle: () -> Unit,
    onSfxToggle: () -> Unit,
    onNightModeToggle: () -> Unit,
    onThemeClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSkipTutorial: () -> Unit,
    onUndoClick: () -> Unit,
    onHintClick: () -> Unit
) {
    val currentTheme = LocalGameTheme.current
    val haptic = LocalHapticFeedback.current
    val densityObj = LocalDensity.current
    val density = densityObj.density
    val configuration = LocalConfiguration.current

    // --- RESZPONZÍV MÉRETEK ---
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val boardHeight = screenHeight * 0.06f
    val iconSize = screenWidth * 0.09f

    data class BarItem(val iconRes: Int?, val label: String, val isActive: Boolean, val isSpacer: Boolean = false, val action: () -> Unit = {})

    val items = listOf(
        BarItem(if (appSettings.musicEnabled) R.drawable.ic_dock_musicon else R.drawable.ic_dock_musicoff, "Music", appSettings.musicEnabled, false, onMusicToggle),
        BarItem(if (appSettings.sfxEnabled) R.drawable.ic_dock_sfxon else R.drawable.ic_dock_sfxoff, "Sound FX", appSettings.sfxEnabled, false, onSfxToggle),
        BarItem(if (appSettings.nightModeEnabled) R.drawable.ic_dock_daylight else R.drawable.ic_dock_night, if (appSettings.nightModeEnabled) "Day Mode" else "Night Mode", appSettings.nightModeEnabled, false, onNightModeToggle),
        BarItem(R.drawable.ic_dock_grid, "Grid", isGridVisible, false) { onGridToggle(!isGridVisible) },
        BarItem(R.drawable.ic_dock_themes, "Theme", true, false, onThemeClick),
        BarItem(R.drawable.ic_dock_undo, "Undo", true, false, onUndoClick),
        BarItem(R.drawable.ic_dock_hint, "Hint", true, false, onHintClick),
        BarItem(null, "Spacer", false, isSpacer = true),
        if (isTutorialMode) BarItem(R.drawable.ic_dock_menu, "Skip", true, false, onSkipTutorial)
        else BarItem(R.drawable.ic_dock_menu, "Menu", true, false, onMenuClick)
    )

    val currentItems by rememberUpdatedState(items)
    var touchX by remember { mutableStateOf<Float?>(null) }
    var isTouching by remember { mutableStateOf(false) }
    var barWidth by remember { mutableFloatStateOf(1f) }

    // --- JAVÍTOTT SZÉLESSÉG KALKULÁCIÓ ---
    // Konvertáljuk a Dp-t valós Pixellé, hogy egyezzen a barWidth-szel!
    // A képernyő 9%-a elemenként optimális teret ad a szétterüléshez.
    val maxItemWidthPx = with(densityObj) { (screenWidth * 0.09f).toPx() }
    val itemWidthPx = min(maxItemWidthPx, barWidth / items.size)
    val totalDockWidthPx = itemWidthPx * items.size
    val hoveredIndex = touchX?.let { x -> (x / itemWidthPx).toInt().coerceIn(0, items.size - 1) } ?: -1

    val infiniteTransition = rememberInfiniteTransition(label = "DockAnimations")

    // --- 1. SHIMMER ANIMÁCIÓ ---
    val shimmerTranslateAnim by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "ShimmerTranslate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent),
        start = Offset(shimmerTranslateAnim, 0f),
        end = Offset(shimmerTranslateAnim + 300f, 300f)
    )

    // --- 2. LEBEGÉS "PHASE" ---
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "WavePhase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Ez a tartó Box maradhat fix, ez csak az interakciós terület
            .onGloballyPositioned { barWidth = it.size.width.toFloat() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // --- 1. RÉTEG: A 3D ÜVEGPOLC ---
        Box(
            modifier = Modifier
                .width(with(densityObj) { (totalDockWidthPx + 80f).toDp() })
                .height(boardHeight) // <-- Reszponzív 4% magasság
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    rotationX = 55f
                    cameraDistance = 12f * density
                }
                .shadow(24.dp, RectangleShape)
                .clip(RectangleShape)
                .background(Brush.verticalGradient(listOf(currentTheme.containerColor.copy(alpha = 0.95f), currentTheme.containerColor.copy(alpha = 0.5f))))
                .background(shimmerBrush)
                .border(1.5.dp, Color.White.copy(alpha = 0.3f), RectangleShape)
        )

        // TOOLTIP
        AnimatedVisibility(
            visible = isTouching && hoveredIndex != -1 && !currentItems[hoveredIndex].isSpacer,
            enter = fadeIn() + slideInVertically { 20 },
            exit = fadeOut() + slideOutVertically { 20 },
            modifier = Modifier.align(Alignment.TopCenter).padding(bottom = 110.dp)
        ) {
            if (hoveredIndex != -1) {
                Text(
                    text = currentItems[hoveredIndex].label.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                )
            }
        }

        // --- 2. RÉTEG: A HOLOGRAFIKUS KORONGOK ---
        Row(
            modifier = Modifier
                .width(with(densityObj) { totalDockWidthPx.toDp() })
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        isTouching = true; touchX = down.position.x; down.consume()

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null) { touchX = change.position.x; change.consume() }
                        } while (event.changes.any { it.pressed })

                        val releaseX = touchX
                        if (releaseX != null) {
                            val finalIndex = (releaseX / itemWidthPx).toInt().coerceIn(0, currentItems.size - 1)
                            if (!currentItems[finalIndex].isSpacer) {
                                currentItems[finalIndex].action()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                        isTouching = false; touchX = null
                    }
                },
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEachIndexed { index, item ->
                if (item.isSpacer) { Spacer(modifier = Modifier.weight(1f)); return@forEachIndexed }

                // 1. Interakciós számítások (Hover) +20% méretnövekedéssel!
                val dist = if (hoveredIndex != -1) kotlin.math.abs(hoveredIndex - index) else 10
                val targetScale = if (isTouching && dist == 0) 2.4f else if (isTouching && dist == 1) 1.56f else 1f
                val scale by animateFloatAsState(targetValue = targetScale, label = "DockScale")

                // Az eltolást is megnöveltük, hogy a nagyobb ikonok feljebb húzódjanak
                val targetOffsetY = if (isTouching && dist == 0) -75f else if (isTouching && dist == 1) -30f else -10f
                val baseOffsetY by animateFloatAsState(targetValue = targetOffsetY, label = "DockOffset")

                // 2. Szerves 3D lebegés számítások
                val hoverIntensity = if (isTouching) 0f else 1f
                val floatY = sin(phase + index * 0.8f) * 6f * hoverIntensity
                val wobbleZ = sin(phase + index * 0.8f) * 4f * hoverIntensity
                val wobbleY = cos(phase + index * 0.8f) * 12f * hoverIntensity

                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationY = baseOffsetY + floatY
                        rotationZ = wobbleZ
                        rotationY = wobbleY
                        cameraDistance = 10f * density
                    },
                    contentAlignment = Alignment.Center
                ) {
                    if (item.iconRes != null) {
                        Image(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(iconSize) // <-- Reszponzív 5% méret
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                .graphicsLayer {
                                    alpha = if (item.isActive) 0.8f else 0.4f
                                }
                        )
                    }
                }
            }
        }
    }
}