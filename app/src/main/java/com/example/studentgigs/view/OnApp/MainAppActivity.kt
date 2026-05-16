package com.example.studentgigs.view.OnApp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studentgigs.data.local.SessionManager
import com.example.studentgigs.ui.theme.StudentGigsTheme
import com.example.studentgigs.view.OnRegister.LoginAppActivity
import com.example.studentgigs.viewmodel.ApplicationViewModel
import com.example.studentgigs.viewmodel.AuthViewModel
import com.example.studentgigs.viewmodel.TaskViewModel

class MainAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager.getInstance(this)
        if (!sessionManager.isLoggedIn()) {
            val intent = Intent(this, LoginAppActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            StudentGigsTheme {
                val authViewModel: AuthViewModel = viewModel()
                val taskViewModel: TaskViewModel = viewModel()
                val applicationViewModel: ApplicationViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainApp(
                        innerPadding = innerPadding,
                        authViewModel = authViewModel,
                        taskViewModel = taskViewModel,
                        applicationViewModel = applicationViewModel
                    )
                }
            }
        }
    }
}
