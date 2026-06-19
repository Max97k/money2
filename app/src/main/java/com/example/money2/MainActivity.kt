package com.example.money2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.money2.presentation.navigation.MoneyNavGraph
import com.example.money2.presentation.navigation.NavRoutes
import com.example.money2.presentation.settings.SettingsViewModel
import com.example.money2.ui.theme.MoneyTheme
import com.example.money2.utils.CurrencyInfo
import com.example.money2.utils.LocalCurrencyInfo
import org.koin.androidx.viewmodel.ext.android.getViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider

import androidx.compose.ui.res.stringResource

data class BottomNavItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomNavItems = listOf(
                    BottomNavItem(R.string.tab_dashboard, Icons.Filled.Home, NavRoutes.Dashboard.route),
                    BottomNavItem(R.string.tab_holdings, Icons.Filled.PieChart, NavRoutes.Holdings.route),
                    BottomNavItem(R.string.tab_settings, Icons.Filled.Settings, NavRoutes.Settings.route)
                )

                val settingsViewModel: SettingsViewModel = getViewModel()
                val selectedCurrency by settingsViewModel.selectedCurrency.collectAsState()
                val exchangeRate by settingsViewModel.exchangeRate.collectAsState()

                CompositionLocalProvider(
                    LocalCurrencyInfo provides CurrencyInfo(selectedCurrency, exchangeRate)
                ) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                bottomNavItems.forEach { item ->
                                    val labelText = stringResource(item.labelRes)
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = labelText) },
                                        label = { Text(labelText) },
                                        selected = currentRoute == item.route,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(NavRoutes.Dashboard.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                            MoneyNavGraph(
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}