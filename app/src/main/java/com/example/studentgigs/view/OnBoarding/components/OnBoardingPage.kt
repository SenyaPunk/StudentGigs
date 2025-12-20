package com.example.studentgigs.view.OnBoarding.components

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun OnBoardingPage(
    title: String,
    description: String,
    icon: @Composable (floatingOffset: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val enterTransition = remember {
            MutableTransitionState(false).apply { targetState = true }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "float")

        val floatingOffset by infiniteTransition.animateFloat(
            initialValue = -16f,
            targetValue = 16f,
            animationSpec = infiniteRepeatable(
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
            ) + slideInVertically(
                animationSpec = tween(600),
                initialOffsetY = { it / 3 }
            )
        ) {
            icon(floatingOffset)
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            modifier = Modifier.width(300.dp),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
