package com.example.btlearninglab.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.btlearninglab.ui.EPaperScreen
import com.example.btlearninglab.ui.HomeScreen
import com.example.btlearninglab.ui.LogScreen
import com.example.btlearninglab.ui.PrinterScreen
import com.example.btlearninglab.ui.ScaleScreen

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
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Scale.route) {
            ScaleScreen(navController = navController)
        }
        composable(Screen.Printer.route) {
            PrinterScreen(navController = navController)
        }
        composable(Screen.EPaper.route) {
            EPaperScreen(navController = navController)
        }
        composable(Screen.Log.route) {
            LogScreen(navController = navController)
        }
    }
}
