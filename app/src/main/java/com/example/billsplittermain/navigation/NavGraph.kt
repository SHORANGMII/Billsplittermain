package com.example.billsplittermain.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.billsplittermain.BillSplitterApplication
import com.example.billsplittermain.Screen
import com.example.billsplittermain.ui.BillViewModel
import com.example.billsplittermain.ui.screens.HomeScreen
import com.example.billsplittermain.ui.screens.ScanScreen
import com.example.billsplittermain.ui.screens.ItemsScreen
import com.example.billsplittermain.ui.screens.SplitScreen
import com.example.billsplittermain.ui.screens.ResultScreen
import com.example.billsplittermain.ui.screens.HistoryScreen
import com.example.billsplittermain.ui.screens.BillListScreen
import com.example.billsplittermain.viewmodel.BillListViewModel

/** Main navigation host. Wires all screens with navigation controller and ViewModels. */
@Composable
fun BillSplitterNavHost(
    navController: NavHostController,
    viewModel: BillViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        
        composable(Screen.Scan.route) {
            ScanScreen(viewModel = viewModel, navController = navController)
        }
        
        composable(Screen.Items.route) {
            ItemsScreen(viewModel = viewModel, navController = navController)
        }
        
        composable(Screen.Split.route) {
            SplitScreen(viewModel = viewModel, navController = navController)
        }
        
        composable(Screen.Result.route) {
            ResultScreen(viewModel = viewModel, navController = navController)
        }
        
        composable(Screen.History.route) {
            HistoryScreen(viewModel = viewModel, navController = navController)
        }

        composable(Screen.BillList.route) {
            BillListScreen(
                navController = navController,
                viewModel = BillListViewModel(
                    (LocalContext.current.applicationContext as BillSplitterApplication).repository
                )
            )
        }
    }
}
