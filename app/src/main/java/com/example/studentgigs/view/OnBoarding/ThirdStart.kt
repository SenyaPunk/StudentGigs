package com.example.studentgigs.view.OnBoarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
fun UsersIcon(
    modifier: Modifier = Modifier,
    circleSize: Dp = 40.dp,
    starSize: Dp = 22.dp,
    circleColor: Color = Color(0xFF00a9b3),
    starColor: Color = Color(0xFF0F161D),
    floatingOffset: Float
) {
    Box(modifier = modifier.offset(y = floatingOffset.dp).padding(50.dp)) {
        Box(
            modifier = modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(color = circleColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Face,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(starSize)
            )
        }
    }

}

@Composable
fun ThirdStart(navController: NavHostController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isDark = isSystemInDarkTheme()

        val enterTransition = remember {
            MutableTransitionState(false).apply { targetState = true }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "float")

        val floatingOffset by infiniteTransition.animateFloat(
            -16f,
            16f,
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 3000,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "floatingOffset"
        )



        AnimatedVisibility(
            visibleState = enterTransition,
            enter = fadeIn(
                animationSpec = tween(600)
            ) + slideInVertically (
                animationSpec = tween(600),
                initialOffsetY = { it / 3}
            )
        ) {
            UsersIcon(circleSize = 210.dp, starSize = 140.dp, floatingOffset = floatingOffset, starColor = if (isDark) Color(0xFF0F161D) else Color(0xFFFBFBFC)
            )
        }

        Text("Связи в индустрии", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 14.dp))
        Text("Работодатели ищут талантливых студентов прямо здесь", style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            modifier = Modifier.width(300.dp),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = Color(0xFF89898A)
        )
    }
}


