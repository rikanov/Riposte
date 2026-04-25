package hu.riposte.game.ui.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemeSelectorDialog(
    currentThemeId: String,
    onThemeSelected: (String) -> Unit,
    onThemePreview: (String) -> Unit,
    onDismiss: () -> Unit,
    soundManager: SoundManager
) {
    val themes = ThemeRegistry.allThemes
    val initialPage = themes.indexOfFirst { it.id == currentThemeId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { themes.size }
    val accentColor = LocalGameTheme.current.uiAccentColor

    LaunchedEffect(pagerState.currentPage) {
        val selectedTheme = themes[pagerState.currentPage]
        onThemePreview(selectedTheme.id)
    }

    GlassDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.width(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.theme_title),
                color = accentColor,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .height(280.dp)
                    .fillMaxWidth(),
                pageSpacing = 32.dp
            ) { page ->
                val theme = themes[page]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ThemeCard(theme = theme, isSelected = pagerState.currentPage == page)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = themes[pagerState.currentPage].displayName.uppercase(),
                color = DialogContentColor,
                letterSpacing = 2.sp,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                            onThemeSelected(themes[pagerState.currentPage].id)
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