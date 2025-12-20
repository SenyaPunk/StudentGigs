package com.example.studentgigs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.studentgigs.view.OnBoarding.OnBoarding

@Composable
fun App(innerPadding: PaddingValues) {
    OnBoarding(innerPadding)
}