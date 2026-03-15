package com.example.billsplittermain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billsplittermain.data.Bill
import com.example.billsplittermain.data.BillRepository
import com.example.billsplittermain.data.BillWithItems
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Manages the list of all bills. Provides bills StateFlow and delete functionality. */
class BillListViewModel(private val repository: BillRepository) : ViewModel() {

    val bills: StateFlow<List<BillWithItems>> = repository.allBillsWithItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }
}
