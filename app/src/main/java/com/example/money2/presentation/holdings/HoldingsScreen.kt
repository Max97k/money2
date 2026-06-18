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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingsScreen(
    navController: NavController,
    viewModel: HoldingsViewModel = koinViewModel()
) {
    val holdings by viewModel.holdings.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("持倉管理") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                    text = "目前無持倉，請點擊右下角新增。",
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
                        HoldingItem(holding = holding)
                    }
                }
            }
        }

        if (showDialog) {
            AddHoldingDialog(
                onDismiss = { showDialog = false },
                onConfirm = { symbol, name, quantity, avgCost, assetType ->
                    viewModel.addHolding(symbol, name, quantity, avgCost, assetType)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun HoldingItem(holding: Holding) {
    val totalValue = holding.quantity * holding.currentPrice
    val totalCost = holding.quantity * holding.avgCost
    val unrealizedPnl = totalValue - totalCost
    val pnlColor = if (unrealizedPnl >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = String.format("$%.2f", holding.currentPrice),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "數量: ${holding.quantity}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "成本: $${holding.avgCost}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "總價值: $${String.format("%.2f", totalValue)}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "未實現損益: ${if (unrealizedPnl >= 0) "+" else ""}${String.format("%.2f", unrealizedPnl)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = pnlColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
