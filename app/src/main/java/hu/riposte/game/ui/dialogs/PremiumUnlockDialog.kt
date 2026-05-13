package hu.riposte.game.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.engine.logic.SoundManager

// --- PREMIUM UNLOCK DIALOG ---
@Composable
fun PremiumUnlockDialog(
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    val accentColor = Color(0xFFD4AF37)

    GlassDialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "UNLOCK FULL EXPERIENCE",
                color = accentColor,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Support the development and get access to all features!",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            val features = listOf(
                "⚔️ Conquer the Top 14 Legends in Tournament",
                "🤖 Challenge Master & Grandmaster AI",
                "🌍 Compete on the Global Leaderboard",
                "☕ Support a solo indie developer"
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = feature,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .background(accentColor)
                    .clickable {
                        soundManager.playClick()
                        // TODO: Ide jön majd a Google Play Billing logika!
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "UPGRADE TO PREMIUM",
                    color = Color(0xFF1E272E),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "MAYBE LATER",
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable {
                        soundManager.playClick()
                        onDismiss()
                    }
                    .padding(8.dp)
            )
        }
    }
}
