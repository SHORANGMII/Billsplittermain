package com.example.billsplittermain.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.billsplittermain.Screen
import com.example.billsplittermain.data.BillItem
import com.example.billsplittermain.ui.BillViewModel
import com.example.billsplittermain.utils.formatCurrency

/**
 * Screen for editing bill items, including adding, updating, and removing entries.
 * Also allows configuring tax and tip percentages.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    viewModel: BillViewModel,
    navController: NavController
) {
    val billItems = viewModel.billItems
    val currentBill by viewModel.currentBill
    val selectedCurrency by viewModel.selectedCurrency
    
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<BillItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Items") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { navController.navigate(Screen.Split.route) }) {
                        Text("⚖️ Split", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddItemDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ==================== ITEMS LIST ====================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(billItems) { item ->
                    ItemRow(
                        item = item,
                        onEdit = { editingItem = item },
                        onDelete = { viewModel.removeItem(item.id) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TaxTipCard(
                        taxPercentage = currentBill?.taxPercentage ?: 0.0,
                        tipPercentage = currentBill?.tipPercentage ?: 0.0,
                        onTaxChange = { viewModel.setTaxPercentage(it) },
                        onTipChange = { viewModel.setTipPercentage(it) }
                    )
                }
            }

            // ==================== FOOTER ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val subtotal = currentBill?.subtotal ?: 0.0
                    val tax = currentBill?.taxAmount ?: 0.0
                    val tip = currentBill?.tipAmount ?: 0.0
                    val total = currentBill?.total ?: 0.0

                    FooterRow("Subtotal", subtotal, selectedCurrency.code)
                    FooterRow("Tax", tax, selectedCurrency.code)
                    FooterRow("Tip", tip, selectedCurrency.code)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    FooterRow("TOTAL", total, selectedCurrency.code, isBold = true)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { navController.navigate(Screen.Split.route) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Proceed to Split →")
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, price, quantity ->
                viewModel.addItem(name, price, quantity)
                showAddItemDialog = false
            }
        )
    }

    editingItem?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { name, price, quantity ->
                viewModel.updateItem(item.id, name, price, quantity)
                editingItem = null
            }
        )
    }
}

/** Displays a single line item with its price and quantity. */
@Composable
private fun ItemRow(
    item: BillItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "$${String.format("%.2f", item.price)} × ${item.quantity} = $${String.format("%.2f", item.totalPrice)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** Card for adjusting tax and tip percentages. */
@Composable
private fun TaxTipCard(
    taxPercentage: Double,
    tipPercentage: Double,
    onTaxChange: (Double) -> Unit,
    onTipChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Adjust Tax & Tip", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Tax: ${taxPercentage.toInt()}%")
            Slider(
                value = taxPercentage.toFloat(),
                onValueChange = { onTaxChange(it.toDouble()) },
                valueRange = 0f..30f
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Tip: ${tipPercentage.toInt()}%")
            Slider(
                value = tipPercentage.toFloat(),
                onValueChange = { onTipChange(it.toDouble()) },
                valueRange = 0f..40f
            )
        }
    }
}

/** A simple row for displaying totals in the footer. */
@Composable
private fun FooterRow(label: String, amount: Double, currencyCode: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = formatCurrency(amount, currencyCode),
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium
        )
    }
}

/** Dialog for adding a new item to the bill. */
@Composable
private fun AddItemDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                TextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, price.toDoubleOrNull() ?: 0.0, quantity.toIntOrNull() ?: 1) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/** Dialog for editing an existing item. */
@Composable
private fun EditItemDialog(
    item: BillItem,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.price.toString()) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                TextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(
                    name, 
                    price.toDoubleOrNull() ?: 0.0, 
                    quantity.toIntOrNull() ?: 1
                ) 
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
