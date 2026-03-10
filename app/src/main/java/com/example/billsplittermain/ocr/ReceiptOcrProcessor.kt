package com.example.billsplittermain.ocr

import android.app.Application
import android.net.Uri
import com.example.billsplittermain.data.OcrResult
import com.example.billsplittermain.data.ReceiptItem
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * Processor for extracting bill information from receipt images using Google ML Kit OCR.
 *
 * Strategy:
 * 1. Uses ML Kit to recognize text blocks and lines in the image.
 * 2. Iterates through lines and uses regular expressions to identify items and their prices.
 * 3. Looks for specific keywords (e.g., "tax", "tip", "subtotal") to extract financial totals.
 * 4. Aggregates all detected items and totals into an [OcrResult].
 */
class ReceiptOcrProcessor(private val context: Application) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val lineRegex = Regex("""(.+?)\s+\$?([\d,]+\.?\d{0,2})""")

    /**
     * Processes a receipt image from the given [Uri] and extracts its contents.
     *
     * @param uri The URI of the image to process.
     * @return An [OcrResult] containing the list of items and extracted totals.
     */
    suspend fun processImage(uri: Uri): OcrResult {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            val result = recognizer.process(image).await()

            val items = mutableListOf<ReceiptItem>()
            var subtotal: Double? = null
            var tax: Double? = null
            var tip: Double? = null
            var total: Double? = null

            for (block in result.textBlocks) {
                for (line in block.lines) {
                    val lineText = line.text
                    val textLower = lineText.lowercase()

                    // Check for total keywords BEFORE generic item matching
                    val priceMatch = Regex("""\$?([\d,]+\.?\d{0,2})""").find(lineText)
                    val price = priceMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

                    when {
                        textLower.contains("tax") -> tax = price
                        textLower.contains("tip") || textLower.contains("gratuity") -> tip = price
                        textLower.contains("subtotal") -> subtotal = price
                        textLower.contains("total") -> if (total == null) total = price
                        else -> {
                            // Only if it's not a total line, try to match as a generic item
                            val match = lineRegex.find(lineText)
                            if (match != null) {
                                val name = match.groupValues[1].trim()
                                val itemPrice = match.groupValues[2].replace(",", "").toDoubleOrNull() ?: 0.0
                                items.add(ReceiptItem(name = name, price = itemPrice))
                            }
                        }
                    }
                }
            }

            OcrResult(
                items = items,
                subtotal = subtotal,
                tax = tax,
                tip = tip,
                total = total
            )
        } catch (e: Exception) {
            OcrResult(emptyList(), null, null, null, null)
        }
    }

    /**
     * Closes the underlying ML Kit text recognizer to release resources.
     */
    fun cleanup() {
        recognizer.close()
    }
}
