package com.securebeam.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = CyberPurple,
    background = ObsidianDark,
    surface = CardSurface,
    surfaceVariant = CardSurfaceBorder,
    onPrimary = ObsidianDark,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = TextPrimary,
    error = StatusError
)

@Composable
fun SecureBeamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
