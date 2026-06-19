package com.example.money2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.money2.utils.LocalSpotlightRegistry
import org.koin.androidx.viewmodel.ext.android.getViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.money2.presentation.components.DynamicBackground
import com.example.money2.presentation.components.SpotlightTour
import com.example.money2.presentation.components.SpotlightTarget
import com.example.money2.data.local.prefs.EncryptedPrefs
import org.koin.compose.koinInject
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.ui.res.stringResource

data class BottomNavItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
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
                
                val spotlightTargets = remember { androidx.compose.runtime.mutableStateMapOf<String, Rect>() }

                CompositionLocalProvider(
                    LocalCurrencyInfo provides CurrencyInfo(selectedCurrency, exchangeRate),
                    LocalSpotlightRegistry provides { key, rect -> spotlightTargets[key] = rect }
                ) {
                    val prefs: EncryptedPrefs = koinInject()
                    val isFirstLaunch by prefs.isFirstLaunchFlow.collectAsState()
                    var holdingsTabRect by remember { mutableStateOf(Rect.Zero) }
                    var tourCurrentIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }

                    DynamicBackground {
                        Scaffold(
                            containerColor = Color.Transparent,
                            bottomBar = {
                                NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) {
                                bottomNavItems.forEach { item ->
                                    val labelText = stringResource(item.labelRes)
                                    NavigationBarItem(
                                        modifier = Modifier.then(
                                            if (item.route == NavRoutes.Holdings.route) {
                                                Modifier.onGloballyPositioned { coordinates ->
                                                    val position = coordinates.positionInRoot()
                                                    val size = coordinates.size
                                                    holdingsTabRect = Rect(position, Size(size.width.toFloat(), size.height.toFloat()))
                                                }
                                            } else Modifier
                                        ),
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
                        
                        if (isFirstLaunch && holdingsTabRect != Rect.Zero && currentRoute == NavRoutes.Dashboard.route) {
                            val targets = mutableListOf<SpotlightTarget>()
                            
                            spotlightTargets["dashboard_total"]?.let { rect ->
                                targets.add(SpotlightTarget(
                                    rect = rect,
                                    title = "歡迎來到 money2！",
                                    description = "這裡會為您統整所有的投資組合，自動繪製真實的資產成長趨勢圖與總損益。就算目前空空如也，馬上就會豐富起來！"
                                ))
                            }
                            
                            spotlightTargets["currency_toggle"]?.let { rect ->
                                targets.add(SpotlightTarget(
                                    rect = rect,
                                    title = "無縫多幣別轉換",
                                    description = "有美股也有台股？沒問題！隨時點擊右上角，一鍵為您將所有資產換算成您熟悉的幣別。"
                                ))
                            }
                            
                            targets.add(SpotlightTarget(
                                rect = holdingsTabRect,
                                title = "開始您的第一步",
                                description = "現在，請點擊下方的『持倉』，加入您的第一檔股票或 ETF 吧！"
                            ))

                            if (targets.isNotEmpty()) {
                                SpotlightTour(
                                    targets = targets,
                                    currentTargetIndex = tourCurrentIndex,
                                    onNext = {
                                        if (tourCurrentIndex < targets.size - 1) {
                                            tourCurrentIndex++
                                        } else {
                                            prefs.saveIsFirstLaunch(false)
                                            navController.navigate(NavRoutes.Holdings.route)
                                        }
                                    },
                                    onSkip = {
                                        prefs.saveIsFirstLaunch(false)
                                    }
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}