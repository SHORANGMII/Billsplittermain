package com.example.billsplittermain.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    error = Error,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = TextPrimary
)

/** Root theme for Bill Splitter OCR. Always dark — no light mode. */
@Composable
fun BillSplitterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
