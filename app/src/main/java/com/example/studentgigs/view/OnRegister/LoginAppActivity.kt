package com.example.studentgigs.view.OnRegister

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studentgigs.data.local.SessionManager
import com.example.studentgigs.ui.theme.StudentGigsTheme
import com.example.studentgigs.view.OnApp.MainAppActivity
import com.example.studentgigs.viewmodel.AuthViewModel

class LoginAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем, авторизован ли пользователь
        val sessionManager = SessionManager.getInstance(this)
        if (sessionManager.isLoggedIn()) {
            // Переходим на главный экран
            val intent = Intent(this, MainAppActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            StudentGigsTheme {
                val authViewModel: AuthViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = LocalContext.current

                    LoginApp(
                        innerPadding = innerPadding,
                        onFinish = {
                            val intent = Intent(context, RegisterAppActivity::class.java)
                            context.startActivity(intent)
                        },
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
