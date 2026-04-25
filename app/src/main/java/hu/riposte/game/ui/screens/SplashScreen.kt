package hu.riposte.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.ui.screens.AmbientDustVFX
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animációs értékek
    val logoScale = remember { Animatable(0.85f) }
    val logoAlpha = remember { Animatable(0f) }
    val subTextAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. A logó lassan beúszik és felnagyítódik
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2000, easing = EaseOutSine)
            )
        }
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = EaseInOutSine)
            )
        }

        // 2. Az alcím pici késéssel jelenik meg
        launch {
            delay(800)
            subTextAlpha.animateTo(
                targetValue = 0.5f,
                animationSpec = tween(durationMillis = 1000)
            )
        }

        // 3. Várunk egy kicsit, hogy a játékos kiélvezze a látványt, majd váltunk
        delay(2800)
        onSplashFinished()
    }

    // A Splash Screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F141E)), // Mélykék/fekete alap
        contentAlignment = Alignment.Center
    ) {
        // Használjuk a Bajnokságból ismerős szálló szikrákat!
        AmbientDustVFX()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = logoScale.value
                scaleY = logoScale.value
            }
        ) {
            // Főcím (Később ezt cserélhetjük az Inkscape-es logódra)
            Text(
                text = "R I P O S T E",
                color = Color(0xFFD4AF37), // Elegáns arany
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                modifier = Modifier.graphicsLayer { alpha = logoAlpha.value }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Alcím
            Text(
                text = "TACTICAL BOARD GAME",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                modifier = Modifier.graphicsLayer { alpha = subTextAlpha.value }
            )
        }
    }
}