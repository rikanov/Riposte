package hu.riposte.game

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Game : Screen("game")
}

@Composable
fun RiposteApp() {
    val navController = rememberNavController()
    val gameViewModel: GameViewModel = viewModel()
    val activity = (LocalContext.current as? Activity)

    NavHost(navController = navController, startDestination = Screen.Main.route) {

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToTournament = {
                    // Később
                },
                onNavigateToTutorial = {
                    gameViewModel.isTutorialMode = true
                    gameViewModel.gamePhase = GameWaitingFor.TUTORIAL_WELCOME
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onNavigateToAiTraining = {
                    // AZONNALI INDÍTÁS: AI Mód, Váltakozó kezdés, Fix Depth=9
                    gameViewModel.startNewGame(
                        GameSettings(
                            gameMode = GameMode.VS_AI,
                            startingPlayer = StartingPlayer.ALTERNATING,
                            difficulty = 9,
                            riposteAllowed = gameViewModel.settings.riposteAllowed
                        )
                    )
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onNavigateToLocal = {
                    // AZONNALI INDÍTÁS: Local Mód, Váltakozó kezdés
                    gameViewModel.startNewGame(
                        GameSettings(
                            gameMode = GameMode.LOCAL_MULTIPLAYER,
                            startingPlayer = StartingPlayer.ALTERNATING,
                            difficulty = 5, // Local módnál a depth nem számít
                            riposteAllowed = gameViewModel.settings.riposteAllowed
                        )
                    )
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onNavigateToOnline = { /* Később implementáljuk */ },
                onNavigateToSettings = { /* Később implementáljuk */ },
                onExitGame = {
                    activity?.finish()
                }
            )
        }

        composable(Screen.Game.route) {
            RiposteGameBoard(
                gameViewModel = gameViewModel,
                onBackToMenu = {
                    // Ha a játékból lépünk ki, visszatérünk a Főmenübe
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                }
            )
        }
    }
}