package com.example.billsplittermain.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.billsplittermain.Screen
import com.example.billsplittermain.ui.BillViewModel
import com.example.billsplittermain.ui.theme.Background
import com.example.billsplittermain.utils.supportedCurrencies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BillViewModel,
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onUiReady()
    }

    val isInitialLoading by viewModel.isInitialLoading.collectAsStateWithLifecycle()
    val billHistory by viewModel.billHistory.collectAsStateWithLifecycle()
    val isOffline by viewModel.isForcedOffline
    val selectedCurrency by viewModel.selectedCurrency

    var showCurrencyMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "🧾 Bill Splitter",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            scope.launch(Dispatchers.IO) {
                                viewModel.toggleForcedOffline()
                            }
                        }) {
                            Icon(
                                imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                                contentDescription = null,
                                tint = if (isOffline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        Box {
                            IconButton(onClick = { showCurrencyMenu = true }) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showCurrencyMenu,
                                onDismissRequest = { showCurrencyMenu = false }
                            ) {
                                supportedCurrencies.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text("${currency.symbol} - ${currency.name}") },
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                viewModel.setSelectedCurrency(currency)
                                            }
                                            showCurrencyMenu = false
                                        },
                                        leadingIcon = {
                                            if (selectedCurrency == currency) {
                                                Icon(Icons.Default.Check, contentDescription = null)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
                )
            },
            bottomBar = {
                BottomAppBar(containerColor = Background) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { navController.navigate(Screen.History.route) }) {
                            Icon(Icons.Default.History, contentDescription = null)
                        }
                    }
                }
            },
            containerColor = Background
        ) { innerPadding ->
            if (isInitialLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                HomeContent(
                    modifier = Modifier.padding(innerPadding),
                    billHistoryCount = billHistory.size,
                    onScanClick = { navController.navigate(Screen.Scan.route) },
                    onManualClick = {
                        scope.launch(Dispatchers.IO) {
                            viewModel.createNewBill()
                            withContext(Dispatchers.Main) {
                                navController.navigate(Screen.Items.route)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    billHistoryCount: Int,
    onScanClick: () -> Unit,
    onManualClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (billHistoryCount == 0) "Welcome!" else "Active Splits",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (billHistoryCount == 0)
                        "Scan a receipt to start splitting bills effortlessly."
                        else "You have $billHistoryCount saved splits in your history.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onScanClick,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Scan New Receipt", style = MaterialTheme.typography.titleMedium)
        }

        OutlinedButton(
            onClick = onManualClick,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Enter Items Manually", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Tip: Use the History icon below to see past bills.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
