package hu.riposte.game

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hu.riposte.game.engine.data.GameMode
import hu.riposte.game.engine.data.GameSettings
import hu.riposte.game.engine.logic.GameViewModel
import hu.riposte.game.engine.data.GameWaitingFor
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.screens.RiposteGameBoard
import hu.riposte.game.engine.data.StartingPlayer
import hu.riposte.game.ui.screens.IntroScreen
import hu.riposte.game.ui.screens.MainScreen
import hu.riposte.game.ui.screens.SplashScreen
import hu.riposte.game.ui.screens.TournamentScreen

sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Splash : Screen("splash")
    object Main : Screen("main")
    object Game : Screen("game")
    object Tournament : Screen("tournament")
}

@Composable
fun RiposteApp(soundManager: SoundManager) {
    val navController = rememberNavController()
    val gameViewModel = remember { GameViewModel() }
    val activity = (LocalContext.current as? Activity)

    NavHost(navController = navController, startDestination = Screen.Intro.route) {

        composable(Screen.Intro.route) {
            IntroScreen(
                onIntroFinished = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Intro.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                isInterruptedGame = gameViewModel.isInterruptedGame,
                gameViewModel = gameViewModel,
                soundManager = soundManager,
                onResumeGame = {
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onNavigateToTournament = {
                    navController.navigate(Screen.Tournament.route) { launchSingleTop = true }
                },
                onNavigateToTutorial = {
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onNavigateToAiTraining = {
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onNavigateToLocal = {
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onExitGame = {
                    activity?.finish()
                }
            )
        }

        composable(
            route = Screen.Game.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(700)
                )
            },
            exitTransition = {
                // PREMIUM POLISH: Smooth diagonal slide out combined with fade
                slideOut(
                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                ) { fullSize -> 
                    IntOffset(fullSize.width / 4, fullSize.height) 
                } + fadeOut(animationSpec = tween(800))
            }
        ) {
            RiposteGameBoard(
                gameViewModel = gameViewModel,
                soundManager = soundManager,
                onBackToMenu = {
                    if (gameViewModel.isTournamentMode) {
                        navController.popBackStack(Screen.Tournament.route, inclusive = false)
                    } else {
                        navController.popBackStack(Screen.Main.route, inclusive = false)
                    }
                }
            )
        }

        composable(Screen.Tournament.route) {
            TournamentScreen(
                gameViewModel = gameViewModel,
                soundManager = soundManager,
                onBackToMenu = {
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                },
                onStartMatch = {
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onResumeGame = {
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                }
            )
        }
    }
}
