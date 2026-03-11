package com.example.billsplittermain.utils

import com.example.billsplittermain.data.Person
import java.util.Locale

/** Formats a Double as currency string. Example: 25.50.toCurrency() → "$25.50" */
fun Double.toCurrency(): String {
    return String.format(Locale.getDefault(), "$%.2f", this)
}

/** Parses a currency string to Double. Example: "$25.50".toCurrencyDouble() → 25.50 */
fun String.toCurrencyDouble(): Double? {
    return this.replace("$", "").toDoubleOrNull()
}

/** Sums all amountOwed in a list of Person. Example: persons.totalOwed() → 150.0 */
fun List<Person>.totalOwed(): Double {
    return this.sumOf { it.amountOwed }
}
