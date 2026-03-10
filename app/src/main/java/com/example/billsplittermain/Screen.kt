package com.example.billsplittermain

/**
 * Defines all navigation destinations within the Bill Splitter application.
 * Each screen is associated with a unique route string used by the NavController.
 *
 * @property route The unique identifier for the navigation destination.
 */
sealed class Screen(val route: String) {
    /**
     * The initial home screen displaying recent bills and options to start a new split.
     */
    object Home : Screen("home")

    /**
     * Screen for scanning a receipt image using OCR.
     */
    object Scan : Screen("scan")

    /**
     * Screen for reviewing and editing items extracted from the receipt.
     */
    object Items : Screen("items")

    /**
     * Screen for assigning items to participants.
     */
    object Split : Screen("split")

    /**
     * Final result screen showing what each person owes and handling payments.
     */
    object Result : Screen("result")

    /**
     * Screen showing the history of all completed bill splits.
     */
    object History : Screen("history")
}
