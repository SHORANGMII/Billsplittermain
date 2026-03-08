package com.example.billsplittermain.utils

import java.text.DecimalFormat

/**
 * Information about a supported currency.
 */
data class CurrencyInfo(val code: String, val symbol: String, val name: String)

/**
 * List of currencies supported by the application.
 */
val supportedCurrencies = listOf(
    CurrencyInfo("USD", "$", "United States Dollar"),
    CurrencyInfo("EUR", "€", "Euro"),
    CurrencyInfo("GBP", "£", "British Pound"),
    CurrencyInfo("NPR", "रू", "Nepalese Rupee"),
    CurrencyInfo("INR", "₹", "Indian Rupee"),
    CurrencyInfo("JPY", "¥", "Japanese Yen"),
    CurrencyInfo("AUD", "A$", "Australian Dollar"),
    CurrencyInfo("CAD", "C$", "Canadian Dollar")
)

private val rates = mapOf(
    "USD" to 1.0,
    "NPR" to 133.0,
    "EUR" to 0.92,
    "GBP" to 0.79,
    "INR" to 83.0,
    "JPY" to 149.0,
    "AUD" to 1.53,
    "CAD" to 1.36
)

/**
 * Converts an amount from one currency to another using hardcoded exchange rates.
 * USD is used as the base currency for conversions.
 */
fun convert(amount: Double, fromCode: String, toCode: String): Double {
    if (fromCode == toCode) return amount
    val fromRate = rates[fromCode] ?: 1.0
    val toRate = rates[toCode] ?: 1.0
    
    // Convert to USD first
    val amountInUsd = amount / fromRate
    // Convert from USD to target
    return amountInUsd * toRate
}

/**
 * Formats a currency amount with its symbol and two decimal places.
 * Example: "$ 12.50" or "रू 1,250.00"
 */
fun formatCurrency(amount: Double, currencyCode: String): String {
    val symbol = supportedCurrencies.find { it.code == currencyCode }?.symbol ?: ""
    val formatter = DecimalFormat("#,##0.00")
    return "$symbol ${formatter.format(amount)}"
}

/**
 * Rounds a double value to two decimal places.
 */
fun roundToCents(amount: Double): Double {
    return Math.round(amount * 100.0) / 100.0
}
