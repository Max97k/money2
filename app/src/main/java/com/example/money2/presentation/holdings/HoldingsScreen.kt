package com.example.money2.presentation.holdings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.money2.domain.model.Holding
import org.koin.androidx.compose.koinViewModel

import androidx.compose.ui.res.stringResource
import com.example.money2.R
import com.example.money2.utils.CurrencyFormatter
import com.example.money2.utils.LocalCurrencyInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingsScreen(
    navController: NavController,
    viewModel: HoldingsViewModel = koinViewModel()
) {
    val holdings by viewModel.holdings.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.holdings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val settingsViewModel: com.example.money2.presentation.settings.SettingsViewModel = koinViewModel()
                    TextButton(onClick = { settingsViewModel.toggleCurrency() }) {
                        Text(LocalCurrencyInfo.current.currency, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { viewModel.refreshPrices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Prices")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Holding")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (holdings.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_holdings),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(holdings) { holding ->
                        HoldingItem(
                            holding = holding,
                            onClick = { navController.navigate(com.example.money2.presentation.navigation.NavRoutes.HoldingDetail.createRoute(holding.symbol)) }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddHoldingDialog(
                searchResults = searchResults,
                onSearch = { query -> viewModel.searchStocks(query) },
                onDismiss = { 
                    showDialog = false
                    viewModel.searchStocks("") // clear search results
                },
                onConfirm = { symbol, name, quantity, avgCost, assetType ->
                    viewModel.addHolding(symbol, name, quantity, avgCost, assetType)
                    showDialog = false
                    viewModel.searchStocks("") // clear search results
                }
            )
        }
    }
}

@Composable
fun HoldingItem(holding: Holding, onClick: () -> Unit = {}) {
    val totalValue = holding.totalQuantity * holding.currentPrice
    val totalCost = holding.totalQuantity * holding.avgCost
    val unrealizedPnl = totalValue - totalCost
    val pnlColor = if (unrealizedPnl >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    val currencyInfo = LocalCurrencyInfo.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = holding.symbol, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = holding.name, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = CurrencyFormatter.format(holding.currentPrice, currencyInfo.currency, currencyInfo.exchangeRate),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.quantity_label, holding.totalQuantity.toString()), style = MaterialTheme.typography.bodyMedium)
                Text(text = stringResource(R.string.cost_label, CurrencyFormatter.format(holding.avgCost, currencyInfo.currency, currencyInfo.exchangeRate).replace("$ ", "").replace("NT$ ", "")), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.total_value_label, CurrencyFormatter.format(totalValue, currencyInfo.currency, currencyInfo.exchangeRate).replace("$ ", "").replace("NT$ ", "")), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stringResource(R.string.unrealized_pnl_label, if (unrealizedPnl >= 0) "+" else "", CurrencyFormatter.format(unrealizedPnl, currencyInfo.currency, currencyInfo.exchangeRate).replace("$ ", "").replace("NT$ ", "")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = pnlColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
