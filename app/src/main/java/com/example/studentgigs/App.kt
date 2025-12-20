package com.example.studentgigs

import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.studentgigs.view.OnBoarding.OnBoarding
import com.example.studentgigs.view.OnRegister.LoginAppActivity

@Composable
fun App(innerPadding: PaddingValues) {
    val context = LocalContext.current

    OnBoarding(
        innerPadding = innerPadding,
        onFinish = {
            val intent = Intent(context, LoginAppActivity::class.java)
            context.startActivity(intent)
        }
    )
}
