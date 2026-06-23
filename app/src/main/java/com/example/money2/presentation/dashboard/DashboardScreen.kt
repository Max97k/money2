package com.example.money2.presentation.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitFirstDown
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import com.example.money2.utils.LocalCurrencyInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.money2.domain.model.ChartUiState
import com.example.money2.domain.model.TimeRange
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
    val chartUiState by viewModel.chartUiState.collectAsStateWithLifecycle()
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

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val ranges = listOf(
                        "1D" to TimeRange.DAY_1, "5D" to TimeRange.DAY_5, 
                        "1M" to TimeRange.MONTH_1, "6M" to TimeRange.MONTH_6,
                        "YTD" to TimeRange.YTD, "1Y" to TimeRange.YEAR_1,
                        "5Y" to TimeRange.YEAR_5, "MAX" to TimeRange.MAX
                    )
                    ranges.forEach { (label, range) ->
                        TextButton(
                            onClick = { viewModel.selectTimeRange(range) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, color = if (chartUiState.selectedRange == range) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (chartUiState.assetPoints.size > 1) {
                    AssetTrendChart(
                        state = chartUiState,
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
fun AssetTrendChart(state: ChartUiState, modifier: Modifier = Modifier) {
    val values = state.assetPoints.map { it.normalizedValue }
    if (values.size < 2) return
    val bValues = state.benchmarkPoints?.map { it.normalizedValue }
    
    val lineColor = MaterialTheme.colorScheme.primary
    val gradientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val benchmarkColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
    val currencyInfo = LocalCurrencyInfo.current
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var chartWidth by remember { mutableStateOf(0f) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.US) }
    
    Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()
        .onGloballyPositioned { chartWidth = it.size.width.toFloat() }
        .pointerInput(state.assetPoints) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown()
                    val pointSpacing = size.width.toFloat() / (values.size - 1).coerceAtLeast(1)
                    selectedIndex = (down.position.x / pointSpacing).roundToInt().coerceIn(0, values.lastIndex)

                    var isDragging = true
                    while (isDragging) {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        
                        // Handle pointer movements
                        changes.forEach { change ->
                            if (change.pressed) {
                                selectedIndex = (change.position.x / pointSpacing).roundToInt().coerceIn(0, values.lastIndex)
                            }
                        }
                        
                        // Break if all pointers are released
                        if (!changes.any { it.pressed }) {
                            isDragging = false
                        }
                    }
                    selectedIndex = null
                }
            }
        }
    ) {
        val maxVal = maxOf(values.maxOrNull() ?: 0f, bValues?.maxOrNull() ?: 0f)
        val minVal = minOf(values.minOrNull() ?: 0f, bValues?.minOrNull() ?: 0f)
        val range = (maxVal - minVal).coerceAtLeast(0.01f)
        
        val width = size.width
        val height = size.height
        val pointSpacing = width / (values.size - 1)
        
        val path = Path()
        
        for (i in values.indices) {
            val x = i * pointSpacing
            // Calculate y, mapping the value within the 10%-90% height range
            val y = height - ((values[i] - minVal) / range) * height * 0.8f - height * 0.1f
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = (i - 1) * pointSpacing
                val prevY = height - ((values[i - 1] - minVal) / range) * height * 0.8f - height * 0.1f
                
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
        
        if (bValues != null && bValues.size == values.size) {
            val bPath = Path()
            for (i in bValues.indices) {
                val x = i * pointSpacing
                val y = height - ((bValues[i] - minVal) / range) * height * 0.8f - height * 0.1f
                if (i == 0) {
                    bPath.moveTo(x, y)
                } else {
                    val prevX = (i - 1) * pointSpacing
                    val prevY = height - ((bValues[i - 1] - minVal) / range) * height * 0.8f - height * 0.1f
                    val cx = (prevX + x) / 2
                    bPath.cubicTo(cx, prevY, cx, y, x, y)
                }
            }
            drawPath(
                path = bPath,
                color = benchmarkColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
        }
        
        selectedIndex?.let { index ->
            val x = index * pointSpacing
            val y = height - ((values[index] - minVal) / range) * height * 0.8f - height * 0.1f
            
            // Draw crosshair
            drawLine(
                color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset(x, height),
                strokeWidth = 1.dp.toPx()
            )
            
            // Draw focus dot
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
    
        selectedIndex?.let { index ->
            val pointSpacing = if (values.size > 1) chartWidth / (values.size - 1) else 0f
            val x = index * pointSpacing
            val tooltipWidth = 170.dp
            
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .offset { 
                            val tooltipWidthPx = tooltipWidth.roundToPx()
                            val minX = 0
                            val maxX = (chartWidth - tooltipWidthPx).toInt().coerceAtLeast(0)
                            val preferredX = x.toInt() - tooltipWidthPx / 2
                            IntOffset(preferredX.coerceIn(minX, maxX), 0)
                        }
                        .width(tooltipWidth)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dateFormat.format(Date(state.assetPoints[index].timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.format(
                                amount = state.assetPoints[index].value.toDouble(),
                                targetCurrency = currencyInfo.currency,
                                exchangeRate = currencyInfo.exchangeRate
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val assetChange = values[index] * 100
                            Text(
                                text = String.format(Locale.US, "%+.2f%%", assetChange),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (assetChange >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            if (bValues != null) {
                                val bChange = bValues[index] * 100
                                Text(
                                    text = "S&P 500: ${String.format(Locale.US, "%+.2f%%", bChange)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
