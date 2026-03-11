package com.example.billsplittermain

/** Navigation routes for Bill Splitter app. */
sealed class Screen(val route: String) {
    object BillList : Screen("bill_list")
    object CreateBill : Screen("create_bill")
    data class SplitBill(val billId: Long) : Screen("split_bill/{billId}") {
        fun createRoute(billId: Long) = "split_bill/$billId"
    }
    data class BillDetail(val billId: Long) : Screen("bill_detail/{billId}") {
        fun createRoute(billId: Long) = "bill_detail/$billId"
    }
    
    object Home : Screen("home")
    object Scan : Screen("scan")
    object Items : Screen("items")
    object Split : Screen("split")
    object Result : Screen("result")
    object History : Screen("history")
}
