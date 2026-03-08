/**
 * Utilities for assigning distinct colors to people in a bill split.
 */
package com.example.billsplittermain.utils

import androidx.compose.ui.graphics.Color
import com.example.billsplittermain.ui.theme.PersonColors

/**
 * Returns a Color for a person at the given index. Cycles back to start after 12 people.
 */
fun getPersonColor(index: Int): Color {
    return PersonColors[index % PersonColors.size]
}

/**
 * Returns an Int representation of the person color for Room storage.
 */
fun getPersonColorHex(index: Int): Int {
    return PersonColors[index % PersonColors.size].hashCode()
}
