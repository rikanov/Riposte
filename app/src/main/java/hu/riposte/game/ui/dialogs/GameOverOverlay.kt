package hu.riposte.game.ui.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.R

@Composable
fun GameOverOverlay(
    isWin: Boolean,
    isTimeOut: Boolean,
    subText: String,
    accentColor: Color
) {
    val mainText = if (isTimeOut) {
        stringResource(id = R.string.game_over_timeout)
    } else if (isWin) {
        stringResource(id = R.string.game_over_victory)
    } else {
        stringResource(id = R.string.game_over_defeat)
    }

    val textColor = if (isWin) accentColor else Color(0xFFFF5555)

    // Pulzáló animáció a főszövegnek
    val infiniteTransition = rememberInfiniteTransition(label = "GameOverPulse")
    val textScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "textScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        // Tűzijáték csak győzelem esetén
        if (isWin) {
            FireworksOverlay()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-40).dp)
        ) {
            Text(
                text = mainText,
                color = textColor,
                fontSize = 54.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                modifier = Modifier.graphicsLayer { scaleX = textScale; scaleY = textScale }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subText,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}