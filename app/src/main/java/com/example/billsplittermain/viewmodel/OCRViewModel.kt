package com.example.billsplittermain.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billsplittermain.ocr.ReceiptOcrProcessor
import kotlinx.coroutines.launch

/** Manages OCR processing state. Handles image selection and text recognition. */
class OCRViewModel(private val ocrProcessor: ReceiptOcrProcessor) : ViewModel() {

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    private val _recognizedText = mutableStateOf("")
    val recognizedText: State<String> = _recognizedText

    private val _selectedImageUri = mutableStateOf<Uri?>(null)
    val selectedImageUri: State<Uri?> = _selectedImageUri

    fun processImage(uri: Uri) {
        _selectedImageUri.value = uri
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = ocrProcessor.processImage(uri)
                _recognizedText.value = result.items.joinToString("\n") { "${it.name}: ${it.price}" }
            } catch (e: Exception) {
                _recognizedText.value = "Error: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
