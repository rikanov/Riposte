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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // --- NYERS MÉRETEK KISZÁMÍTÁSA ---
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.toFloat()

    // A logó alapmérete KÉTSZERESE a képernyőnek! (200vw)
    val baseLogoWidth = screenWidth * 2f
    // A 2000x502-es kép pontos képaránya: 502 / 2000 = 0.251
    val baseLogoHeight = baseLogoWidth * 0.251f

    // --- ANIMATABLE ÁLLAPOTOK ---
    // Kardok
    val swordsAlpha = remember { Animatable(0f) }
    val swordOffset = remember { Animatable(1000f) }
    val leftSwordRot = remember { Animatable(-120f) }
    val rightSwordRot = remember { Animatable(120f) }

    // Logó animációk
    val logoAlpha = remember { Animatable(0f) }
    // A 3D rotációt a Y-tengely nyújtásával szimuláljuk (0.01 = teljesen lapos vízszintes csík)
    val logoFlipY = remember { Animatable(0.01f) }
    val logoOffsetY = remember { Animatable(screenHeight * 0.4f) } // Képernyő aljáról indul
    val logoScale = remember { Animatable(1.0f) } // Mivel a baseWidth 200%, az 1.0f skála dupla képernyőt jelent!

    // Fényeffekt
    val shimmerProgress = remember { Animatable(0f) }

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
                        onIntroFinished()
                    }
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // --- AZ EPIKUS KOREOGRÁFIA ---
    LaunchedEffect(Unit) {
        delay(9000) // Várakozás a videó 9. másodpercéig

        // --- 1. FÁZIS (2000 ms) ---
        launch { swordsAlpha.animateTo(1f, tween(500)) }
        launch { swordOffset.animateTo(0f, tween(2000, easing = EaseOutCubic)) }
        launch { leftSwordRot.animateTo(0f, tween(2000, easing = EaseOutCubic)) }
        launch { rightSwordRot.animateTo(0f, tween(2000, easing = EaseOutCubic)) }

        // Logó megjelenik, felemelkedik, feláll és beáll a 3/4-es méretre
        launch { logoAlpha.animateTo(1f, tween(300)) }
        launch { logoOffsetY.animateTo(0f, tween(2000, easing = EaseOutCubic)) } // Középre megy
        launch { logoFlipY.animateTo(1f, tween(2000, easing = EaseOutCubic)) } // "Feláll" (Flip-up hatás)

        // Mivel a bázis 200%, ha 0.375-re szorozzuk: 2.0 * 0.375 = 0.75 (Pontosan a képernyő 3/4-e!)
        launch { logoScale.animateTo(0.375f, tween(2000, easing = EaseOutCubic)) }

        delay(2000) // Megvárjuk a fázis végét

        // --- 2. FÁZIS ---
        // Shimmer fényeffekt átsuhanása (800 ms)
        launch { shimmerProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing)) }

        delay(200) // Rövid szünet a villanás után

        // Logó végső nagyítása 1200 ms alatt
        // Skála 0.5f-re nő -> 2.0 * 0.5 = 1.0 (Pontosan a képernyő 100%-a!)
        logoScale.animateTo(0.4f, tween(1200, easing = EaseInOutQuart))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { exoPlayer.stop(); onIntroFinished() }
    ) {
        // --- 1. VIDEO LAYER ---
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

        // --- 2. OVERLAY LAYER ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // KARDOK
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

            // INTRO LOGO (Nyers erővel kényszerített méret)
            Image(
                painter = painterResource(id = R.drawable.intro_logo),
                contentDescription = "La Riposte Logo",
                // A FillBounds biztosítja, hogy pontosan kitöltse az általunk megadott requiredSize dobozt!
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .requiredSize(width = baseLogoWidth, height = baseLogoHeight) // Kőkemény 200vw doboz
                    .graphicsLayer {
                        translationY = logoOffsetY.value.dp.toPx()
                        scaleX = logoScale.value
                        // Itt történik a varázslat: a 3D forgást a Y skála szorzásával szimuláljuk
                        scaleY = logoScale.value * logoFlipY.value
                        alpha = logoAlpha.value
                        compositingStrategy = CompositingStrategy.Offscreen // Ez most már hibátlanul működik, mert nincs 3D forgatás!
                    }
                    .drawWithContent {
                        drawContent() // Eredeti logó rajzolása
                        val prog = shimmerProgress.value
                        if (prog > 0f && prog < 1f) {
                            val w = size.width
                            val h = size.height
                            // A fénycsík balról a képernyőn kívülről indul, és átsuhan a túlsó oldalra
                            val startX = (w * 1.5f) * prog - (w * 0.25f)
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.9f), Color.Transparent),
                                    start = Offset(startX, 0f),
                                    end = Offset(startX + (w * 0.15f), h) // Megdöntött fénycsík
                                ),
                                blendMode = BlendMode.SrcAtop // Szigorúan csak a betűkön csillan meg!
                            )
                        }
                    }
            )
        }

        // --- 3. SKIP PROMPT ---
        Text(
            text = "SKIP",
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).clickable { exoPlayer.stop(); onIntroFinished() }
        )
    }
}