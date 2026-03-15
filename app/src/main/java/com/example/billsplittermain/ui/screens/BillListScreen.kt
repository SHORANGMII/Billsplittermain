package com.example.billsplittermain.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.billsplittermain.Screen
import com.example.billsplittermain.ui.components.BillCard
import com.example.billsplittermain.viewmodel.BillListViewModel

/**
 * Screen displaying a list of all bills.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(
    navController: NavController,
    viewModel: BillListViewModel
) {
    val bills by viewModel.bills.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bills") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.CreateBill.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
            }
        }
    ) { innerPadding ->
        if (bills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No bills yet, tap + to create one")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                items(bills) { billWithItems ->
                    BillCard(
                        bill = billWithItems.bill,
                        onDelete = { viewModel.deleteBill(billWithItems.bill) },
                        onClick = { 
                            navController.navigate(Screen.BillDetail(billWithItems.bill.id).createRoute(billWithItems.bill.id))
                        }
                    )
                }
            }
        }
    }
}
