/**
 * Color palette for the Bill Splitter OCR application.
 */
package com.example.billsplittermain.ui.theme

import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0F172A)
val Surface = Color(0xFF1E293B)
val SurfaceVariant = Color(0xFF334155)

val Primary = Color(0xFF6366F1)      // indigo/purple for main buttons
val PrimaryDark = Color(0xFF4F46E5)  // darker indigo for pressed state
val PrimaryLight = Color(0xFF818CF8) // lighter indigo for highlights
val Secondary = Color(0xFF10B981)    // emerald green for success actions
val SecondaryDark = Color(0xFF059669) // darker emerald for secondary states
val Tertiary = Color(0xFFF59E0B)     // amber accent

// Text Colors
val TextPrimary = Color(0xFFF1F5F9)    // almost white for main text
val TextSecondary = Color(0xFF94A3B8)  // grey for subtitles
val TextDisabled = Color(0xFF64748B)   // dimmed text

// Status Colors
val Error = Color(0xFFEF4444)          // red for errors
val Success = Color(0xFF22C55E)        // bright green for paid/success
val Warning = Color(0xFFF59E0B)        // amber for warnings

/**
 * Each person added to a bill gets a unique color from this list, 
 * cycling back to start if more than 12 people.
 */
val PersonColors = listOf(
    Color(0xFF6366F1), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444),
    Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFFEC4899), Color(0xFF84CC16),
    Color(0xFFF97316), Color(0xFF14B8A6), Color(0xFF3B82F6), Color(0xFFD946EF)
)
