package com.example.money2.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalUriHandler
import com.example.money2.BuildConfig
import com.example.money2.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {


            val selectedCurrency by viewModel.selectedCurrency.collectAsState()
            val exchangeRate by viewModel.exchangeRate.collectAsState()

            var exchangeRateInput by remember(exchangeRate) { mutableStateOf(exchangeRate.toString()) }

            Text(stringResource(R.string.currency_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedCurrency == "USD",
                        onClick = { viewModel.saveSelectedCurrency("USD") }
                    )
                    Text(stringResource(R.string.usd))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedCurrency == "TWD",
                        onClick = { viewModel.saveSelectedCurrency("TWD") }
                    )
                    Text(stringResource(R.string.twd))
                }
            }

            OutlinedTextField(
                value = exchangeRateInput,
                onValueChange = { exchangeRateInput = it },
                label = { Text(stringResource(R.string.exchange_rate)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    val rate = exchangeRateInput.toFloatOrNull()
                    if (rate != null) {
                        viewModel.saveExchangeRate(rate)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(navController.context.getString(R.string.rate_saved))
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(navController.context.getString(R.string.invalid_rate))
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.save_rate))
            }

            HorizontalDivider()

            Text("關於", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("App 版本", fontWeight = FontWeight.Medium)
                        Text(BuildConfig.VERSION_NAME, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    TextButton(
                        onClick = { uriHandler.openUri("https://github.com/b/money2") }, // Replace with real URL
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("在 GitHub 上查看原始碼")
                    }
                }
            }
        }
    }
}
