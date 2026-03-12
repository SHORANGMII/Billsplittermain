package com.example.billsplittermain.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.billsplittermain.Screen
import com.example.billsplittermain.data.BillWithItems
import com.example.billsplittermain.ui.BillViewModel
import com.example.billsplittermain.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen displaying the history of all saved bill splits.
 * Allows viewing details or deleting past entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: BillViewModel,
    navController: NavController
) {
    val billHistory by viewModel.billHistory.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (billHistory.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(billHistory) { billWithItems ->
                    HistoryBillCard(
                        billWithItems = billWithItems,
                        currencyCode = selectedCurrency.code,
                        onDelete = { viewModel.deleteBill(billWithItems.bill) },
                        onClick = {
                            viewModel.loadBill(billWithItems.bill.id)
                            navController.navigate(Screen.Split.route)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Centered empty state shown when no bill history is available.
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🧾", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No bills saved yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Scan a receipt to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Card displaying a summary of a past bill split.
 */
@Composable
private fun HistoryBillCard(
    billWithItems: BillWithItems,
    currencyCode: String,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val bill = billWithItems.bill
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${billWithItems.items.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = formatCurrency(bill.total, currencyCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
