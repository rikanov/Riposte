package hu.riposte.game.ui.utils

import androidx.compose.runtime.*
import org.jetbrains.compose.resources.*

@Composable
fun stringArrayResource(resource: StringArrayResource): List<String> {
    var result by remember { mutableStateOf(emptyList<String>()) }
    LaunchedEffect(resource) {
        result = getStringArray(resource)
    }
    return result
}
