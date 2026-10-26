package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// BizFlow uses a restrained, high-clarity light theme designed for readability in Indian shop environments
private val BusinessColorScheme = lightColorScheme(
    primary = BizFlowAccent,
    onPrimary = Color.White,
    primaryContainer = BizFlowAccentSoft,
    onPrimaryContainer = BizFlowAccent,
    secondary = BizFlowGrayDark,
    onSecondary = Color.White,
    secondaryContainer = BizFlowBorderSubtle,
    onSecondaryContainer = BizFlowCharcoal,
    tertiary = BizFlowGrayMuted,
    background = BizFlowBackground,
    surface = BizFlowSurface,
    onBackground = BizFlowCharcoal,
    onSurface = BizFlowCharcoal,
    surfaceVariant = BizFlowSurfaceHover,
    onSurfaceVariant = BizFlowGrayDark,
    outline = BizFlowBorder,
    outlineVariant = BizFlowBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = BusinessColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BizFlowSurface.toArgb()
                window.navigationBarColor = BizFlowSurface.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
