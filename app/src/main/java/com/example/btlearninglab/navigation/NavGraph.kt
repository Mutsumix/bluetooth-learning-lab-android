package com.example.btlearninglab.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.btlearninglab.ui.epaper.EPaperScreen
import com.example.btlearninglab.ui.home.HomeScreen
import com.example.btlearninglab.ui.log.LogScreen
import com.example.btlearninglab.ui.printer.PrinterScreen
import com.example.btlearninglab.ui.scale.ScaleScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scale : Screen("scale")
    object Printer : Screen("printer")
    object EPaper : Screen("epaper")
    object Log : Screen("log")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.Scale.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            ScaleScreen(navController = navController)
        }
        composable(
            route = Screen.Printer.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            PrinterScreen(navController = navController)
        }
        composable(
            route = Screen.EPaper.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            EPaperScreen(navController = navController)
        }
        composable(
            route = Screen.Log.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            LogScreen(navController = navController)
        }
    }
}
