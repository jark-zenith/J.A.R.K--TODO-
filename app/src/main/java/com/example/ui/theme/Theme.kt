package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = JarkBlue,
    onPrimary = JarkBackground,
    primaryContainer = JarkBlueContainer,
    onPrimaryContainer = JarkBlueLight,
    secondary = JarkBlueLight,
    onSecondary = JarkBackground,
    secondaryContainer = JarkSurfaceVariant,
    onSecondaryContainer = JarkTextPrimary,
    tertiary = JarkGreen,
    onTertiary = JarkBackground,
    tertiaryContainer = JarkGreenContainer,
    onTertiaryContainer = JarkGreen,
    error = JarkRed,
    onError = JarkBackground,
    errorContainer = JarkRedContainer,
    onErrorContainer = JarkRed,
    background = JarkBackground,
    onBackground = JarkTextPrimary,
    surface = JarkSurface,
    onSurface = JarkTextPrimary,
    surfaceVariant = JarkSurfaceVariant,
    onSurfaceVariant = JarkTextSecondary,
    outline = JarkBorder,
    outlineVariant = JarkBorderBright
)

@Composable
fun JarkTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = JarkBackground.toArgb()
            window.navigationBarColor = JarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
