package com.example.studentgigs.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

object AppColors {
    // Gradients
    val darkGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF05070F),
            Color(0xFF162028),
            Color(0xFF0C1221)
        )
    )

    val lightGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE6E9F3),
            Color(0xFFFFFFFF),
            Color(0xFFE0E0E0)
        )
    )

    // OnBoarding colors
    val starYellow = Color(0xFFFFD600)
    val connectionCyan = Color(0xFF00A9B3)
    val rocketPink = Color(0xFFE068D8)

    // backgrounds (dark/light)
    val iconBackgroundDark = Color(0xFF05070F)
    val iconBackgroundLight = Color(0xFFFBFBFC)
}
