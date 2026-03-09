package com.example.billsplittermain.ui

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.billsplittermain.data.Bill
import com.example.billsplittermain.data.BillItem
import com.example.billsplittermain.data.Person
import com.example.billsplittermain.data.SplitResult
import com.example.billsplittermain.utils.supportedCurrencies

/**
 * ViewModel for the Bill Splitter application.
 *
 * Features:
 * - OCR scanning of receipts to extract items and totals.
 * - Multi-currency support with real-time conversion.
 * - Accurate split calculation (percentage-based or equal split).
 * - Management of saved contacts for quick bill participation.
 * - Tracking of payment status for each participant.
 * - Offline-first architecture with background synchronization.
 */
class BillViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Tracks the current bill being edited or viewed.
     */
    private val _currentBill = mutableStateOf<Bill?>(null)
    val currentBill: State<Bill?> = _currentBill

    /**
     * List of items associated with the current bill.
     */
    private val _billItems = mutableStateListOf<BillItem>()
    val billItems: List<BillItem> = _billItems

    /**
     * List of persons participating in the current bill.
     */
    private val _persons = mutableStateListOf<Person>()
    val persons: List<Person> = _persons

    /**
     * Indicates whether a long-running process (like OCR or saving) is in progress.
     */
    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    /**
     * Stores any error messages to be displayed to the user.
     */
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    /**
     * Holds the calculated results of the bill split for each person.
     */
    private val _splitResults = mutableStateOf<List<SplitResult>>(emptyList())
    val splitResults: State<List<SplitResult>> = _splitResults

    /**
     * Tracks the global tax percentage applied to the bill.
     */
    private val _taxPercentage = mutableStateOf(0.0)
    val taxPercentage: State<Double> = _taxPercentage

    /**
     * Tracks the global tip percentage applied to the bill.
     */
    private val _tipPercentage = mutableStateOf(0.0)
    val tipPercentage: State<Double> = _tipPercentage

    /**
     * The currently selected currency for the bill.
     */
    private val _selectedCurrency = mutableStateOf(supportedCurrencies.first())
    val selectedCurrency: State<com.example.billsplittermain.utils.CurrencyInfo> = _selectedCurrency

    /**
     * Tracks if the app is forced into offline mode by the user.
     */
    private val _isForcedOffline = mutableStateOf(false)
    val isForcedOffline: State<Boolean> = _isForcedOffline
}
