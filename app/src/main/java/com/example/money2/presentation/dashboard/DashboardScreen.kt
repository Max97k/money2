package com.example.money2.presentation.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.money2.domain.model.Holding
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.ui.res.stringResource
import com.example.money2.R
import com.example.money2.utils.CurrencyFormatter
import com.example.money2.utils.LocalCurrencyInfo
import com.example.money2.utils.LocalSpotlightRegistry
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val trendPoints by viewModel.trendPoints.collectAsStateWithLifecycle()
    val currencyInfo = LocalCurrencyInfo.current
    val spotlightRegistry = LocalSpotlightRegistry.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                actions = {
                    val settingsViewModel: com.example.money2.presentation.settings.SettingsViewModel = org.koin.androidx.compose.koinViewModel()
                    TextButton(
                        onClick = { settingsViewModel.toggleCurrency() },
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInRoot()
                            val size = coordinates.size
                            spotlightRegistry("currency_toggle", Rect(position, Size(size.width.toFloat(), size.height.toFloat())))
                        }
                    ) {
                        Text(currencyInfo.currency, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.total_assets),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = stats?.totalValue ?: 0.0,
                    label = "totalValueAnimation",
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInRoot()
                        val size = coordinates.size
                        spotlightRegistry("dashboard_total", Rect(position, Size(size.width.toFloat(), size.height.toFloat())))
                    }
                ) { value ->
                    Text(
                        text = CurrencyFormatter.format(value, currencyInfo.currency, currencyInfo.exchangeRate),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (trendPoints.size > 1) {
                    AssetTrendChart(
                        points = trendPoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PnlCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.today_pnl),
                        amount = stats?.todayPnl ?: 0.0
                    )
                    PnlCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.total_pnl),
                        amount = stats?.totalPnl ?: 0.0
                    )
                }
            }
            
            Text(
                text = stringResource(R.string.tab_holdings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(holdings) { holding ->
                    HoldingItem(holding = holding)
                }
            }
        }
    }
}

@Composable
fun PnlCard(modifier: Modifier = Modifier, title: String, amount: Double) {
    val isPositive = amount >= 0
    val amountColor = if (isPositive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    val currencyInfo = LocalCurrencyInfo.current

    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedContent(
                targetState = amount,
                label = "amountAnimation"
            ) { value ->
                Text(
                    text = "${if (value >= 0) "+" else ""}${CurrencyFormatter.format(value, currencyInfo.currency, currencyInfo.exchangeRate)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
            }
        }
    }
}

@Composable
fun HoldingItem(holding: Holding) {
    val nativeCurrency = if (holding.symbol.endsWith(".TW") || holding.symbol.endsWith(".TWO")) "TWD" else "USD"
    val currencyInfo = LocalCurrencyInfo.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = holding.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = holding.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val totalValue = holding.currentPrice * holding.totalQuantity
                Text(
                    text = CurrencyFormatter.format(totalValue, currencyInfo.currency, currencyInfo.exchangeRate, nativeCurrency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CurrencyFormatter.format(holding.currentPrice, currencyInfo.currency, currencyInfo.exchangeRate, nativeCurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return format.format(amount)
}

@Composable
fun AssetTrendChart(points: List<Float>, modifier: Modifier = Modifier) {
    if (points.size < 2) return
    val lineColor = MaterialTheme.colorScheme.primary
    val gradientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    
    Canvas(modifier = modifier) {
        val maxVal = points.maxOrNull() ?: 0f
        val minVal = points.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(0.1f)
        
        val width = size.width
        val height = size.height
        val pointSpacing = width / (points.size - 1)
        
        val path = Path()
        
        for (i in points.indices) {
            val x = i * pointSpacing
            // Calculate y, mapping the value within the 10%-90% height range
            val y = height - ((points[i] - minVal) / range) * height * 0.8f - height * 0.1f
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = (i - 1) * pointSpacing
                val prevY = height - ((points[i - 1] - minVal) / range) * height * 0.8f - height * 0.1f
                
                // Cubic bezier for smooth curve
                val cx = (prevX + x) / 2
                path.cubicTo(cx, prevY, cx, y, x, y)
            }
        }
        
        drawPath(path = path, color = lineColor, style = Stroke(width = 3.dp.toPx()))
        
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(listOf(gradientColor, Color.Transparent))
        )
    }
}
