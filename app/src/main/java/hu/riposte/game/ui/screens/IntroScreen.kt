package hu.riposte.game.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import hu.riposte.game.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun IntroScreen(
    onIntroFinished: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.toFloat()

    val baseLogoWidth = screenWidth * 2f
    val baseLogoHeight = baseLogoWidth * 0.251f

    var isFinishing by remember { mutableStateOf(false) }

    val swordsAlpha = remember { Animatable(0f) }
    val swordOffset = remember { Animatable(1000f) }
    val leftSwordRot = remember { Animatable(-120f) }
    val rightSwordRot = remember { Animatable(120f) }

    val logoAlpha = remember { Animatable(0f) }
    val logoFlipY = remember { Animatable(0.01f) }
    val logoOffsetY = remember { Animatable(screenHeight * 0.4f) }
    val logoScale = remember { Animatable(1.0f) }

    val shimmerProgress = remember { Animatable(0f) }

    val overlayFadeAlpha by animateFloatAsState(
        targetValue = if (isFinishing) 0f else 1f,
        animationSpec = tween(500, easing = LinearEasing),
        label = "overlay_fade"
    )

    // ExoPlayer Setup
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.intro_video}")
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        isFinishing = true // Azonnali kilépés helyett elhalványulás indul
                    }
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(isFinishing) {
        if (isFinishing) {
            delay(500) // Megvárjuk, amíg a fade-out lefut
            exoPlayer.stop()
            onIntroFinished()
        }
    }

    // --- AZ EPIKUS KOREOGRÁFIA ---
    LaunchedEffect(Unit) {
        delay(9000)

        // --- 1. FÁZIS (2000 ms) ---
        launch { swordsAlpha.animateTo(1f, tween(500)) }
        launch { swordOffset.animateTo(0f, tween(2000, easing = EaseOutCubic)) }
        launch { leftSwordRot.animateTo(0f, tween(2000, easing = EaseOutCubic)) }
        launch { rightSwordRot.animateTo(0f, tween(2000, easing = EaseOutCubic)) }

        launch { logoAlpha.animateTo(1f, tween(300)) }
        launch { logoOffsetY.animateTo(0f, tween(2000, easing = EaseOutCubic)) }
        launch { logoFlipY.animateTo(1f, tween(2000, easing = EaseOutCubic)) }
        launch { logoScale.animateTo(0.375f, tween(2000, easing = EaseOutCubic)) }

        delay(2000)

        // --- 2. FÁZIS ---
        launch { shimmerProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing)) }

        delay(200)

        logoScale.animateTo(0.5f, tween(2000, easing = EaseInOutQuart))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = !isFinishing) { isFinishing = true } // Kattintáskor is lágyan halványul el
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = overlayFadeAlpha }, // <-- ÚJ: Elhalványul a réteg
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.intro_sword_left),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                        .graphicsLayer {
                            translationX = -swordOffset.value
                            rotationZ = leftSwordRot.value
                            alpha = swordsAlpha.value
                        }
                )
                Image(
                    painter = painterResource(id = R.drawable.intro_sword_right),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                        .graphicsLayer {
                            translationX = swordOffset.value
                            rotationZ = rightSwordRot.value
                            alpha = swordsAlpha.value
                        }
                )
            }

            // INTRO LOGO
            Image(
                painter = painterResource(id = R.drawable.intro_logo),
                contentDescription = "La Riposte Logo",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .requiredSize(width = baseLogoWidth, height = baseLogoHeight)
                    .graphicsLayer {
                        translationY = logoOffsetY.value.dp.toPx()
                        scaleX = logoScale.value
                        scaleY = logoScale.value * logoFlipY.value
                        alpha = logoAlpha.value
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        val prog = shimmerProgress.value
                        if (prog > 0f && prog < 1f) {
                            val w = size.width
                            val h = size.height
                            val startX = (w * 1.5f) * prog - (w * 0.25f)
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.9f), Color.Transparent),
                                    start = Offset(startX, 0f),
                                    end = Offset(startX + (w * 0.15f), h)
                                ),
                                blendMode = BlendMode.SrcAtop
                            )
                        }
                    }
            )
        }

        // --- 3. SKIP PROMPT ---
        Text(
            text = "SKIP",
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .graphicsLayer { alpha = overlayFadeAlpha }
        )
    }
}