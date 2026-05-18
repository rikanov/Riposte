package hu.riposte.game.engine.utils

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset

@Composable
expect fun rememberDeviceTilt(): State<Offset>
