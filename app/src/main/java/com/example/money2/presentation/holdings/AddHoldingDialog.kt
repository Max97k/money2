package com.example.money2.presentation.holdings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.money2.data.remote.dto.FinnhubSearchResult
import com.example.money2.domain.model.AssetType

import androidx.compose.ui.res.stringResource
import com.example.money2.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHoldingDialog(
    searchResults: List<FinnhubSearchResult>,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (symbol: String, name: String, quantity: Double, avgCost: Double, assetType: AssetType) -> Unit
) {
    var symbol by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("") }
    var avgCostStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_holding_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var searchExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = searchExpanded && searchResults.isNotEmpty(),
                    onExpandedChange = { searchExpanded = it }
                ) {
                    OutlinedTextField(
                        value = symbol,
                        onValueChange = {
                            symbol = it
                            onSearch(it)
                            searchExpanded = true
                        },
                        label = { Text(stringResource(R.string.holding_symbol)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    
                    if (searchResults.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = searchExpanded,
                            onDismissRequest = { searchExpanded = false },
                            modifier = Modifier.heightIn(max = 250.dp)
                        ) {
                            searchResults.forEach { result ->
                                DropdownMenuItem(
                                    text = { Text("${result.displaySymbol} - ${result.description}") },
                                    onClick = {
                                        symbol = result.displaySymbol
                                        name = result.description
                                        searchExpanded = false
                                        onSearch("") // close results
                                    }
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.holding_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text(stringResource(R.string.holding_quantity)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = avgCostStr,
                    onValueChange = { avgCostStr = it },
                    label = { Text(stringResource(R.string.holding_avg_cost)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantity = quantityStr.toDoubleOrNull() ?: 0.0
                    val avgCost = avgCostStr.toDoubleOrNull() ?: 0.0
                    if (symbol.isNotBlank() && quantity > 0) {
                        onConfirm(symbol, name, quantity, avgCost, AssetType.STOCK)
                    }
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
