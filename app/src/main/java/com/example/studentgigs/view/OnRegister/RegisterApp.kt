package com.example.studentgigs.view.OnRegister

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studentgigs.ui.components.DotsIndicator
import com.example.studentgigs.view.OnApp.MainAppActivity
import com.example.studentgigs.view.OnRegister.components.FirstPage
import com.example.studentgigs.view.OnRegister.components.RegisterPageContent
import com.example.studentgigs.view.OnRegister.components.RoleOption
import com.example.studentgigs.view.OnRegister.components.SecondPage

data class RegisterPageInfo(
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
        title = "Почти готово...",
        description = "Осталось заполнить данные для регистрации",
    )
)

@Composable
fun RegisterApp(innerPadding: PaddingValues, onStart: () -> Unit) {
    var currentPageIndex by rememberSaveable { mutableStateOf(0) }
    var selectedRole by rememberSaveable { mutableStateOf<RoleOption?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    if (currentPageIndex > 0) {
                        currentPageIndex = 0
                    } else {
                        onStart()
                    }
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }

            DotsIndicator(
                totalDots = registerPages.size,
                selectedIndex = currentPageIndex,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        AnimatedContent(
            targetState = currentPageIndex,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)))
                        .togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        )
                } else {
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)))
                        .togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        )
                }.using(SizeTransform(clip = false))
            },
            label = "pageTransition"
        ) { pageIndex ->
            when (pageIndex) {
                0 -> {
                    RegisterPageContent(
                        title = registerPages[0].title,
                        description = registerPages[0].description
                    ) {
                        FirstPage(
                            selectedRole = selectedRole,
                            onRoleSelected = { selectedRole = it },
                            onContinue = { currentPageIndex = 1 }
                        )
                    }
                }
                1 -> {
                    RegisterPageContent(
                        title = registerPages[1].title,
                        description = registerPages[1].description
                    ) {
                        val context = LocalContext.current
                        val intent = Intent(context, MainAppActivity::class.java)

                        SecondPage(
                            selectedRole = selectedRole,
                            onApp = { context.startActivity(intent) }
                        )
                    }
                }
            }
        }
    }
}
