package com.example.billsplittermain.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billsplittermain.data.BillRepository
import com.example.billsplittermain.data.BillWithItems
import kotlinx.coroutines.launch

/** 
 * Manages bill detail view. 
 * Note: This ViewModel is currently kept for architectural compatibility. 
 * Most logic has moved to the shared BillViewModel.
 */
class BillDetailViewModel(private val repository: BillRepository) : ViewModel() {

    private val _billWithItems = mutableStateOf<BillWithItems?>(null)
    val billWithItems: State<BillWithItems?> = _billWithItems

    private val _totalPaid = mutableDoubleStateOf(0.0)
    val totalPaid: State<Double> = _totalPaid

    private val _totalUnpaid = mutableDoubleStateOf(0.0)
    val totalUnpaid: State<Double> = _totalUnpaid

    fun loadBillDetails(billId: Long) {
        viewModelScope.launch {
            val result = repository.getBillWithItems(billId)
            _billWithItems.value = result
            
            result?.let {
                _totalPaid.doubleValue = it.items.sumOf { item -> item.totalPrice }
                _totalUnpaid.doubleValue = 0.0
            }
        }
    }
}
