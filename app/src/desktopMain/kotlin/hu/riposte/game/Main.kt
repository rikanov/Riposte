package hu.riposte.game

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import hu.riposte.game.engine.logic.GameViewModel
import hu.riposte.game.ui.screens.MainScreen
import hu.riposte.game.ui.screens.RiposteGameBoard
import hu.riposte.game.ui.screens.TournamentScreen
import hu.riposte.game.ui.theme.RiposteTheme

sealed class Screen {
    object Main : Screen()
    object Game : Screen()
    object Tournament : Screen()
}

fun main() = application {
    val windowState = rememberWindowState(width = 720.dp, height = 1280.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Riposte",
        state = windowState
    ) {
        val gameViewModel = remember { GameViewModel() }
        val soundManager = remember { gameViewModel.soundManager }
        
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }

        RiposteTheme {
            when (currentScreen) {
                is Screen.Main -> {
                    MainScreen(
                        isInterruptedGame = gameViewModel.isInterruptedGame,
                        gameViewModel = gameViewModel,
                        soundManager = soundManager,
                        onResumeGame = { currentScreen = Screen.Game },
                        onNavigateToTournament = { currentScreen = Screen.Tournament },
                        onNavigateToTutorial = { currentScreen = Screen.Game },
                        onNavigateToAiTraining = { currentScreen = Screen.Game },
                        onNavigateToLocal = { currentScreen = Screen.Game },
                        onExitGame = { exitApplication() }
                    )
                }
                is Screen.Game -> {
                    RiposteGameBoard(
                        gameViewModel = gameViewModel,
                        soundManager = soundManager,
                        onBackToMenu = { 
                            if (gameViewModel.isTournamentMode) currentScreen = Screen.Tournament
                            else currentScreen = Screen.Main
                        }
                    )
                }
                is Screen.Tournament -> {
                    TournamentScreen(
                        gameViewModel = gameViewModel,
                        soundManager = soundManager,
                        onBackToMenu = { currentScreen = Screen.Main },
                        onStartMatch = { currentScreen = Screen.Game },
                        onResumeGame = { currentScreen = Screen.Game }
                    )
                }
            }
        }
    }
}
