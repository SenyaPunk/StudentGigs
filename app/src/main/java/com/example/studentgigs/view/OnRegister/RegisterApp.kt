package com.example.studentgigs.view.OnRegister

import android.accessibilityservice.GestureDescription
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studentgigs.view.OnRegister.components.FirstPage
import com.example.studentgigs.view.OnRegister.components.RegisterPageContent
import com.example.studentgigs.view.OnRegister.components.RoleOption
import com.example.studentgigs.view.OnRegister.components.SecondPage
import org.intellij.lang.annotations.JdkConstants

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


data class RegisterPageInfo (
    val route: String,
    val title: String,
    val description: String
)

val registerPages = listOf(
    RegisterPageInfo(
        route = "firstPage",
        title = "Кто вы?",
        description = "Выберите свою роль на платформе",
    ),
    RegisterPageInfo(
        route = "secondPage",
        title = "Создать аккаунт",
        description = "Заполните данные для регистрации",
    )

)

@Composable
fun RegisterApp(innerPadding: PaddingValues, onStart: () -> Unit) {
    // Здесь будут слайды NavHost

    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentIndex = registerPages.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    var selectedRole by rememberSaveable { mutableStateOf<RoleOption?>(null) }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(innerPadding)
    ) {
        Row(
        ) {
            IconButton(
                onClick = {
                    if (currentIndex > 0) {
                        navController.popBackStack()
                    } else {
                        onStart()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }

            DotsIndicator(
                totalDots = registerPages.size,
                selectedIndex = currentIndex,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Column {
            NavHost(
                navController=navController,
                startDestination = registerPages.first().route,
            ) {
                composable(
                    "firstPage",
                    enterTransition = { slideFadeIn(fromRight = true) },
                    exitTransition = { slideFadeOut(toLeft = true) },
                    popEnterTransition = { slideFadeIn(fromRight = false) },
                    popExitTransition = { slideFadeOut(toLeft = false) }
                ) {
                    RegisterPageContent (
                        title = registerPages[0].title,
                        description = registerPages[0].description,
                    ) {
                        FirstPage(
                            selectedRole = selectedRole,
                            onRoleSelected = {selectedRole = it},
                            onContinue = {
                                navController.navigate("secondPage")
                            }
                        )
                    }
                }

                composable(
                    "secondPage",
                    enterTransition = { slideFadeIn(fromRight = true) },
                    exitTransition = { slideFadeOut(toLeft = true) },
                    popEnterTransition = { slideFadeIn(fromRight = false) },
                    popExitTransition = { slideFadeOut(toLeft = false) }
                ) {
                    RegisterPageContent (
                        title = registerPages[1].title,
                        description = registerPages[1].description,
                    ) {
                        SecondPage(
                            selectedRole = selectedRole,
                        )
                    }
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
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { i ->
            val isSelected = i == selectedIndex
            val width by animateDpAsState(
                targetValue = if (isSelected) activeWidth else dotSize,
                label = "dotWidth"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                label = "dotColor"
            )

            Box(
                modifier = Modifier
                    .height(dotSize)
                    .width(width)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}