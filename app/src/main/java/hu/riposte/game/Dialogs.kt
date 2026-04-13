package hu.riposte.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

// --- BASE SETTINGS ---
data class AppSettings(
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val visualAssistsEnabled: Boolean = true,
    val nightModeEnabled: Boolean = false,
    val riposteEnabled: Boolean = true,
    val themeId: String = "abstract_sunrise",
    val hasSeenTutorial: Boolean = false
)

private val DialogContentColor = Color.White
private val DialogDarkTextColor = Color(0xFF1E272E)

// --- UNIVERSAL "JUICY GLASS" CONTAINER ---
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    val accentColor = LocalGameTheme.current.uiAccentColor

    val shimmerProgress = rememberInfiniteTransition(label = "dialog_shimmer").animateFloat(
        initialValue = -0.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer_anim"
    )

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.55f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch { alpha.animateTo(1f, tween(200)) }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E272E).copy(alpha = 0.85f))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .drawWithContent {
                    drawContent()
                    val w = size.width
                    val h = size.height
                    val xOffset = w * shimmerProgress.value
                    val shimmerBrush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.8f), Color.Transparent),
                        start = Offset(xOffset, 0f),
                        end = Offset(xOffset + 200f, h)
                    )
                    drawRoundRect(
                        brush = shimmerBrush,
                        size = size,
                        cornerRadius = CornerRadius(24.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                .padding(24.dp)
        ) {
            content()
        }
    }
}
data class Particle(val vx: Float, val vy: Float, val color: Color, val size: Float)
@Composable
fun FireworksOverlay() {
    val particles = remember {
        List(150) {
            Particle(
                vx = (Math.random() * 60 - 30).toFloat(),
                vy = (Math.random() * -60 - 20).toFloat(),
                color = Color(
                    red = (100..255).random() / 255f,
                    green = (100..255).random() / 255f,
                    blue = (100..255).random() / 255f,
                    alpha = 1f
                ),
                size = (Math.random() * 12 + 6).toFloat()
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2500, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 1f - (progress.value * 0.5f) }) {
        val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.35f)

        particles.forEach { p ->
            val currentX = centerOffset.x + (p.vx * progress.value * 50)
            val currentY = centerOffset.y + (p.vy * progress.value * 50 + 60f * progress.value * progress.value * 50)

            drawCircle(
                color = p.color.copy(alpha = 1f - progress.value),
                radius = p.size * (1f - progress.value),
                center = androidx.compose.ui.geometry.Offset(currentX, currentY)
            )
        }
    }
}

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
@Composable
fun TutorialWelcomeDialog(
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    // --- ÚJ: Fázis kezelés ---
    var currentPhase by remember { mutableIntStateOf(1) } // 1: Swipe, 2: Hold & Select

    val infiniteTransition = rememberInfiniteTransition(label = "TutAnim")

    // Animációk az 1. Fázishoz (Gyors Swipe)
    val swipeX1 by infiniteTransition.animateFloat(
        initialValue = -60f, targetValue = 60f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing, delayMillis = 600), RepeatMode.Restart), label = "s1"
    )
    val touchScale1 by infiniteTransition.animateFloat(
        initialValue = 1.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            keyframes { durationMillis = 1400; 1.3f at 0; 0.9f at 300; 0.9f at 1100; 1.3f at 1400 }, RepeatMode.Restart
        ), label = "ts1"
    )

    // Animációk a 2. Fázishoz (Lassú Hold & Select)
    val swipeX2 by infiniteTransition.animateFloat(
        initialValue = -60f, targetValue = 60f,
        // Sokkal lassabb, "húzós" mozdulat
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing, delayMillis = 600), RepeatMode.Restart), label = "s2"
    )
    val touchScale2 by infiniteTransition.animateFloat(
        initialValue = 1.3f, targetValue = 0.9f,
        // Sokáig lent marad az ujj
        animationSpec = infiniteRepeatable(
            keyframes { durationMillis = 3100; 1.3f at 0; 0.9f at 300; 0.9f at 2800; 1.3f at 3100 }, RepeatMode.Restart
        ), label = "ts2"
    )

    GlassDialog(onDismissRequest = { }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    soundManager.playClick()
                    if (currentPhase == 1) {
                        currentPhase = 2 // Első kattintásra átvált a "lövész" módra
                    } else {
                        onDismiss() // Második kattintásra bezár és indul a játék
                    }
                }
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (currentPhase == 1) "HOW TO MOVE" else "PRO TIP: AIMING",
                color = accentColor, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            // --- AZ ANIMÁLT VIZUÁLIS BEMUTATÓ ---
            Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {

                // Aktuális fázis animációinak kiválasztása
                val currentSwipeX = if (currentPhase == 1) swipeX1 else swipeX2
                val currentTouchScale = if (currentPhase == 1) touchScale1 else touchScale2
                val currentTouchAlpha = if (currentTouchScale <= 1.0f) 1f else 0f

                if (currentPhase == 1) {
                    // 1. Fázis: Elmosódó csík (Trail)
                    Box(modifier = Modifier.width(100.dp).height(40.dp).background(
                        Brush.horizontalGradient(colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.3f), accentColor.copy(alpha = 0.8f))),
                        shape = RoundedCornerShape(20.dp)
                    ).graphicsLayer { alpha = currentTouchAlpha * 0.8f }
                    )
                } else {
                    // 2. Fázis: "Ghost Piece" és Vonal imitálása (Ahogy kérted!)
                    if (currentTouchAlpha > 0f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(color = accentColor, start = Offset(0f, size.height/2), end = Offset(size.width, size.height/2), strokeWidth = 4.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f))
                        }
                        // Szellembábu a falnál
                        Image(painter = painterResource(id = LocalGameTheme.current.pieceP1Res), contentDescription = null, modifier = Modifier.size(40.dp).offset(x = 40.dp).graphicsLayer { alpha = 0.4f })
                    }
                }

                // A toló Bábu
                val pieceOffset = if (currentTouchScale <= 0.9f) currentSwipeX.coerceAtLeast(-40f) else -40f
                Image(
                    painter = painterResource(id = LocalGameTheme.current.pieceP1Res), contentDescription = "Tutorial Piece",
                    modifier = Modifier.size(40.dp).offset(x = pieceOffset.dp)
                )

                // Az Ujj
                Icon(
                    imageVector = Icons.Rounded.TouchApp, contentDescription = "Swipe", tint = Color.White,
                    modifier = Modifier.size(56.dp).offset(x = currentSwipeX.dp, y = 20.dp).graphicsLayer { scaleX = currentTouchScale; scaleY = currentTouchScale; alpha = currentTouchAlpha }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = if (currentPhase == 1) "Swipe any piece to slide it.\nIt will only stop when it hits a wall or another piece."
                else "Hold your finger on the screen and move it slowly to select your target visually before releasing.",
                color = DialogContentColor, textAlign = TextAlign.Center, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            val infinitePulse = rememberInfiniteTransition()
            val textAlpha by infinitePulse.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "")

            Text(
                text = if (currentPhase == 1) "- TAP TO SEE PRO TIP -" else "- TAP TO PLAY -",
                color = accentColor.copy(alpha = textAlpha), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp
            )
        }
    }
}
@Composable
fun TutorialCompleteDialog(
    soundManager: SoundManager,
    onBackToMenu: () -> Unit // Most már a Menübe térünk vissza!
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    // Villogó szöveg animációja a "TAP TO CONTINUE"-hoz
    val infinitePulse = rememberInfiniteTransition()
    val textAlpha by infinitePulse.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "PulseAnim"
    )

    GlassDialog(onDismissRequest = { /* Csak a belső felületre kattintva zárható be */ }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    soundManager.playClick()
                    onBackToMenu()
                } // Bárhova kattintva kilép a menübe
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Text("TUTORIAL COMPLETED", color = accentColor, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(16.dp))

            Text(
                "Great job! You now know all the rules.\nYou are ready to test your skills in the real matches!",
                color = Color.White, textAlign = TextAlign.Center, fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            // Villogó "TAP TO EXIT" instrukció
            Text(
                text = "- TAP TO RETURN TO MENU -",
                color = accentColor.copy(alpha = textAlpha),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}