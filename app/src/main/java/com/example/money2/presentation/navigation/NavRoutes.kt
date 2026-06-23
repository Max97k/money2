package com.example.money2.presentation.navigation

sealed class NavRoutes(val route: String) {
    data object Dashboard : NavRoutes("dashboard")
    data object Holdings : NavRoutes("holdings")
    data object Settings : NavRoutes("settings")
    data object AddHolding : NavRoutes("add_holding")
    
    data object HoldingDetail : NavRoutes("holding_detail/{symbol}") {
        fun createRoute(symbol: String) = "holding_detail/$symbol"
    }
}
