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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.R
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

    // Kategóriák betöltése
    val categories = listOf(
        stringArrayResource(R.array.daily_tips_useful),
        stringArrayResource(R.array.daily_tips_strategy),
        stringArrayResource(R.array.daily_tips_lore),
        stringArrayResource(R.array.daily_tips_legendary),
        stringArrayResource(R.array.daily_tips_history)
    )
    val tabNames = listOf("GAME INFO", "STRATEGY", "LORE", "LEGENDS", "HISTORY")

    var selectedTabIndex by remember { mutableIntStateOf(appSettings.lastInfoTab) }

    // Golyóálló, lokális memóriatömb az aktuális oldal-indexekhez
    val pageIndexes = remember {
        mutableStateListOf(
            appSettings.infoUsefulIndex,
            appSettings.infoStrategyIndex,
            appSettings.infoLoreIndex,
            appSettings.infoLegendIndex,
            appSettings.infoHistoryIndex
        )
    }

    // Mentés funkció
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
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- TABOK KÖZÉPRE IGAZÍTÁSSAL (ScrollableTabRow) ---
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = accentColor,
                edgePadding = 16.dp, // Hogy a szélső tabok is bejöhessenek középre
                indicator = {}, // Saját indikátort használunk a kártyákban
                divider = {}, // Eltüntetjük az alapértelmezett alsó vonalat
                modifier = Modifier.fillMaxWidth()
            ) {
                tabNames.forEachIndexed { index, name ->
                    InfoCategoryTab(
                        text = name,
                        isSelected = selectedTabIndex == index,
                        accentColor = accentColor,
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

            // --- PAGER NYILAKKAL ---
            // A key() blokk garantálja, hogy tabváltáskor a Pager teljesen újratöltődik a helyes indexszel,
            // elkerülve a vad scrollozást és az állapotok véletlen felülírását.
            key(selectedTabIndex) {
                val pagerState = rememberPagerState(
                    initialPage = pageIndexes[selectedTabIndex].coerceIn(0, currentTips.size - 1),
                    pageCount = { currentTips.size }
                )

                // Frissítjük a memóriát, ha a játékos lapoz
                LaunchedEffect(pagerState.currentPage) {
                    pageIndexes[selectedTabIndex] = pagerState.currentPage
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bal nyíl
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
                        Text("◀", color = if (canScrollBackward) accentColor else Color.Transparent, fontSize = 20.sp)
                    }

                    // Tartalom kártya
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f).height(280.dp)
                    ) { page ->
                        val tipParts = currentTips[page].split("\n")
                        val title = tipParts.getOrNull(0) ?: "INFO"
                        val body = tipParts.getOrNull(1) ?: currentTips[page]

                        val scrollState = rememberScrollState()
                        // Optimalizált állapotfigyelés, hogy ne akadjon be a görgetés
                        val canScrollMore by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }

                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(currentTheme.containerColor.copy(alpha = 0.85f)) // Markánsabb sötétített háttér
                                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)) // Téma-specifikus keret
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title.uppercase(),
                                    color = accentColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(12.dp))
                                Box(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
                                    Text(
                                        text = body,
                                        color = currentTheme.textColor.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }

                            // --- PULZÁLÓ ÉS LEBEGŐ NYÍL ---
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
                                    // Összeszorozzuk a láthatóságot a pulzálással
                                    color = accentColor.copy(alpha = arrowPulseAlpha * visibilityAlpha),
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp)
                                        .graphicsLayer { translationY = arrowY }
                                )
                            }
                        }
                    }

                    // Jobb nyíl
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
                        Text("▶", color = if (canScrollForward) accentColor else Color.Transparent, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${pagerState.currentPage + 1} / ${currentTips.size}",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bezárás
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f).height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.15f)) // Ez is a témához igazodik
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { soundManager.playClick(); saveState(); onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text("BACK", fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
fun InfoCategoryTab(
    text: String,
    isSelected: Boolean,
    accentColor: Color,
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
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(40.dp).height(2.dp)
                .background(if (isSelected) accentColor else Color.Transparent, RoundedCornerShape(1.dp))
        )
    }
}