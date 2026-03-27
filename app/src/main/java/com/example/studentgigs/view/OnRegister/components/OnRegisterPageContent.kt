package com.example.studentgigs.view.OnRegister.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RoleOption { STUDENT, EMPLOYER }
@Composable
fun FirstPage() {
    val isDark = isSystemInDarkTheme()
    var selected by rememberSaveable { mutableStateOf<RoleOption?>(null) }

    var accent = MaterialTheme.colorScheme.primary
    var cardBg = if (isDark) Color(0xFF0E111B) else Color.White
    val cardBgSelected = if (isDark) Color(0xFF0d1a17) else Color(0xFFDFEAE4)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SelectableCard(
            title = "Студент",
            subtitle = "Ищу проекты и стажировки",
            icon = { Icon(Icons.Default.School, contentDescription = null) },
            selected = selected == RoleOption.STUDENT,
            onClick = { selected = RoleOption.STUDENT },
            accentColor = accent,
            background = cardBg,
            backgroundSelected = cardBgSelected
        )

        SelectableCard(
            title = "Работодатель",
            subtitle = "Ищу студентов для задач",
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            selected = selected == RoleOption.EMPLOYER,
            onClick = { selected = RoleOption.EMPLOYER },
            accentColor = accent,
            background = cardBg,
            backgroundSelected = cardBgSelected
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { selected?.let { null } },
            enabled = selected != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Text(text = "Продолжить", fontSize = 18.sp)
        }
    }

}


@Composable
fun SecondPage() {

}


@Composable
fun SelectableCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    background: Color,
    backgroundSelected: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val borderColor = if (selected) accentColor else (if (isDark) Color(0xFF1e212b) else MaterialTheme.colorScheme.outline )
    val bg = if (selected) backgroundSelected else background

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bg,
        tonalElevation = if (selected) 6.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface (
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (selected) accentColor else (if (isDark) Color(0xFF1e212b) else MaterialTheme.colorScheme.outline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    icon()
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = if (isDark) Color.White else Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = if (isDark) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurfaceVariant)

            }
        }
    }
}