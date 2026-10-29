package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RetroDarkColorScheme = darkColorScheme(
    primary = DesiToxicGreen,
    onPrimary = DesiStreetNight,
    secondary = DesiChaiGold,
    onSecondary = DesiStreetNight,
    tertiary = DesiBloodRed,
    onTertiary = DesiTextBright,
    background = DesiStreetNight,
    onBackground = DesiTextBright,
    surface = DesiSurfaceDark,
    onSurface = DesiTextBright,
    surfaceVariant = DesiCardBorder,
    onSurfaceVariant = DesiTextDim
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RetroDarkColorScheme,
        typography = Typography,
        content = content
    )
}
