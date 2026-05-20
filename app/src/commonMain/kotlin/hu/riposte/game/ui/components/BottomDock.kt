package hu.riposte.game.ui.components

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.engine.logic.AppSettings
import hu.riposte.game.ui.theme.LocalGameTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import org.jetbrains.compose.resources.*
import riposte.app.generated.resources.*

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
    onHintClick: () -> Unit,
    onInfoClick: () -> Unit,
    isTournamentMode: Boolean,
) {
    val haptic = LocalHapticFeedback.current
    val densityObj = LocalDensity.current
    val density = densityObj.density

    // Simplified layout for KMP for now
    val boardHeight = 60.dp

    data class BarItem(val iconRes: DrawableResource?, val label: String, val isActive: Boolean, val isSpacer: Boolean = false, val action: () -> Unit = {})

    val items = buildList {
        add(BarItem(if (appSettings.musicEnabled) Res.drawable.ic_dock_musicon else Res.drawable.ic_dock_musicoff, stringResource(Res.string.options_music), appSettings.musicEnabled, false, onMusicToggle))
        add(BarItem(if (appSettings.sfxEnabled) Res.drawable.ic_dock_sfxon else Res.drawable.ic_dock_sfxoff, stringResource(Res.string.options_sfx), appSettings.sfxEnabled, false, onSfxToggle))
        add(BarItem(if (appSettings.nightModeEnabled) Res.drawable.ic_dock_daylight else Res.drawable.ic_dock_night, if (appSettings.nightModeEnabled) "Day Mode" else "Night Mode", appSettings.nightModeEnabled, false, onNightModeToggle))
        add(BarItem(Res.drawable.ic_dock_grid, stringResource(Res.string.options_grid), isGridVisible, false) { onGridToggle(!isGridVisible) })
        add(BarItem(Res.drawable.ic_dock_themes, stringResource(Res.string.cd_themes), true, false, onThemeClick))

        if (!isTournamentMode) {
            add(BarItem(Res.drawable.ic_dock_undo, "Undo", true, false, onUndoClick))
            add(BarItem(Res.drawable.ic_dock_hint, "Hint", true, false, onHintClick))
        } else {
            add(BarItem(Res.drawable.ic_dock_hint, "Opponent", true, false, onHintClick))
        }
        add(BarItem(Res.drawable.ic_dock_info, "Info", true, false, onInfoClick))

        add(BarItem(null, "Spacer", false, isSpacer = true))

        if (isTutorialMode) add(BarItem(Res.drawable.ic_dock_menu, "Skip", true, false, onSkipTutorial))
        else add(BarItem(Res.drawable.ic_dock_menu, "Menu", true, false, onMenuClick))
    }

    val currentItems by rememberUpdatedState(items)
    var touchX by remember { mutableStateOf<Float?>(null) }
    var isTouching by remember { mutableStateOf(false) }
    var barWidth by remember { mutableFloatStateOf(1f) }

    val totalRowWidthPx = barWidth * 0.9f
    val itemWidthPx = totalRowWidthPx / items.size
    val hoveredIndex = touchX?.let { x -> (x / itemWidthPx).toInt().coerceIn(0, items.size - 1) } ?: -1

    val dynamicIconSize = with(densityObj) { (barWidth * 0.1f).toDp() }.coerceIn(36.dp, 64.dp)

    val infiniteTransition = rememberInfiniteTransition(label = "DockAnimations")

    val shimmerTranslateAnim by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "ShimmerTranslate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.25f), Color.Transparent),
        start = Offset(shimmerTranslateAnim, 0f),
        end = Offset(shimmerTranslateAnim + 300f, 300f)
    )

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "WavePhase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .onGloballyPositioned { barWidth = it.size.width.toFloat() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // --- DOCK BAR GLASS BACKGROUND ---
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(boardHeight)
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    rotationX = 55f
                    cameraDistance = 12f * density
                }
                .shadow(16.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .background(shimmerBrush)
                .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        )

        AnimatedVisibility(
            visible = isTouching && hoveredIndex != -1 && !currentItems[hoveredIndex].isSpacer,
            enter = fadeIn() + slideInVertically { 20 },
            exit = fadeOut() + slideOutVertically { 20 },
            modifier = Modifier.align(Alignment.TopCenter).padding(bottom = 110.dp)
        ) {
            if (hoveredIndex != -1) {
                Text(
                    text = currentItems[hoveredIndex].label,
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

        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .pointerInput(itemWidthPx) {
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

                val dist = if (hoveredIndex != -1) abs(hoveredIndex - index) else 10
                val targetScale = if (isTouching && dist == 0) 2.4f else if (isTouching && dist == 1) 1.56f else 1f
                val scale by animateFloatAsState(targetValue = targetScale, label = "DockScale")

                val targetOffsetY = if (isTouching && dist == 0) -75f else if (isTouching && dist == 1) -30f else -10f
                val baseOffsetY by animateFloatAsState(targetValue = targetOffsetY, label = "DockOffset")

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
                            painter = painterResource(item.iconRes),
                            contentDescription = item.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(dynamicIconSize)
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
