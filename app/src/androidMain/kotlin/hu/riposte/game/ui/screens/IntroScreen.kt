package hu.riposte.game.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import riposte.app.generated.resources.*
import org.jetbrains.compose.resources.*
import hu.riposte.game.ui.components.VolumetricLightOrgan
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
    val logoWidth = screenWidth * 0.85f

    var isFinishing by remember { mutableStateOf(false) }
    var showEffects by remember { mutableStateOf(false) }

    val logoAlpha = remember { Animatable(0f) }

    val overlayFadeAlpha by animateFloatAsState(
        targetValue = if (isFinishing) 0f else 1f,
        animationSpec = tween(600, easing = LinearEasing),
        label = "overlay_fade"
    )

    // ExoPlayer Setup
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Note: intro_video.mp4 is in commonMain/composeResources/files/
            // On Android, we can still use the generated R if the plugin exposes it, 
            // but we moved it out of res/raw.
            // Actually, we moved it to composeResources/files. 
            // We might need to handle it differently for ExoPlayer.
            // For now, I'll try to use the raw resource ID if it's still available through some magic, 
            // or use the asset path.
            val videoUri = Uri.parse("asset:///composeResources/riposte.app.generated.resources/files/intro_video.mp4")
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        isFinishing = true
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
            delay(600)
            exoPlayer.stop()
            onIntroFinished()
        }
    }

    // --- AZ ÚJ, LETISZTULT KOREOGRÁFIA ---
    LaunchedEffect(Unit) {
        // 1. Várjuk a videó 12. másodpercéig
        delay(10000)

        // 2. Megjelenik a fényorgona (automatikusan elkezd forogni és lüktetni)
        showEffects = true

        // 3. Megkezdjük a logó 1200 ms-os beúsztatását (fade-in)
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(12000, easing = LinearOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = !isFinishing) { isFinishing = true }
    ) {
        // VIDEO RÉTEG
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

        // EFFEKT ÉS LOGÓ RÉTEG
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = overlayFadeAlpha },
            contentAlignment = Alignment.Center
        ) {
            if (showEffects) {
                // 1. A Volumetric Light Organ (mögötte)
                VolumetricLightOrgan(
                    accentColor = Color(0xFFD4AF37),
                    centerYRatio = 0.5f
                )

                // 2. A fő logó egyszerű fade animációval (előtte)
                Image(
                    painter = painterResource(Res.drawable.intro_logo),
                    contentDescription = "La Riposte Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(logoWidth)
                        .graphicsLayer {
                            alpha = logoAlpha.value
                            // Egy hajszálnyi méretnövekedés a dráma kedvéért
                            scaleX = 0.95f + (logoAlpha.value * 0.05f)
                            scaleY = 0.95f + (logoAlpha.value * 0.05f)
                        }
                )
            }
        }

        // SKIP PROMPT
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