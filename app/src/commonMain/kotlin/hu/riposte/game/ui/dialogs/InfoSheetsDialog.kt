package hu.riposte.game.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.ui.utils.stringArrayResource
import riposte.app.generated.resources.*
import org.jetbrains.compose.resources.*
import hu.riposte.game.engine.logic.AppSettings
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.LocalGameTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoSheetsDialog(
    appSettings: AppSettings,
    onDismiss: () -> Unit,
    soundManager: SoundManager,
    onSettingsUpdate: (AppSettings) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTheme = LocalGameTheme.current
    val accentColor = currentTheme.uiAccentColor
    val fontFamily = currentTheme.fontFamily
    val categories = listOf(
        stringArrayResource(Res.array.daily_tips_useful),
        stringArrayResource(Res.array.daily_tips_strategy),
        stringArrayResource(Res.array.daily_tips_lore),
        stringArrayResource(Res.array.daily_tips_legendary),
        stringArrayResource(Res.array.daily_tips_history)
    )
    val tabNames = listOf("GAME INFO", "STRATEGY", "LORE", "LEGENDS", "HISTORY")

    var selectedTabIndex by remember { mutableIntStateOf(appSettings.lastInfoTab) }
    val pageIndexes = remember {
        mutableStateListOf(
            appSettings.infoUsefulIndex,
            appSettings.infoStrategyIndex,
            appSettings.infoLoreIndex,
            appSettings.infoLegendIndex,
            appSettings.infoHistoryIndex
        )
    }
    val saveState = {
        onSettingsUpdate(
            appSettings.copy(
                lastInfoTab = selectedTabIndex,
                infoUsefulIndex = pageIndexes[0],
                infoStrategyIndex = pageIndexes[1],
                infoLoreIndex = pageIndexes[2],
                infoLegendIndex = pageIndexes[3],
                infoHistoryIndex = pageIndexes[4]
            )
        )
    }

    GlassDialog(onDismissRequest = { saveState(); onDismiss() }) {
        Column(
            modifier = Modifier.width(340.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RIPOSTE ARCHIVE",
                color = accentColor,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                fontSize = 22.sp,
                fontFamily = fontFamily
            )

            Spacer(modifier = Modifier.height(24.dp))
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = accentColor,
                edgePadding = 16.dp,
                indicator = {},
                divider = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                tabNames.forEachIndexed { index, name ->
                    InfoCategoryTab(
                        text = name,
                        isSelected = selectedTabIndex == index,
                        accentColor = accentColor,
                        fontFamily = fontFamily,
                        onClick = {
                            if (selectedTabIndex != index) {
                                soundManager.playClick()
                                selectedTabIndex = index
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val currentTips = categories[selectedTabIndex]
            if (currentTips.isNotEmpty()) {
                key(selectedTabIndex) {
                    val pagerState = rememberPagerState(
                        initialPage = pageIndexes[selectedTabIndex].coerceIn(0, currentTips.size - 1),
                        pageCount = { currentTips.size }
                    )
                    LaunchedEffect(pagerState.currentPage) {
                        pageIndexes[selectedTabIndex] = pagerState.currentPage
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val canScrollBackward = pagerState.currentPage > 0
                        Box(
                            modifier = Modifier.size(40.dp).clickable(
                                enabled = canScrollBackward,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                soundManager.playClick()
                                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "◀",
                                color = if (canScrollBackward) accentColor else Color.Transparent,
                                fontSize = 20.sp,
                                fontFamily = fontFamily
                            )
                        }
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f).height(280.dp)
                        ) { page ->
                            val tipParts = currentTips[page].split("\n")
                            val title = tipParts.getOrNull(0) ?: "INFO"
                            val body = tipParts.getOrNull(1) ?: currentTips[page]

                            val scrollState = rememberScrollState()
                            val canScrollMore by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }

                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF151921).copy(alpha = 0.9f))
                                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = title.uppercase(),
                                        color = accentColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center,
                                        fontFamily = fontFamily
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Box(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
                                        Text(
                                            text = body,
                                            color = currentTheme.textColor.copy(alpha = 0.9f),
                                            textAlign = TextAlign.Center,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                            fontStyle = FontStyle.Italic,
                                            fontFamily = fontFamily
                                        )
                                    }
                                }
                                val visibilityAlpha by animateFloatAsState(
                                    targetValue = if (canScrollMore) 1f else 0f,
                                    animationSpec = tween(300),
                                    label = "arrow_visibility"
                                )

                                if (visibilityAlpha > 0f) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "arrow_anim")
                                    val arrowY by infiniteTransition.animateFloat(
                                        initialValue = -5f,
                                        targetValue = 5f,
                                        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
                                        label = "arrow_y"
                                    )
                                    val arrowPulseAlpha by infiniteTransition.animateFloat(
                                        initialValue = 0.3f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
                                        label = "arrow_alpha"
                                    )

                                    Text(
                                        text = "▼",
                                        color = accentColor.copy(alpha = arrowPulseAlpha * visibilityAlpha),
                                        fontSize = 14.sp,
                                        fontFamily = fontFamily,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                            .graphicsLayer { translationY = arrowY }
                                    )
                                }
                            }
                        }
                        val canScrollForward = pagerState.currentPage < currentTips.size - 1
                        Box(
                            modifier = Modifier.size(40.dp).clickable(
                                enabled = canScrollForward,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                soundManager.playClick()
                                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▶",
                                color = if (canScrollForward) accentColor else Color.Transparent,
                                fontSize = 20.sp,
                                fontFamily = fontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${pagerState.currentPage + 1} / ${currentTips.size}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                    Text("Loading...", color = accentColor, fontFamily = fontFamily)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bezárás
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f).height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { soundManager.playClick(); saveState(); onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BACK",
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 2.sp,
                    fontFamily = fontFamily
                )
            }
        }
    }
}

@Composable
fun InfoCategoryTab(
    text: String,
    isSelected: Boolean,
    accentColor: Color,
    fontFamily: FontFamily,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.3f, label = "tab_alpha")
    val scale by animateFloatAsState(if (isSelected) 1.15f else 0.9f, label = "tab_scale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            color = if (isSelected) accentColor else Color.White.copy(alpha = alpha),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = fontFamily
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(40.dp).height(2.dp)
                .background(if (isSelected) accentColor else Color.Transparent, RoundedCornerShape(1.dp))
        )
    }
}