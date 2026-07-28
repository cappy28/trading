package com.tradingsim.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppColorScheme = darkColorScheme(
    background = BackgroundDark,
    surface = CardDark,
    surfaceVariant = CardDark,
    primary = AccentBlue,
    secondary = ProfitGreen,
    error = LossRed,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onPrimary = BackgroundDark,
    outline = BorderSubtle
)

/** L'application impose le mode sombre en permanence, quel que soit le thème système. */
@Composable
fun TradingSimulatorTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            it.statusBarColor = BackgroundDark.toArgb()
            it.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content
    )
}
