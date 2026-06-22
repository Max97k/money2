package com.example.money2.presentation.holdings.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var transactionToEdit by remember { mutableStateOf<HoldingTransaction?>(null) }
    val currencyInfo = LocalCurrencyInfo.current

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
            FloatingActionButton(onClick = { 
                transactionToEdit = null
                showDialog = true 
            }) {
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
                val nativeCurrency = if (h.symbol.endsWith(".TW") || h.symbol.endsWith(".TWO")) "TWD" else "USD"
                val totalValue = h.totalQuantity * h.currentPrice
                val totalCost = h.totalQuantity * h.avgCost
                val dayGain = (h.currentPrice - h.previousClosePrice) * h.totalQuantity
                val totalGain = totalValue - totalCost
                val dayGainPct = if (h.previousClosePrice > 0) (h.currentPrice - h.previousClosePrice) / h.previousClosePrice * 100 else 0.0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(h.symbol, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(h.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem("現價", CurrencyFormatter.format(h.currentPrice, currencyInfo.currency, currencyInfo.exchangeRate, nativeCurrency).replace("$ ", "").replace("NT$ ", ""), Modifier.weight(1f))
                            DetailItem("數量", h.totalQuantity.toString(), Modifier.weight(1f))
                            DetailItem("總價值", CurrencyFormatter.format(totalValue, currencyInfo.currency, currencyInfo.exchangeRate, nativeCurrency).replace("$ ", "").replace("NT$ ", ""), Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val dayGainColor = if (dayGain >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            val totalGainColor = if (totalGain >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            
                            val dayGainStr = (if (dayGain >= 0) "+" else "") + CurrencyFormatter.format(dayGain, currencyInfo.currency, currencyInfo.exchangeRate, nativeCurrency).replace("$ ", "").replace("NT$ ", "") + " (${String.format(Locale.US, "%.2f", dayGainPct)}%)"
                            val totalGainStr = (if (totalGain >= 0) "+" else "") + CurrencyFormatter.format(totalGain, currencyInfo.currency, currencyInfo.exchangeRate, nativeCurrency).replace("$ ", "").replace("NT$ ", "")
                            
                            DetailItem("今日損益", dayGainStr, Modifier.weight(1f), dayGainColor)
                            DetailItem("總損益", totalGainStr, Modifier.weight(1f), totalGainColor)
                            DetailItem("平均成本", CurrencyFormatter.format(h.avgCost, currencyInfo.currency, currencyInfo.exchangeRate, nativeCurrency).replace("$ ", "").replace("NT$ ", ""), Modifier.weight(1f))
                        }
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
                    TransactionLotItem(
                        tx = tx,
                        onEdit = { 
                            transactionToEdit = tx
                            showDialog = true 
                        },
                        onDelete = { viewModel.deleteTransaction(tx.id) }
                    )
                }
            }
        }

        if (showDialog) {
            AddTransactionDialog(
                initialTx = transactionToEdit,
                onDismiss = { 
                    showDialog = false 
                    transactionToEdit = null
                },
                onConfirm = { type, qty, price, dateMillis ->
                    if (transactionToEdit != null) {
                        viewModel.updateTransaction(transactionToEdit!!.id, type, qty, price, dateMillis)
                    } else {
                        viewModel.addTransaction(type, qty, price, dateMillis)
                    }
                    showDialog = false
                    transactionToEdit = null
                }
            )
        }
    }
}

@Composable
fun TransactionLotItem(tx: HoldingTransaction, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isBuy = tx.type.name == "BUY"
    val color = if (isBuy) Color(0xFF4CAF50) else Color(0xFFF44336)
    val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = if (isBuy) "買入" else "賣出", color = color, fontWeight = FontWeight.Bold)
                Text(text = formatter.format(Date(tx.dateMillis)), style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(text = "數量: ${tx.quantity}")
                Text(text = "價格: ${CurrencyFormatter.format(tx.price, currencyInfo.currency, currencyInfo.exchangeRate)}")
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    initialTx: HoldingTransaction? = null,
    onDismiss: () -> Unit,
    onConfirm: (type: String, qty: Double, price: Double, dateMillis: Long) -> Unit
) {
    var isBuy by remember(initialTx) { mutableStateOf(initialTx?.type?.name != "SELL") }
    var qtyStr by remember(initialTx) { mutableStateOf(initialTx?.quantity?.toString() ?: "") }
    var priceStr by remember(initialTx) { mutableStateOf(initialTx?.price?.toString() ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialTx?.dateMillis ?: System.currentTimeMillis()
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
    val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    val dateString = formatter.format(Date(selectedDate))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTx != null) "編輯交易" else "新增交易") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isBuy, onClick = { isBuy = true })
                    Text("買入")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = !isBuy, onClick = { isBuy = false })
                    Text("賣出")
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("價格") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
