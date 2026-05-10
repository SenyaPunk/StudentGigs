package com.example.studentgigs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.studentgigs.data.local.SessionManager
import com.example.studentgigs.ui.theme.StudentGigsTheme
import com.example.studentgigs.view.OnApp.MainAppActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем, авторизован ли пользователь
        val sessionManager = SessionManager.getInstance(this)

        if (sessionManager.isLoggedIn()) {
            // Пользователь авторизован - переходим на главный экран
            val intent = Intent(this, MainAppActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Пользователь не авторизован - показываем онбординг
        enableEdgeToEdge()
        setContent {
            StudentGigsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(innerPadding)
                }
            }
        }
    }
}
