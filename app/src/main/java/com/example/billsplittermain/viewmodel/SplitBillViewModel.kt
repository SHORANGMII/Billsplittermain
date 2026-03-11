package com.example.billsplittermain.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billsplittermain.data.BillWithPersons
import com.example.billsplittermain.data.BillRepository
import com.example.billsplittermain.data.Person
import kotlinx.coroutines.launch

/** Manages bill splitting logic. Handles loading bill with persons, adding persons, and toggling paid status. */
class SplitBillViewModel(private val repository: BillRepository) : ViewModel() {

    private val _currentBill = mutableStateOf<BillWithPersons?>(null)
    val currentBill: State<BillWithPersons?> = _currentBill

    private val _persons = mutableStateListOf<Person>()
    val persons: List<Person> = _persons

    fun loadBillWithPersons(billId: Long) {
        viewModelScope.launch {
            val result = repository.getBillWithPersons(billId)
            _currentBill.value = result
            _persons.clear()
            result?.persons?.let { _persons.addAll(it) }
        }
    }

    fun addPerson(name: String, amount: Double, billId: Long) {
        viewModelScope.launch {
            val newPerson = Person(name = name, amountOwed = amount, billId = billId)
            repository.insertPerson(newPerson)
            loadBillWithPersons(billId)
        }
    }

    fun togglePaidStatus(personId: Long, currentStatus: Boolean, billId: Long) {
        viewModelScope.launch {
            repository.updatePersonPaidStatus(personId, !currentStatus)
            loadBillWithPersons(billId)
        }
    }
}
