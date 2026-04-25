package com.example.studentgigs.view.OnBoarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studentgigs.R
import com.example.studentgigs.ui.theme.AppColors


@Composable
fun CircleIcon(
    icon: ImageVector,
    circleColor: Color,
    floatingOffset: Float,
    modifier: Modifier = Modifier,
    circleSize: Dp = 210.dp,
    iconSize: Dp = 140.dp,
) {
    val isDark = isSystemInDarkTheme()
    val iconColor = if (isDark) AppColors.iconBackgroundDark else AppColors.iconBackgroundLight

    Box(
        modifier = modifier
            .offset(y = floatingOffset.dp)
            .padding(50.dp)
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(color = circleColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun LogoIcon(
    floatingOffset: Float,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp
) {
    val isDark = isSystemInDarkTheme()

    Image(
        painter = painterResource(
            if (isDark) R.drawable.dark_theme_logo_1
            else R.drawable.light_theme_logo_1
        ),
        contentDescription = null,
        modifier = modifier
            .offset(y = floatingOffset.dp)
            .size(size)
            .padding(bottom = 10.dp)
    )
}

// Предопределенные иконки
@Composable
fun ProjectsIcon(floatingOffset: Float) {
    LogoIcon(floatingOffset = floatingOffset)
}




@Composable
fun PortfolioIcon(floatingOffset: Float) {
    CircleIcon(
        icon = Icons.Filled.FavoriteBorder,
        circleColor = AppColors.starYellow,
        floatingOffset = floatingOffset
    )
}

@Composable
fun ConnectionsIcon(floatingOffset: Float) {
    CircleIcon(
        icon = Icons.Outlined.Face,
        circleColor = AppColors.connectionCyan,
        floatingOffset = floatingOffset
    )
}

@Composable
fun CareerIcon(floatingOffset: Float) {
    CircleIcon(
        icon = Icons.Outlined.CheckCircle,
        circleColor = AppColors.rocketPink,
        floatingOffset = floatingOffset
    )
}
