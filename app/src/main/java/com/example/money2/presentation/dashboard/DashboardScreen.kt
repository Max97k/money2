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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val currencyInfo = LocalCurrencyInfo.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                actions = {
                    val settingsViewModel: com.example.money2.presentation.settings.SettingsViewModel = koinViewModel()
                    TextButton(onClick = { settingsViewModel.toggleCurrency() }) {
                        Text(currencyInfo.currency, fontWeight = FontWeight.Bold)
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
                    label = "totalValueAnimation"
                ) { value ->
                    Text(
                        text = CurrencyFormatter.format(value, currencyInfo.currency, currencyInfo.exchangeRate),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

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
                    text = CurrencyFormatter.format(totalValue, currencyInfo.currency, currencyInfo.exchangeRate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CurrencyFormatter.format(holding.currentPrice, currencyInfo.currency, currencyInfo.exchangeRate),
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
