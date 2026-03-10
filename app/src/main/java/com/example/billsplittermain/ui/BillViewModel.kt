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
import com.example.billsplittermain.data.ItemAssignment
import com.example.billsplittermain.data.Person
import com.example.billsplittermain.data.SavedContact
import com.example.billsplittermain.data.SplitResult
import com.example.billsplittermain.utils.CurrencyUtils
import com.example.billsplittermain.utils.getPersonColorHex
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
     * Tracks the global tip amount applied to the bill.
     */
    private val _tipAmount = mutableStateOf(0.0)
    val tipAmount: State<Double> = _tipAmount

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

    /**
     * Internal list of item assignments for the current bill.
     */
    private val _itemAssignments = mutableStateListOf<ItemAssignment>()

    // ==================== BILL MANAGEMENT ====================

    /**
     * Resets all bill-related state to start fresh with a new bill.
     */
    fun createNewBill(name: String = "") {
        _currentBill.value = Bill(name = name)
        _billItems.clear()
        _persons.clear()
        _itemAssignments.clear()
        _splitResults.value = emptyList()
        _taxPercentage.value = 0.0
        _tipPercentage.value = 0.0
        _tipAmount.value = 0.0
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
                    _tipAmount.value = it.bill.tipAmount
                    
                    // Load persons and assignments
                    val personsFromDb = repository.getPersonsForBill(billId)
                    _persons.clear()
                    _persons.addAll(personsFromDb)
                    
                    _itemAssignments.clear()
                    personsFromDb.forEach { person ->
                        _itemAssignments.addAll(repository.getAssignmentsForPerson(person.id))
                    }
                    
                    calculateSplit()
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
        recalculateTotals()
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
        recalculateTotals()
    }

    /**
     * Removes an item from the current bill list by its ID.
     */
    fun removeItem(itemId: Long) {
        _billItems.removeAll { it.id == itemId }
        _itemAssignments.removeAll { it.itemId == itemId }
        recalculateTotals()
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
            recalculateTotals()
        }
    }

    // ==================== PERSON MANAGEMENT ====================

    /**
     * Adds a person to the bill.
     * If [contactId] is provided, increments its usage; otherwise, saves the name as a new contact.
     */
    fun addPerson(name: String, contactId: Long? = null) {
        val tempId = -(System.currentTimeMillis() % 1000000)
        val person = Person(
            id = tempId,
            name = name,
            color = getPersonColorHex(_persons.size)
        )
        _persons.add(person)
        
        viewModelScope.launch {
            if (contactId != null) {
                repository.incrementContactUsage(contactId)
            } else {
                repository.saveContact(name)
            }
        }
        calculateSplit()
    }

    /**
     * Removes a person from the current bill participation.
     */
    fun removePerson(personId: Long) {
        _persons.removeAll { it.id == personId }
        _itemAssignments.removeAll { it.personId == personId }
        calculateSplit()
    }

    /**
     * Toggles the paid status of a person. Updates both local state and database.
     */
    fun togglePersonPaid(personId: Long, isPaid: Boolean) {
        val index = _persons.indexOfFirst { it.id == personId }
        if (index != -1) {
            _persons[index] = _persons[index].copy(isPaid = isPaid)
            
            // Refresh split results locally to show "Paid" checkmarks
            calculateSplit()
            
            if (personId > 0) {
                viewModelScope.launch {
                    repository.updatePersonPaidStatus(personId, isPaid)
                }
            }
        }
    }

    /**
     * Assigns or unassigns an item to a specific person.
     */
    fun toggleItemAssignment(itemId: Long, personId: Long) {
        val existing = _itemAssignments.find { it.itemId == itemId && it.personId == personId }
        if (existing != null) {
            _itemAssignments.remove(existing)
        } else {
            _itemAssignments.add(ItemAssignment(itemId = itemId, personId = personId))
        }
        calculateSplit()
    }

    /**
     * Returns a list of person IDs currently assigned to a specific item.
     */
    fun getAssignedPersonsForItem(itemId: Long): List<Long> {
        return _itemAssignments.filter { it.itemId == itemId }.map { it.personId }
    }

    /**
     * Filters the bill items to return only those assigned to a specific person.
     */
    fun getItemsForPerson(personId: Long): List<BillItem> {
        val itemIds = _itemAssignments.filter { it.personId == personId }.map { it.itemId }
        return _billItems.filter { it.id in itemIds }
    }

    // ==================== SPLIT CALCULATION ====================

    /**
     * Updates the tax percentage and triggers a total recalculation.
     */
    fun setTaxPercentage(percentage: Double) {
        _taxPercentage.value = percentage
        recalculateTotals()
    }

    /**
     * Updates the tip percentage, resets the flat tip amount, and triggers a total recalculation.
     */
    fun setTipPercentage(percentage: Double) {
        _tipPercentage.value = percentage
        _tipAmount.value = 0.0
        recalculateTotals()
    }

    /**
     * Updates the flat tip amount, resets the tip percentage, and triggers a total recalculation.
     */
    fun setTipAmount(amount: Double) {
        _tipAmount.value = amount
        _tipPercentage.value = 0.0
        recalculateTotals()
    }

    /**
     * Updates the current bill's financial totals based on the current items and settings.
     */
    private fun recalculateTotals() {
        val subtotal = _billItems.sumOf { it.totalPrice }
        val taxAmount = subtotal * (_taxPercentage.value / 100.0)
        val tipAmount = if (_tipPercentage.value > 0) {
            subtotal * (_tipPercentage.value / 100.0)
        } else {
            _tipAmount.value
        }
        val total = subtotal + taxAmount + tipAmount

        _currentBill.value = _currentBill.value?.copy(
            subtotal = subtotal,
            taxAmount = taxAmount,
            taxPercentage = _taxPercentage.value,
            tipAmount = tipAmount,
            tipPercentage = _tipPercentage.value,
            total = total
        )
        calculateSplit()
    }

    /**
     * Calculates the bill split for each participant.
     * Formula: personSubtotal + (personSubtotal / subtotal) * (tax + tip)
     */
    fun calculateSplit() {
        val bill = _currentBill.value ?: return
        val subtotal = bill.subtotal
        if (subtotal == 0.0) {
            _splitResults.value = emptyList()
            return
        }

        _splitResults.value = _persons.map { person ->
            val itemsForPerson = getItemsForPerson(person.id)
            val personSubtotal = itemsForPerson.sumOf { item ->
                // Account for shared items
                val splitCount = getAssignedPersonsForItem(item.id).size
                if (splitCount > 0) item.totalPrice / splitCount else 0.0
            }
            
            val shareRatio = personSubtotal / subtotal
            val personTax = bill.taxAmount * shareRatio
            val personTip = bill.tipAmount * shareRatio
            
            SplitResult(
                person = person,
                items = itemsForPerson,
                itemsSubtotal = personSubtotal,
                taxShare = personTax,
                tipShare = personTip,
                total = personSubtotal + personTax + personTip
            )
        }
    }

    /**
     * Resets the split by assigning all current bill items to every person participating.
     */
    fun splitEqually() {
        _itemAssignments.clear()
        _persons.forEach { person ->
            _billItems.forEach { item ->
                _itemAssignments.add(ItemAssignment(itemId = item.id, personId = person.id))
            }
        }
        calculateSplit()
    }

    /**
     * Returns the grand total converted to the user's selected currency.
     */
    fun getConvertedGrandTotal(): Double {
        val total = _currentBill.value?.total ?: 0.0
        return CurrencyUtils.convert(total, "USD", _selectedCurrency.value.code)
    }
}
