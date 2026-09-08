package com.example.wasla.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WaslaPrimary,
    onPrimary = WaslaOnPrimary,
    primaryContainer = WaslaSurfaceVariant,
    onPrimaryContainer = WaslaPrimary,
    secondary = WaslaAccent,
    onSecondary = WaslaOnPrimary,
    background = WaslaBackground,
    onBackground = WaslaTextPrimary,
    surface = WaslaSurface,
    onSurface = WaslaTextPrimary,
    surfaceVariant = WaslaSurfaceVariant,
    onSurfaceVariant = WaslaTextSecondary,
    outline = WaslaCardBorder,
    error = WaslaError,
    onError = WaslaOnError
)

@Composable
fun WaslaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
