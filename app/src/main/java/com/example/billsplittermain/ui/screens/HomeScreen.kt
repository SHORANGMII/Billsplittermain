package com.example.billsplittermain.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.billsplittermain.ui.BillViewModel
import com.example.billsplittermain.ui.theme.Background

/**
 * The main entry point of the application after launch.
 * Displays a summary of recent activity and options to start new bill splits.
 *
 * @param viewModel The shared [BillViewModel] for state management.
 * @param navController Navigation controller for screen transitions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BillViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧾 Bill Splitter") },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Wifi, contentDescription = "Offline Toggle")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = "Currency Selector")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Background
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                }
            }
        },
        containerColor = Background
    ) { innerPadding ->
        HomeContent(
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Main content area for the Home screen.
 * Includes statistics overview, primary actions (Scan/Manual), and recent bills summary.
 */
@Composable
private fun HomeContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Card Placeholder
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Stats will appear here")
            }
        }

        // Scan Receipt Button
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📷 Scan Receipt")
        }

        // Manual Entry Button
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("✏️ Enter Manually")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Bills Placeholder
        Text(
            text = "Recent bills will appear here",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
