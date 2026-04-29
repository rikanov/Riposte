package hu.riposte.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.ui.theme.LocalGameTheme

@Composable
fun RiposteDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHanging: Boolean = false // Ha a gomb a dialógus alján "lóg"
) {
    val theme = LocalGameTheme.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHanging) theme.uiAccentColor else theme.uiAccentColor.copy(alpha = 0.15f))
            .border(
                1.dp,
                theme.uiAccentColor,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isHanging) Color.Black else theme.uiAccentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
