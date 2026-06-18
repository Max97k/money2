package com.example.money2.presentation.holdings.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.money2.domain.model.HoldingTransaction
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.money2.utils.CurrencyFormatter
import com.example.money2.utils.LocalCurrencyInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingDetailScreen(
    symbol: String,
    navController: NavController,
    viewModel: HoldingDetailViewModel = koinViewModel()
) {
    val holding by viewModel.holding.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val currencyInfo = LocalCurrencyInfo.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(holding?.name ?: symbol) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            holding?.let { h ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "目前價格: ${CurrencyFormatter.format(h.currentPrice, currencyInfo.currency, currencyInfo.exchangeRate)}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "總持倉量: ${h.totalQuantity}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "平均成本: ${CurrencyFormatter.format(h.avgCost, currencyInfo.currency, currencyInfo.exchangeRate)}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Text(
                text = "交易紀錄 (Lots)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { tx ->
                    TransactionLotItem(tx)
                }
            }
        }

        if (showDialog) {
            AddTransactionDialog(
                onDismiss = { showDialog = false },
                onConfirm = { type, qty, price, dateMillis ->
                    viewModel.addTransaction(type, qty, price, dateMillis)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun TransactionLotItem(tx: HoldingTransaction) {
    val isBuy = tx.type.name == "BUY"
    val color = if (isBuy) Color(0xFF4CAF50) else Color(0xFFF44336)
    val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val currencyInfo = LocalCurrencyInfo.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = if (isBuy) "買入" else "賣出", color = color, fontWeight = FontWeight.Bold)
                Text(text = formatter.format(Date(tx.dateMillis)), style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "數量: ${tx.quantity}")
                Text(text = "價格: ${CurrencyFormatter.format(tx.price, currencyInfo.currency, currencyInfo.exchangeRate)}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, qty: Double, price: Double, dateMillis: Long) -> Unit
) {
    var isBuy by remember { mutableStateOf(true) }
    var qtyStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("確認")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val dateString = formatter.format(Date(selectedDate))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增交易") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isBuy, onClick = { isBuy = true })
                    Text("買入 (BUY)")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = !isBuy, onClick = { isBuy = false })
                    Text("賣出 (SELL)")
                }
                
                OutlinedTextField(
                    value = dateString,
                    onValueChange = {},
                    label = { Text("交易日期") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Select Date") // Can change icon later
                        }
                    }
                )

                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it },
                    label = { Text("數量") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("價格") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val q = qtyStr.toDoubleOrNull() ?: 0.0
                    val p = priceStr.toDoubleOrNull() ?: 0.0
                    if (q > 0 && p > 0) {
                        onConfirm(if (isBuy) "BUY" else "SELL", q, p, selectedDate)
                    }
                }
            ) {
                Text("確認")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
