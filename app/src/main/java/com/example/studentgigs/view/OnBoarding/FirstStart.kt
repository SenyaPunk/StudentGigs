package com.example.studentgigs.view.OnBoarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.studentgigs.R

@Composable
fun FirstStart(navController: NavHostController) {
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
            Image(
                painter = painterResource(if (isDark) R.drawable.dark_theme_logo_1 else R.drawable.light_theme_logo_1),
                null,
                modifier = Modifier.offset(y = floatingOffset.dp).size(250.dp).padding(bottom = 10.dp)
            )

        }


        Text("Реальные проекты", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 14.dp))
        Text(
            "Выполняй микро-проекты от реальных компаний и стартапов",
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            modifier = Modifier.width(300.dp),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = Color(0xFF89898A)
        )

    }

}


