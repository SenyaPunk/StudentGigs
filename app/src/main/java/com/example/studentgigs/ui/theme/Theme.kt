package com.example.studentgigs.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF57CB60),
    secondary = Color(0xFF152F1F),
    tertiary = Color(0xFFF14D4C),
    background = Color(0xFF05070F),
    surface = Color(0xFF0E111B),
    surfaceVariant = Color(0xFF171A24),
    onPrimary = Color(0xFF05070F),
    onSecondary = Color(0xFFCCCCCC),
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF89898A),
    outline = Color(0xFF9AA0A6),
    outlineVariant = Color(0xFF3A3D47),
    error = Color(0xFFF14D4C),
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2DBE60),
    secondary = Color(0xFFDFF3E6),
    tertiary = Color(0xFFD84343),
    background = Color(0xFFF6F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0F1F5),
    onPrimary = Color.White,
    onSecondary = Color(0xFF1A5F2E),
    onTertiary = Color.White,
    onBackground = Color(0xFF0E111B),
    onSurface = Color(0xFF0E111B),
    onSurfaceVariant = Color(0xFF6F6F73),
    outline = Color(0xFF9AA0A6),
    outlineVariant = Color(0xFFD9DCE3),
    error = Color(0xFFD84343),
    onError = Color.White,
)

@Composable
fun StudentGigsTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
