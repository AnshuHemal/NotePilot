package com.white.notepilot.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = LightBlack,
    tertiary = Pink80,
    background = Black,
    surface = LightBlack,
    surfaceVariant = Color(0xFF5A5A5A),
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = White,
    onSecondary = White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = Color(0xFFE0E0E0),
    tertiary = Pink40,
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFFE0E0E0),
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Black,
    onSecondary = Color.Black
)

@Composable
fun NotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context).copy(
                background = Black,
                surface = LightBlack,
                surfaceVariant = Color(0xFF5A5A5A),
                onBackground = White,
                onSurface = White,
                onSurfaceVariant = White,
                secondary = LightBlack,
                onSecondary = White
            ) else dynamicLightColorScheme(context).copy(
                background = Color.White,
                surface = Color(0xFFF5F5F5),
                surfaceVariant = Color(0xFFE0E0E0),
                onBackground = Color.Black,
                onSurface = Color.Black,
                onSurfaceVariant = Color.Black,
                secondary = Color(0xFFE0E0E0),
                onSecondary = Color.Black
            )
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        Surface(
            color = colorScheme.background,
            content = content
        )
    }
}