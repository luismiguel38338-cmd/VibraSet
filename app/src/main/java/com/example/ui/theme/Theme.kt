package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AudioConsoleColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CarbonDark,
    primaryContainer = RackSurfaceVariant,
    onPrimaryContainer = NeonCyan,
    secondary = NeonAmber,
    onSecondary = CarbonDark,
    secondaryContainer = RackSurfaceVariant,
    onSecondaryContainer = NeonAmber,
    tertiary = NeonRed,
    onTertiary = CarbonDark,
    background = CarbonDark,
    onBackground = TextBright,
    surface = RackSurface,
    onSurface = TextBright,
    surfaceVariant = RackSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = BezelBorder,
    outlineVariant = Color(0xFF1B2436)
)

@Composable
fun VibraSetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AudioConsoleColorScheme,
        typography = Typography,
        content = content
    )
}
