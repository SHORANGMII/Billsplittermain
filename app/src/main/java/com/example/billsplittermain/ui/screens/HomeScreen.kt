package com.example.billsplittermain.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.billsplittermain.data.BillWithItems
import com.example.billsplittermain.ui.BillViewModel
import com.example.billsplittermain.ui.theme.Background
import com.example.billsplittermain.utils.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Updated Main screen for the application providing entry points for scanning and manual entry.
 * Now features real stats, recent bill history, and full currency/offline controls.
 */
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
                            viewModel.toggleForcedOffline() 
                        }) {
                            Icon(
                                imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                                contentDescription = "Offline Mode",
                                tint = if (isOffline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Box {
                            IconButton(onClick = { showCurrencyMenu = true }) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = "Select Currency")
                            }
                            DropdownMenu(
                                expanded = showCurrencyMenu,
                                onDismissRequest = { showCurrencyMenu = false }
                            ) {
                                supportedCurrencies.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text("${currency.symbol} - ${currency.name}") },
                                        onClick = {
                                            viewModel.setSelectedCurrency(currency)
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
                            Icon(Icons.Default.History, contentDescription = "History")
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
                    billHistory = billHistory,
                    selectedCurrency = selectedCurrency,
                    onScanClick = { navController.navigate(Screen.Scan.route) },
                    onManualClick = {
                        viewModel.createNewBill()
                        navController.navigate(Screen.Items.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    billHistory: List<BillWithItems>,
    selectedCurrency: CurrencyInfo,
    onScanClick: () -> Unit,
    onManualClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val recentBills = billHistory.take(3)
    val lastBill = billHistory.firstOrNull()

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
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Bills", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${billHistory.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Latest Amount", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = lastBill?.let { 
                                formatCurrency(convert(it.bill.total, "USD", selectedCurrency.code), selectedCurrency.code) 
                            } ?: "---",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onScanClick,
                modifier = Modifier.weight(1f).height(64.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Scan", style = MaterialTheme.typography.titleMedium)
            }

            OutlinedButton(
                onClick = onManualClick,
                modifier = Modifier.weight(1f).height(64.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Manual", style = MaterialTheme.typography.titleMedium)
            }
        }

        Text(
            text = "Recent History",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start
        )

        if (recentBills.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No recent bills.\nStart by scanning a receipt!",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentBills) { item ->
                    val bill = item.bill
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = bill.name.ifEmpty { "Unnamed Bill" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dateFormatter.format(bill.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatCurrency(convert(bill.total, "USD", selectedCurrency.code), selectedCurrency.code),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Tip: Use the History icon below to see all past bills.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
