package com.example.billsplittermain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.navigation.compose.rememberNavController
import com.example.billsplittermain.navigation.BillSplitterNavHost
import com.example.billsplittermain.ui.BillViewModel
import com.example.billsplittermain.ui.theme.BillSplitterTheme
import com.example.billsplittermain.viewmodel.BillDetailViewModel
import com.example.billsplittermain.viewmodel.BillListViewModel
import com.example.billsplittermain.viewmodel.CreateBillViewModel
import com.example.billsplittermain.viewmodel.OCRViewModel
import com.example.billsplittermain.viewmodel.SplitBillViewModel

/**
 * Main entry point of the Bill Splitter application.
 * Manages the high-level navigation and screen-specific ViewModels.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: BillViewModel by viewModels()
    private val billListViewModel: BillListViewModel by viewModels()
    private val createBillViewModel: CreateBillViewModel by viewModels()
    private val splitBillViewModel: SplitBillViewModel by viewModels()
    private val ocrViewModel: OCRViewModel by viewModels()
    private val billDetailViewModel: BillDetailViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Detect window size for potential tablet optimizations
        val windowSizeClass = calculateWindowSizeClass(this)

        setContent {
            BillSplitterTheme {
                val navController = rememberNavController()
                BillSplitterNavHost(
                    navController = navController,
                    billListViewModel = billListViewModel,
                    createBillViewModel = createBillViewModel,
                    splitBillViewModel = splitBillViewModel,
                    ocrViewModel = ocrViewModel,
                    billDetailViewModel = billDetailViewModel
                )
            }
        }
    }
}
