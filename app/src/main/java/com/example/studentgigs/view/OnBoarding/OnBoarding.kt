package com.example.studentgigs.view.OnBoarding

import android.content.Intent
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.studentgigs.R
import com.example.studentgigs.view.OnRegister.LoginApp
import com.example.studentgigs.view.OnRegister.LoginAppActivity


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


@OptIn(ExperimentalAnimationApi::class)
fun slideFadeIn(fromRight: Boolean) =
    slideInHorizontally(
        initialOffsetX = { if (fromRight) it else -it},
        animationSpec = tween(250)
    ) + fadeIn(
        animationSpec = tween(250)
    )

@OptIn(ExperimentalAnimationApi::class)
fun slideFadeOut(toLeft: Boolean) =
    slideOutHorizontally(
        targetOffsetX = { if (toLeft) -it else it},
        animationSpec = tween(250)
    ) + fadeOut(
        animationSpec = tween(250)
    )


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnBoarding(innerPadding: PaddingValues) {
    val navController = rememberNavController()
    val pages = listOf(Screens.FirstStart.route, Screens.SecondStart.route, Screens.ThirdStart.route,
        Screens.FourthStart.route)

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentIndex = pages.indexOf(currentRoute).coerceAtLeast(0)

    val isDark = isSystemInDarkTheme()

    val context = LocalContext.current

    val gradient = if (isDark) darkGradient else lightGradient



    Box(
        modifier = Modifier.fillMaxSize().background(gradient)
    ) {

        Image(
            painter = painterResource(R.drawable.noise),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.007f)
        )

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
                    .clickable(
                        onClick = {
                            val intent = Intent(context, LoginAppActivity::class.java)
                            context.startActivity(intent)
                        }
                    ),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF89898A)
            )
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .offset(y = (-30).dp),

            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            AnimatedNavHost(
                navController = navController,
                startDestination = pages.first(),
            ) {

                composable(
                    route = Screens.FirstStart.route,
                    enterTransition = { slideFadeIn(fromRight = true) },
                    exitTransition = { slideFadeOut(toLeft = true) },
                    popEnterTransition = { slideFadeIn(fromRight = false)},
                    popExitTransition = { slideFadeOut(toLeft = false) }
                ) {
                    FirstStart(navController)
                }

                composable(
                    route = Screens.SecondStart.route,
                    enterTransition = { slideFadeIn(fromRight = true) },
                    exitTransition = { slideFadeOut(toLeft = true) },
                    popEnterTransition = { slideFadeIn(fromRight = false)},
                    popExitTransition = { slideFadeOut(toLeft = false) }
                ) {
                    SecondStart(navController)
                }

                composable(
                    route = Screens.ThirdStart.route,
                    enterTransition = { slideFadeIn(fromRight = true) },
                    exitTransition = { slideFadeOut(toLeft = true) },
                    popEnterTransition = { slideFadeIn(fromRight = false)},
                    popExitTransition = { slideFadeOut(toLeft = false) }
                ) {
                    ThirdStart(navController)
                }

                composable(
                    route = Screens.FourthStart.route,
                    enterTransition = { slideFadeIn(fromRight = true) },
                    exitTransition = { slideFadeOut(toLeft = true) },
                    popEnterTransition = { slideFadeIn(fromRight = false)},
                    popExitTransition = { slideFadeOut(toLeft = false) }
                ) {
                    FourthStart(navController)
                }
            }

        }

        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)

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

                Button(onClick = {
                    val next = (currentIndex + 1).coerceAtMost(pages.lastIndex)
                    if (next > currentIndex) {
                        navController.navigate(pages[next]) {
                            launchSingleTop = true
                        }
                    } else {
                        val intent = Intent(context, LoginAppActivity::class.java)
                        context.startActivity(intent)
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (currentIndex == pages.lastIndex) "Начать  " else "Далее  ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (currentIndex == pages.lastIndex) Icons.Default.Check else Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next slide"
                    )
                }
            }
        }
    }




}


@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    spacing: Dp = 8.dp,
    activeWidth: Dp = 28.dp,
    activeColor: Color = Color(0xFF57CB60),
    inactiveColor: Color = Color(0xFF89898A),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalDots) {
            val isSelected = i == selectedIndex
            val width by animateDpAsState(targetValue = if (isSelected) activeWidth else dotSize)
            val height = dotSize
            val color by animateColorAsState(targetValue = if (isSelected) activeColor else inactiveColor)

            Box(
                modifier = Modifier
                    .height(height)
                    .width(width)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}


