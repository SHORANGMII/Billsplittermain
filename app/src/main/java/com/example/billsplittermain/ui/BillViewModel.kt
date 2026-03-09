package com.example.billsplittermain.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.billsplittermain.BillSplitterApplication
import com.example.billsplittermain.data.Bill
import com.example.billsplittermain.data.BillItem
import com.example.billsplittermain.data.BillRepository
import com.example.billsplittermain.data.BillWithItems
import com.example.billsplittermain.data.Person
import com.example.billsplittermain.data.SavedContact
import com.example.billsplittermain.data.SplitResult
import com.example.billsplittermain.utils.supportedCurrencies
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    private val repository: BillRepository = (application as BillSplitterApplication).repository
    private val sharedPrefs = application.getSharedPreferences("bill_splitter_prefs", Context.MODE_PRIVATE)

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

    /**
     * Live list of all saved bills with their items. Observed by HistoryScreen.
     */
    val billHistory: StateFlow<List<BillWithItems>> = repository.allBillsWithItems
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Live list of saved contacts ordered by usage. Observed by SplitScreen for quick-add chips.
     */
    val savedContacts: StateFlow<List<SavedContact>> = repository.allSavedContacts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ==================== BILL MANAGEMENT ====================

    /**
     * Resets all bill-related state to start fresh with a new bill.
     */
    fun createNewBill(name: String = "") {
        _currentBill.value = Bill(name = name)
        _billItems.clear()
        _persons.clear()
        _splitResults.value = emptyList()
        _taxPercentage.value = 0.0
        _tipPercentage.value = 0.0
    }

    /**
     * Loads a bill and its items from the repository and restores the ViewModel state.
     */
    fun loadBill(billId: Long) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val billWithItems = repository.getBillWithItems(billId)
                billWithItems?.let {
                    _currentBill.value = it.bill
                    _billItems.clear()
                    _billItems.addAll(it.items)
                    _taxPercentage.value = it.bill.taxPercentage
                    _tipPercentage.value = it.bill.tipPercentage
                    // Persons and SplitResults will be handled in subsequent steps
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load bill: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Saves the current bill and its items to the Room database.
     * @return The ID of the saved bill.
     */
    suspend fun saveBill(): Long {
        val bill = _currentBill.value ?: Bill()
        val billId = repository.insertBill(bill)
        val itemsToInsert = _billItems.map { it.copy(billId = billId) }
        repository.insertItems(itemsToInsert)
        return billId
    }

    /**
     * Deletes a specific bill from the repository.
     */
    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }

    /**
     * Adds a new item to the current bill list with a temporary negative ID.
     */
    fun addItem(name: String, price: Double, quantity: Int = 1) {
        val tempId = -(System.currentTimeMillis() % 1000000)
        _billItems.add(
            BillItem(
                id = tempId,
                name = name,
                price = price,
                quantity = quantity,
                totalPrice = price * quantity
            )
        )
    }

    /**
     * Removes an item from the current bill list by its ID.
     */
    fun removeItem(itemId: Long) {
        _billItems.removeAll { it.id == itemId }
    }

    /**
     * Updates an existing item in the current bill list.
     */
    fun updateItem(itemId: Long, name: String, price: Double, quantity: Int) {
        val index = _billItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            _billItems[index] = _billItems[index].copy(
                name = name,
                price = price,
                quantity = quantity,
                totalPrice = price * quantity
            )
        }
    }
}
