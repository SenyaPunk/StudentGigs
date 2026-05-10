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
import com.example.studentgigs.ui.theme.StudentGigsTheme
import com.example.studentgigs.viewmodel.AuthViewModel

class RegisterAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentGigsTheme {
                val authViewModel: AuthViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = LocalContext.current

                    RegisterApp(
                        innerPadding = innerPadding,
                        onStart = {
                            val intent = Intent(context, LoginAppActivity::class.java)
                            context.startActivity(intent)
                            finish()
                        },
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
