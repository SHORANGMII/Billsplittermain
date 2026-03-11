package com.example.billsplittermain.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.billsplittermain.data.Bill
import com.example.billsplittermain.data.BillRepository

/** Handles new bill creation. Manages bill title, amount, and image URI states. */
class CreateBillViewModel(private val repository: BillRepository) : ViewModel() {

    var billTitle = mutableStateOf("")
    var totalAmount = mutableStateOf("")
    var imageUri = mutableStateOf<Uri?>(null)

    suspend fun saveBill(): Long {
        val bill = Bill(
            name = billTitle.value,
            total = totalAmount.value.toDoubleOrNull() ?: 0.0,
        )
        return repository.insertBill(bill)
    }
}
