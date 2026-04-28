package com.example.studentgigs.view.OnRegister.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text

@Composable
fun RegisterPageContent(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(title)

        Text(description)

        content()
    }
}