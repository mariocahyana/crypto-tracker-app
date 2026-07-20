package com.example.gamestorehb.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Monochrome dark color scheme — no colorful primary/secondary accents.
 * Uses a pure black background with white foreground for a premium trading app aesthetic.
 */
private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = SurfaceVariant,
    onPrimaryContainer = TextPrimary,

    secondary = TextSecondary,
    onSecondary = Black,
    secondaryContainer = OutlineVariant,
    onSecondaryContainer = TextPrimary,

    tertiary = TextTertiary,
    onTertiary = Black,
    tertiaryContainer = AccentGray,
    onTertiaryContainer = TextPrimary,

    background = Background,
    onBackground = TextPrimary,

    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = Outline,
    outlineVariant = OutlineVariant,

    error = Negative,
    onError = White,

    inverseSurface = TextPrimary,
    inverseOnSurface = Background,
    inversePrimary = AccentGray,

    scrim = Black
)

@Composable
fun CryptoPortfolioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
