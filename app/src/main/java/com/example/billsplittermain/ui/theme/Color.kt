/** Color palette for Bill Splitter OCR dark theme. */
package com.example.billsplittermain.ui.theme

import androidx.compose.ui.graphics.Color

// Background Colors
val Background = Color(0xFF0F172A)
val Surface = Color(0xFF1E293B)
val SurfaceVariant = Color(0xFF334155)

// Primary Colors
val Primary = Color(0xFF6366F1)
val PrimaryDark = Color(0xFF4F46E5)
val PrimaryLight = Color(0xFF818CF8)

// Secondary Colors
val Secondary = Color(0xFF10B981)
val SecondaryDark = Color(0xFF059669)
val Tertiary = Color(0xFFF59E0B)

// Text Colors
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextDisabled = Color(0xFF64748B)

// Status Colors
val Error = Color(0xFFEF4444)
val Success = Color(0xFF22C55E)
val Warning = Color(0xFFF59E0B)

/** Each person in the split gets a distinct color from this list. Colors cycle back to index 0 if there are more than 12 people. */
val PersonColors = listOf(
    Color(0xFF6366F1), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444),
    Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFFEC4899), Color(0xFF84CC16),
    Color(0xFFF97316), Color(0xFF14B8A6), Color(0xFF3B82F6), Color(0xFFD946EF)
)
