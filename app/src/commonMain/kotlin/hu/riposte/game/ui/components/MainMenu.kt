package hu.riposte.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import hu.riposte.game.engine.logic.SoundManager
import kotlin.math.PI
import kotlin.math.abs

data class MainMenuItem(
    val label: String,
    val isEnabled: Boolean = true,
    val hasSwipeAction: Boolean = false,
    val isPremiumOnly: Boolean = false,
    val needsAttention: Boolean = false,
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

class ParallelSubMenuShape(private val slantPx: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - slantPx, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun InteractiveMainMenu(
    modifier: Modifier = Modifier,
    menuItems: List<MainMenuItem>,
    aiDifficultyItems: List<MainMenuItem>,
    isPremiumVersion: Boolean,
    soundManager: SoundManager,
    haptic: HapticFeedback,
    screenHeight: Dp,
    screenWidth: Dp
) {
    val density = LocalDensity.current

    val slopeRatio = 0.7349f

    val targetMenuHeight = screenHeight / 3f
    val count = menuItems.size
    val spacingRatio = 0.2f
    val heightMultiplier = count + (count - 1) * spacingRatio

    val itemHeight = targetMenuHeight / heightMultiplier
    val itemSpacing = itemHeight * spacingRatio
    val menuFontSize = with(density) { (itemHeight * 0.35f).toSp() }

    val widthStep = (itemHeight + itemSpacing) * slopeRatio
    val slantAmountDp = itemHeight * slopeRatio

    val bottomWidth = screenWidth * 0.667f
    val topWidth = bottomWidth - (widthStep * (count - 1))

    val maxCombinedMenuWidth = screenWidth * 0.9f
    val menuGap = 2.dp

    val infiniteTransition = rememberInfiniteTransition(label = "MenuWobble")
    val phaseState = infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2*PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart), label = "Phase"
    )

    var touchX by remember { mutableStateOf<Float?>(null) }
    var touchY by remember { mutableStateOf<Float?>(null) }
    var isTouching by remember { mutableStateOf(false) } // Ez fogja reprezentálni a "Hover" állapotot is
    var isDragging by remember { mutableStateOf(false) }
    var dragStartX by remember { mutableStateOf<Float?>(null) }
    var showAiSubMenu by remember { mutableStateOf(false) }

    val itemTotalHeightPx = with(density) { (itemHeight + itemSpacing).toPx() }
    val subMenuYOffsetPx = 0f

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(targetMenuHeight)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pos = event.changes.firstOrNull()?.position
                        val isTouch = event.changes.firstOrNull()?.type == PointerType.Touch

                        when (event.type) {
                            PointerEventType.Move, PointerEventType.Enter -> {
                                if (pos != null) {
                                    touchX = pos.x
                                    touchY = pos.y
                                    isTouching = true

                                    if (isDragging && dragStartX != null) {
                                        val currentMainIdx = (pos.y / itemTotalHeightPx).toInt().coerceIn(0, menuItems.size - 1)
                                        val isTrainingItem = menuItems[currentMainIdx].hasSwipeAction

                                        if (!showAiSubMenu && isTrainingItem && dragStartX!! - pos.x > 50f) {
                                            showAiSubMenu = true
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        if (showAiSubMenu && pos.x - dragStartX!! > -20f) {
                                            showAiSubMenu = false
                                        }
                                    }
                                }
                            }
                            PointerEventType.Press -> {
                                if (pos != null) {
                                    touchX = pos.x
                                    touchY = pos.y
                                    isTouching = true
                                    isDragging = true
                                    dragStartX = pos.x
                                    showAiSubMenu = false
                                }
                            }
                            PointerEventType.Release -> {
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
                                        if (selectedItem.isEnabled) {
                                            if (selectedItem.hasSwipeAction) {
                                                showAiSubMenu = true
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            } else {
                                                soundManager.playClick()
                                                selectedItem.action()
                                            }
                                        }
                                    }
                                }
                                isDragging = false
                                dragStartX = null
                                showAiSubMenu = false

                                if (isTouch) {
                                    isTouching = false
                                    touchX = null
                                    touchY = null
                                }
                            }
                            PointerEventType.Exit -> {
                                isTouching = false
                                isDragging = false
                                dragStartX = null
                                showAiSubMenu = false
                                touchX = null
                                touchY = null
                            }
                        }
                    }
                }
            }
    ) {
        val hoveredMainIndex = touchY?.let { y -> (y / itemTotalHeightPx).toInt().coerceIn(0, menuItems.size - 1) } ?: -1
        val hoveredSubIndex = if (showAiSubMenu && touchY != null) { ((touchY!! - subMenuYOffsetPx) / itemTotalHeightPx).toInt().coerceIn(0, aiDifficultyItems.size - 1) } else -1

        // --- SUBMENU (PARALEL AND MIRRORED) ---
        val subMenuAlpha by animateFloatAsState(targetValue = if (showAiSubMenu) 1f else 0f, animationSpec = tween(200), label = "")
        val subMenuOffsetX by animateFloatAsState(targetValue = if (showAiSubMenu) 0f else 50f, animationSpec = spring(), label = "")

        if (subMenuAlpha > 0f) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = (itemHeight + itemSpacing) * (menuItems.size - aiDifficultyItems.size).toFloat())
                    .width(maxCombinedMenuWidth)
                    .graphicsLayer { alpha = subMenuAlpha; translationX = subMenuOffsetX },
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                aiDifficultyItems.forEachIndexed { index, item ->
                    val dist = if (hoveredSubIndex != -1) abs(hoveredSubIndex - index) else 10
                    val targetScale = if (showAiSubMenu && isTouching && dist == 0) 1.25f else 1f
                    val scale by animateFloatAsState(targetValue = targetScale, label = "SubScaleAnim")

                    val isStygian = index == 5
                    val correspondingMainWidth = topWidth + (widthStep * index)
                    val subButtonWidth = maxCombinedMenuWidth - correspondingMainWidth - menuGap

                    ParallelMenuButton(
                        item = item,
                        isPremiumVersion = isPremiumVersion,
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

        // --- FŐMENÜ ---
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).graphicsLayer { alpha = if (showAiSubMenu) 0.6f else 1f },
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            menuItems.forEachIndexed { index, item ->
                val buttonWidth = topWidth + (widthStep * index)
                val isThisMainHovered = (!showAiSubMenu && isTouching && hoveredMainIndex == index)
                val forceHover = (showAiSubMenu && hoveredMainIndex == index && item.hasSwipeAction)
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
