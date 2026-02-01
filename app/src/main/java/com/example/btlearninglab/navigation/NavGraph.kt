package com.example.btlearninglab.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Screen.Scale.route) {
            ScaleScreen(navController = navController)
        }
        composable(route = Screen.Printer.route) {
            PrinterScreen(navController = navController)
        }
        composable(route = Screen.EPaper.route) {
            EPaperScreen(navController = navController)
        }
        composable(route = Screen.Log.route) {
            LogScreen(navController = navController)
        }
    }
}
