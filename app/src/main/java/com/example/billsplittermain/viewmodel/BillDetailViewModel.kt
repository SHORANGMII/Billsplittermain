package com.example.billsplittermain.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billsplittermain.data.BillWithPersons
import com.example.billsplittermain.data.BillRepository
import kotlinx.coroutines.launch

/** Manages bill detail view. Calculates total paid/unpaid amounts from persons list. */
class BillDetailViewModel(private val repository: BillRepository) : ViewModel() {

    private val _billWithPersons = mutableStateOf<BillWithPersons?>(null)
    val billWithPersons: State<BillWithPersons?> = _billWithPersons

    private val _totalPaid = mutableStateOf(0.0)
    val totalPaid: State<Double> = _totalPaid

    private val _totalUnpaid = mutableStateOf(0.0)
    val totalUnpaid: State<Double> = _totalUnpaid

    fun loadBillDetails(billId: Long) {
        viewModelScope.launch {
            val result = repository.getBillWithPersons(billId)
            _billWithPersons.value = result
            calculateTotals()
        }
    }

    private fun calculateTotals() {
        val persons = _billWithPersons.value?.persons ?: emptyList()
        _totalPaid.value = persons.filter { it.isPaid }.sumOf { it.amountOwed }
        _totalUnpaid.value = persons.filter { !it.isPaid }.sumOf { it.amountOwed }
    }
}
