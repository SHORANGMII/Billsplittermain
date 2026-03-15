package com.example.billsplittermain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.billsplittermain.navigation.BillSplitterNavHost
import com.example.billsplittermain.ui.BillViewModel
import com.example.billsplittermain.ui.theme.BillSplitterTheme

/**
 * Main entry point of the Bill Splitter application.
 * Manages the high-level navigation and the shared BillViewModel.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: BillViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BillSplitterTheme {
                val navController = rememberNavController()
                BillSplitterNavHost(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
