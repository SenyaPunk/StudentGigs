package com.example.studentgigs.view.OnBoarding

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studentgigs.R
import com.example.studentgigs.ui.components.DotsIndicator
import com.example.studentgigs.ui.theme.AppColors
import com.example.studentgigs.view.OnBoarding.components.CareerIcon
import com.example.studentgigs.view.OnBoarding.components.ConnectionsIcon
import com.example.studentgigs.view.OnBoarding.components.OnBoardingPage
import com.example.studentgigs.view.OnBoarding.components.PortfolioIcon
import com.example.studentgigs.view.OnBoarding.components.ProjectsIcon

data class OnBoardingPageData(
    val route: String,
    val title: String,
    val description: String,
    val icon: @Composable (Float) -> Unit
)

val onBoardingPages = listOf(
    OnBoardingPageData(
        route = "firstStart",
        title = "Реальные проекты",
        description = "Выполняй микро-проекты от реальных компаний и стартапов",
        icon = { ProjectsIcon(it) }
    ),
    OnBoardingPageData(
        route = "secondStart",
        title = "Создай портфолио",
        description = "Каждый выполненный проект - это строчка в твоем резюме",
        icon = { PortfolioIcon(it) }
    ),
    OnBoardingPageData(
        route = "thirdStart",
        title = "Связи в индустрии",
        description = "Работодатели ищут талантливых студентов прямо здесь",
        icon = { ConnectionsIcon(it) }
    ),
    OnBoardingPageData(
        route = "fourthStart",
        title = "Начни карьеру",
        description = "От первого проекта до стажировки мечты - один шаг",
        icon = { CareerIcon(it) }
    )
)

private fun slideFadeIn(fromRight: Boolean) =
    slideInHorizontally(
        initialOffsetX = { if (fromRight) it else -it },
        animationSpec = tween(250)
    ) + fadeIn(animationSpec = tween(250))

private fun slideFadeOut(toLeft: Boolean) =
    slideOutHorizontally(
        targetOffsetX = { if (toLeft) -it else it },
        animationSpec = tween(250)
    ) + fadeOut(animationSpec = tween(250))

@Composable
fun OnBoarding(
    innerPadding: PaddingValues,
    onFinish: () -> Unit
) {
    val navController = rememberNavController()
    val pages = onBoardingPages

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentIndex = pages.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val isDark = isSystemInDarkTheme()
    val gradient = if (isDark) AppColors.darkGradient else AppColors.lightGradient

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        // Noise overlay
        Image(
            painter = painterResource(R.drawable.noise),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.007f)
        )

        // Skip button
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            Text(
                text = "Пропустить",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
                    .clickable(onClick = onFinish),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .offset(y = (-30).dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavHost(
                navController = navController,
                startDestination = pages.first().route,
            ) {
                pages.forEach { pageData ->
                    composable(
                        route = pageData.route,
                        enterTransition = { slideFadeIn(fromRight = true) },
                        exitTransition = { slideFadeOut(toLeft = true) },
                        popEnterTransition = { slideFadeIn(fromRight = false) },
                        popExitTransition = { slideFadeOut(toLeft = false) }
                    ) {
                        OnBoardingPage(
                            title = pageData.title,
                            description = pageData.description,
                            icon = pageData.icon
                        )
                    }
                }
            }
        }

        // Bottom controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DotsIndicator(
                    totalDots = pages.size,
                    selectedIndex = currentIndex,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Button(
                    onClick = {
                        val next = (currentIndex + 1).coerceAtMost(pages.lastIndex)
                        if (next > currentIndex) {
                            navController.navigate(pages[next].route) {
                                launchSingleTop = true
                            }
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    val isLast = currentIndex == pages.lastIndex
                    Text(
                        text = if (isLast) "Начать  " else "Далее  ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isLast) Icons.Default.Check else Icons.Default.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
