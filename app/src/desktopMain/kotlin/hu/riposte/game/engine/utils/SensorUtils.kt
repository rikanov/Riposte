package hu.riposte.game.engine.utils

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset

@Composable
actual fun rememberDeviceTilt(): State<Offset> {
    return remember { mutableStateOf(Offset.Zero) }
}
