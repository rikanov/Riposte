package hu.riposte.game

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hu.riposte.game.engine.data.GameMode
import hu.riposte.game.engine.data.GameSettings
import hu.riposte.game.engine.logic.GameViewModel
import hu.riposte.game.engine.data.GameWaitingFor
import hu.riposte.game.engine.utils.RiposteGameBoard
import hu.riposte.game.engine.data.StartingPlayer
import hu.riposte.game.ui.screens.MainScreen
import hu.riposte.game.ui.screens.TournamentScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object Game : Screen("game")
    object Tournament : Screen("tournament")
}

@Composable
fun RiposteApp() {
    val navController = rememberNavController()
    val gameViewModel: GameViewModel = viewModel()
    val activity = (LocalContext.current as? Activity)

    // A startDestination helyesen a Splash!
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        // --- 1. SPLASH SCREEN (Önálló blokk!) ---
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // --- 2. MAIN SCREEN ---
        composable(Screen.Main.route) {
            MainScreen(
                isInterruptedGame = gameViewModel.isInterruptedGame,
                gameViewModel = gameViewModel,
                onResumeGame = {
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onNavigateToTournament = {
                    navController.navigate(Screen.Tournament.route) { launchSingleTop = true }
                },
                onNavigateToTutorial = {
                    gameViewModel.isTutorialMode = true
                    gameViewModel.gamePhase = GameWaitingFor.TUTORIAL_WELCOME
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onNavigateToAiTraining = {
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onNavigateToLocal = {
                    // AZONNALI INDÍTÁS: Local Mód, Váltakozó kezdés
                    gameViewModel.startNewGame(
                        GameSettings(
                            gameMode = GameMode.LOCAL_MULTIPLAYER,
                            startingPlayer = StartingPlayer.ALTERNATING,
                            difficulty = 5, // Local módnál a depth nem számít
                            riposteAllowed = gameViewModel.settings.riposteAllowed,
                        ),
                        isTournament = false
                    )
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                // Az onNavigateToOnline törölve!
                onExitGame = {
                    activity?.finish()
                }
            )
        }

        // --- 3. GAME BOARD ---
        composable(Screen.Game.route) {
            RiposteGameBoard(
                gameViewModel = gameViewModel,
                onBackToMenu = {
                    if (gameViewModel.isTournamentMode) {
                        navController.popBackStack(Screen.Tournament.route, inclusive = false)
                    } else {
                        navController.popBackStack(Screen.Main.route, inclusive = false)
                    }
                }
            )
        }

        // --- 4. TOURNAMENT SCREEN ---
        composable(Screen.Tournament.route) {
            TournamentScreen(
                gameViewModel = gameViewModel,
                onBackToMenu = {
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                },
                onStartMatch = {
                    gameViewModel.isTournamentMode = true
                    val opponent = gameViewModel.tournamentManager.getNextOpponent()

                    gameViewModel.startNewGame(
                        GameSettings(
                            gameMode = GameMode.VS_AI,
                            startingPlayer = StartingPlayer.ALTERNATING,
                            difficulty = opponent.engineDepth,
                            riposteAllowed = true
                        ),
                        isTournament = true
                    )
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onResumeGame = {
                    // ÚJ: Visszaugrunk a megkezdett játékba!
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                }
            )
        }
    }
}