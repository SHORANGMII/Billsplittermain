package com.example.billsplittermain.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.billsplittermain.Screen
import com.example.billsplittermain.ui.screens.BillListScreen
import com.example.billsplittermain.viewmodel.BillDetailViewModel
import com.example.billsplittermain.viewmodel.BillListViewModel
import com.example.billsplittermain.viewmodel.CreateBillViewModel
import com.example.billsplittermain.viewmodel.OCRViewModel
import com.example.billsplittermain.viewmodel.SplitBillViewModel

/** Main navigation host. Wires all screens with navigation controller and ViewModels. */
@Composable
fun BillSplitterNavHost(
    navController: NavHostController,
    billListViewModel: BillListViewModel,
    createBillViewModel: CreateBillViewModel,
    splitBillViewModel: SplitBillViewModel,
    ocrViewModel: OCRViewModel,
    billDetailViewModel: BillDetailViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.BillList.route
    ) {
        composable(Screen.BillList.route) {
            BillListScreen(navController = navController, viewModel = billListViewModel)
        }
        
        composable(Screen.CreateBill.route) {
        }
        
        composable(
            route = Screen.SplitBill(0).route,
            arguments = listOf(navArgument("billId") { type = NavType.LongType })
        ) { backStackEntry ->
            val billId = backStackEntry.arguments?.getLong("billId") ?: 0L
        }
        
        composable(
            route = Screen.BillDetail(0).route,
            arguments = listOf(navArgument("billId") { type = NavType.LongType })
        ) { backStackEntry ->
            val billId = backStackEntry.arguments?.getLong("billId") ?: 0L
        }
    }
}
