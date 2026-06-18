package com.example.money2.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.money2.presentation.dashboard.DashboardScreen
import com.example.money2.presentation.holdings.AddHoldingDialog
import com.example.money2.presentation.holdings.HoldingsScreen
import com.example.money2.presentation.holdings.detail.HoldingDetailScreen
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
        
        composable(
            route = NavRoutes.HoldingDetail.route
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            HoldingDetailScreen(
                symbol = symbol,
                navController = navController
            )
        }
        composable(NavRoutes.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
