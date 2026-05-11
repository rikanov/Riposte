package hu.riposte.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import hu.riposte.game.ui.theme.LocalGameTheme

// RENDSZERSZÍNEK (Fix, a MainScreen hangulatához)
val SystemAccent = Color(0xFFD4AF37) // Arany / Sárgaréz
val SystemSurface = Color(0xFF4A5570).copy(alpha = 0.2f)

@Composable
fun RiposteSystemButton(
    text: String,
    modifier: Modifier = Modifier,
    isHanging: Boolean = false,
    onClick: () -> Unit
) {
    val themeFont = LocalGameTheme.current.fontFamily

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHanging) SystemAccent else SystemSurface)
            .border(1.dp, SystemAccent, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isHanging) Color.Black else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            maxLines = 2,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            fontFamily = themeFont
        )
    }
}

@Composable
fun RiposteSystemToggleButton(
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val themeFont = LocalGameTheme.current.fontFamily

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) SystemAccent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                if (isActive) SystemAccent else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = themeFont
        )
    }
}
