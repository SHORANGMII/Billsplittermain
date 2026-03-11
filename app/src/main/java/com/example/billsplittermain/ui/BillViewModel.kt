package com.example.billsplittermain.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.billsplittermain.BillSplitterApplication
import com.example.billsplittermain.data.*
import com.example.billsplittermain.ocr.ReceiptOcrProcessor
import com.example.billsplittermain.utils.CurrencyInfo
import com.example.billsplittermain.utils.convert
import com.example.billsplittermain.utils.getPersonColorHex
import com.example.billsplittermain.utils.supportedCurrencies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BillRepository by lazy {
        (application as BillSplitterApplication).repository
    }

    private val ocrProcessor: ReceiptOcrProcessor by lazy {
        ReceiptOcrProcessor(application)
    }

    private val _currentBill = mutableStateOf<Bill?>(null)
    val currentBill: State<Bill?> = _currentBill

    private val _billItems = mutableStateListOf<BillItem>()
    val billItems: List<BillItem> = _billItems

    private val _persons = mutableStateListOf<Person>()
    val persons: List<Person> = _persons

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _splitResults = mutableStateOf<List<SplitResult>>(emptyList())
    val splitResults: State<List<SplitResult>> = _splitResults

    private val _selectedCurrency = mutableStateOf(supportedCurrencies.first())
    val selectedCurrency: State<CurrencyInfo> = _selectedCurrency

    private val _isForcedOffline = mutableStateOf(false)
    val isForcedOffline: State<Boolean> = _isForcedOffline

    private val _billHistory = MutableStateFlow<List<BillWithItems>>(emptyList())
    val billHistory: StateFlow<List<BillWithItems>> = _billHistory.asStateFlow()

    private val _savedContacts = MutableStateFlow<List<SavedContact>>(emptyList())
    val savedContacts: StateFlow<List<SavedContact>> = _savedContacts.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _itemAssignments = mutableStateListOf<ItemAssignment>()
    private var isInitialized = false

    fun onUiReady() {
        if (isInitialized) return
        isInitialized = true

        viewModelScope.launch(Dispatchers.IO) {
            launch {
                repository.allBillsWithItems.collectLatest { history ->
                    _billHistory.value = history
                    _isInitialLoading.value = false
                }
            }

            val prefs = getApplication<Application>().getSharedPreferences("bill_splitter_prefs", Context.MODE_PRIVATE)
            val offlineMode = prefs.getBoolean("forced_offline", false)

            withContext(Dispatchers.Main) {
                _isForcedOffline.value = offlineMode
            }
        }
    }

    fun toggleForcedOffline() {
        val newValue = !_isForcedOffline.value
        _isForcedOffline.value = newValue
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = getApplication<Application>().getSharedPreferences("bill_splitter_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("forced_offline", newValue).apply()
        }
    }

    fun setSelectedCurrency(currency: CurrencyInfo) {
        _selectedCurrency.value = currency
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun createNewBill(name: String = "") {
        _currentBill.value = Bill(name = name)
        _billItems.clear()
        _persons.clear()
        _itemAssignments.clear()
        _splitResults.value = emptyList()
    }

    fun loadBill(billId: Long) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val billWithItems = withContext(Dispatchers.IO) {
                    repository.getBillWithItems(billId)
                }
                billWithItems?.let {
                    _currentBill.value = it.bill
                    _billItems.clear()
                    _billItems.addAll(it.items)

                    val personsFromDb = withContext(Dispatchers.IO) {
                        repository.getPersonsForBill(billId)
                    }
                    _persons.clear()
                    _persons.addAll(personsFromDb)

                    _itemAssignments.clear()
                    withContext(Dispatchers.IO) {
                        personsFromDb.forEach { person ->
                            _itemAssignments.addAll(repository.getAssignmentsForPerson(person.id))
                        }
                    }

                    calculateSplit()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun processReceipt(uri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    ocrProcessor.processImage(uri)
                }

                createNewBill("Receipt ${System.currentTimeMillis()}")
                result.items.forEach {
                    addItem(it.name, it.price, it.quantity.toInt())
                }

                _currentBill.value = _currentBill.value?.copy(
                    taxAmount = result.tax ?: 0.0,
                    tipAmount = result.tip ?: 0.0,
                    subtotal = result.subtotal ?: result.items.sumOf { it.price * it.quantity },
                    total = result.total ?: 0.0
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process receipt: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    suspend fun saveBill(): Long {
        return withContext(Dispatchers.IO) {
            recalculateTotals()
            val bill = _currentBill.value ?: Bill()
            val billId = repository.insertBill(bill)

            val itemMap = mutableMapOf<Long, Long>()
            val itemsToInsert = _billItems.map { it.copy(id = 0, billId = billId) }
            val insertedItemIds = repository.insertItems(itemsToInsert)
            _billItems.forEachIndexed { index, item -> itemMap[item.id] = insertedItemIds[index] }

            val personMap = mutableMapOf<Long, Long>()
            val insertedPersonIds = _persons.map { repository.insertPerson(it.copy(id = 0, billId = billId)) }
            _persons.forEachIndexed { index, person -> personMap[person.id] = insertedPersonIds[index] }

            val assignmentsToInsert = _itemAssignments.map {
                it.copy(id = 0, itemId = itemMap[it.itemId]!!, personId = personMap[it.personId]!!)
            }
            repository.insertAssignments(assignmentsToInsert)

            billId
        }
    }

    fun addItem(name: String, price: Double, quantity: Int = 1) {
        val tempId = System.nanoTime()
        _billItems.add(BillItem(id = tempId, billId = 0, name = name, price = price, quantity = quantity, totalPrice = price * quantity))
        recalculateTotals()
    }

    private fun recalculateTotals() {
        val subtotal = _billItems.sumOf { it.totalPrice }
        _currentBill.value = _currentBill.value?.copy(subtotal = subtotal, total = subtotal)
        calculateSplit()
    }

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
                val splitCount = getAssignedPersonsForItem(item.id).size
                if (splitCount > 0) item.totalPrice / splitCount else 0.0
            }
            SplitResult(person, itemsForPerson, personSubtotal, 0.0, 0.0, personSubtotal)
        }
    }

    private fun getAssignedPersonsForItem(itemId: Long): List<Long> {
        return _itemAssignments.filter { it.itemId == itemId }.map { it.personId }
    }

    private fun getItemsForPerson(personId: Long): List<BillItem> {
        val itemIds = _itemAssignments.filter { it.personId == personId }.map { it.itemId }
        return _billItems.filter { it.id in itemIds }
    }

    override fun onCleared() {
        super.onCleared()
        ocrProcessor.cleanup()
    }
}
