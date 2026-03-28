package com.example.kalorientracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    secondary = Sky,
    tertiary = Coral,
    background = Night,
    surface = NightPanel,
    surfaceVariant = OliveDeep,
    onPrimary = Ink,
    onSecondary = Ink,
    onTertiary = WhiteSmoke,
    onBackground = NightText,
    onSurface = NightText,
    onSurfaceVariant = NightText.copy(alpha = 0.82f)
)

private val LightColorScheme = lightColorScheme(
    primary = Olive,
    secondary = Sky,
    tertiary = Coral,
    background = Canvas,
    surface = Paper,
    surfaceVariant = Panel,
    onPrimary = WhiteSmoke,
    onSecondary = Ink,
    onTertiary = WhiteSmoke,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Ink.copy(alpha = 0.74f)
)

@Composable
fun KalorientrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalContext.current
    if (view is Activity) {
        SideEffect {
            val window = view.window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
