package hu.riposte.game.ui.dialogs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.R
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.GameTheme
import hu.riposte.game.ui.theme.LocalGameTheme
import hu.riposte.game.ui.theme.ThemeRegistry
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemeSelectorDialog(
    currentThemeId: String,
    onThemeSelected: (String) -> Unit,
    onThemePreview: (String) -> Unit,
    onDismiss: () -> Unit,
    soundManager: SoundManager
) {
    val coroutineScope = rememberCoroutineScope()
    val targetAccentColor = LocalGameTheme.current.uiAccentColor
    val accentColor by animateColorAsState(
        targetValue = targetAccentColor,
        animationSpec = tween(durationMillis = 800),
        label = "DialogAccentColor"
    )

    var selectedTabIndex by remember {
        mutableIntStateOf(
            when {
                ThemeRegistry.classicThemes.any { it.id == currentThemeId } -> 0
                ThemeRegistry.modernThemes.any { it.id == currentThemeId } -> 1
                else -> 2
            }
        )
    }

    val currentThemeList = when (selectedTabIndex) {
        0 -> ThemeRegistry.classicThemes
        1 -> ThemeRegistry.modernThemes
        else -> ThemeRegistry.zenThemes
    }

    val initialPage = currentThemeList.indexOfFirst { it.id == currentThemeId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { currentThemeList.size })

    LaunchedEffect(pagerState.settledPage, currentThemeList) {
        if (pagerState.settledPage in currentThemeList.indices) {
            val selectedTheme = currentThemeList[pagerState.settledPage]
            onThemePreview(selectedTheme.id)
        }
    }

    GlassDialog(onDismissRequest = {
        onThemePreview(currentThemeId)
        onDismiss()
    }) {
        Column(
            modifier = Modifier.width(320.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.theme_title),
                color = accentColor,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- ÚJ: COLLECTION CÍM ---
            Text(
                text = "SELECT COLLECTION",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // --- CATEGORY TABS ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ThemeCategoryTab(
                    text = "CLASSIC",
                    isSelected = selectedTabIndex == 0,
                    accentColor = accentColor,
                    onClick = {
                        if (selectedTabIndex != 0) {
                            soundManager.playClick()
                            selectedTabIndex = 0
                            coroutineScope.launch { pagerState.scrollToPage(0) }
                        }
                    }
                )
                ThemeCategoryTab(
                    text = "MODERN",
                    isSelected = selectedTabIndex == 1,
                    accentColor = accentColor,
                    onClick = {
                        if (selectedTabIndex != 1) {
                            soundManager.playClick()
                            selectedTabIndex = 1
                            coroutineScope.launch { pagerState.scrollToPage(0) }
                        }
                    }
                )
                ThemeCategoryTab(
                    text = "ZEN",
                    isSelected = selectedTabIndex == 2,
                    accentColor = accentColor,
                    onClick = {
                        if (selectedTabIndex != 2) {
                            soundManager.playClick()
                            selectedTabIndex = 2
                            coroutineScope.launch { pagerState.scrollToPage(0) }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ÚJ: NYILAK ÉS PAGER ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bal nyíl
                val canScrollBackward = pagerState.currentPage > 0
                Text(
                    text = "◀",
                    color = if (canScrollBackward) accentColor else Color.Transparent,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable(enabled = canScrollBackward) {
                            soundManager.playClick()
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }
                )

                // Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .height(280.dp),
                    pageSpacing = 32.dp
                ) { page ->
                    val theme = currentThemeList[page]
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ThemeCard(theme = theme, isSelected = pagerState.currentPage == page)
                    }
                }

                // Jobb nyíl
                val canScrollForward = pagerState.currentPage < currentThemeList.size - 1
                Text(
                    text = "▶",
                    color = if (canScrollForward) accentColor else Color.Transparent,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable(enabled = canScrollForward) {
                            soundManager.playClick()
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Name
            if (pagerState.settledPage in currentThemeList.indices) {
                Text(
                    text = currentThemeList[pagerState.settledPage].displayName.uppercase(),
                    color = DialogContentColor,
                    letterSpacing = 2.sp,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- ACTIONS ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accentColor)
                        .clickable {
                            soundManager.playClick()
                            if (pagerState.settledPage in currentThemeList.indices) {
                                onThemeSelected(currentThemeList[pagerState.settledPage].id)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.theme_apply), fontWeight = FontWeight.Bold, color = DialogDarkTextColor)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable {
                            soundManager.playClick()
                            onThemePreview(currentThemeId)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.theme_cancel), fontWeight = FontWeight.Bold, color = DialogContentColor)
                }
            }
        }
    }
}
@Composable
fun ThemeCategoryTab(
    text: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.4f, label = "tab_alpha")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Text(
            text = text,
            color = accentColor.copy(alpha = alpha),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Indicator
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(if (isSelected) accentColor else Color.Transparent, RoundedCornerShape(1.dp))
        )
    }
}

@Composable
fun ThemeCard(theme: GameTheme, isSelected: Boolean) {
    val scale by animateFloatAsState(if (isSelected) 1f else 0.85f, label = "card_scale")
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.5f, label = "card_alpha")

    Card(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(5f / 7f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(3.dp, if (theme.id == "random") Color.Yellow else theme.auraP1Color) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (theme.id == "random") Color(0xFF1E272E) else Color.Transparent)
        ) {
            Image(
                painter = painterResource(id = theme.previewImageRes),
                contentDescription = theme.displayName,
                contentScale = if (theme.id == "random") ContentScale.Fit else ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (theme.id == "random") 32.dp else 0.dp)
            )
        }
    }
}
