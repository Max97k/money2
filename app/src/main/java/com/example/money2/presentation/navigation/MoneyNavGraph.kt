package com.example.money2.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.money2.presentation.dashboard.DashboardScreen
import com.example.money2.presentation.holdings.HoldingsScreen
import com.example.money2.presentation.settings.SettingsScreen
import com.example.money2.presentation.settings.SettingsScreen

@Composable
fun MoneyNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Dashboard.route
    ) {
        composable(NavRoutes.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        composable(NavRoutes.Holdings.route) {
            HoldingsScreen(navController = navController)
        }
        composable(NavRoutes.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
