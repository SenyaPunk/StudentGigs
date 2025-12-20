package com.example.studentgigs.view.OnRegister.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.studentgigs.ui.theme.lightSecondary
import com.example.studentgigs.ui.theme.lightTertiary

private val DarkColorScheme = darkColorScheme(
    primary = darkPrimary,
    secondary = darkSecondary,
    tertiary = darkTertiary,

    // Dark theme
    background = Color(0xFF05070F),
    surface = Color(0xFF0E111B),

    onPrimary = Color(0xFF05070F),
    onSecondary = Color(0xFF89898A),
    onTertiary = Color.White,

    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF89898A),
)

private val LightColorScheme = lightColorScheme(
    primary = lightPrimary,
    secondary = lightSecondary,
    tertiary = lightTertiary,

    background = Color(0xFFF6F7FB),
    surface = Color(0xFFFFFFFF),

    onPrimary = Color.White,
    onSecondary = Color(0xFF6F6F73),
    onTertiary = Color.White,

    onBackground = Color(0xFF0E111B),
    onSurface = Color(0xFF0E111B),
    onSurfaceVariant = Color(0xFF6F6F73),
)

@Composable
fun StudentGigsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}