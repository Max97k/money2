package com.example.money2.presentation.holdings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.money2.domain.model.AssetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHoldingDialog(
    onDismiss: () -> Unit,
    onConfirm: (symbol: String, name: String, quantity: Double, avgCost: Double, assetType: AssetType) -> Unit
) {
    var symbol by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("") }
    var avgCostStr by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedAssetType by remember { mutableStateOf(AssetType.STOCK) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增持倉") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it },
                    label = { Text("代號 (Symbol)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名稱 (Name)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("數量 (Quantity)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = avgCostStr,
                    onValueChange = { avgCostStr = it },
                    label = { Text("平均成本 (Avg Cost)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedAssetType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("資產類型 (Asset Type)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        AssetType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedAssetType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantity = quantityStr.toDoubleOrNull() ?: 0.0
                    val avgCost = avgCostStr.toDoubleOrNull() ?: 0.0
                    if (symbol.isNotBlank() && quantity > 0) {
                        onConfirm(symbol, name, quantity, avgCost, selectedAssetType)
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
