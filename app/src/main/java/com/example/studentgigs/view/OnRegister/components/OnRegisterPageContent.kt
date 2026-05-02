package com.example.studentgigs.view.OnRegister.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.R
import com.example.studentgigs.ui.components.DefaultInput
import com.example.studentgigs.ui.components.EmailInput
import com.example.studentgigs.ui.components.PasswordInput

enum class RoleOption { STUDENT, EMPLOYER }

@Composable
fun FirstPage(
    selectedRole: RoleOption?,
    onRoleSelected: (RoleOption) -> Unit,
    onContinue: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val accent = MaterialTheme.colorScheme.primary
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBgSelected = MaterialTheme.colorScheme.secondary

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectableCard(
                title = "Студент",
                subtitle = "Ищу проекты и стажировки",
                icon = { Icon(Icons.Default.School, contentDescription = null, tint = it) },
                selected = selectedRole == RoleOption.STUDENT,
                onClick = { onRoleSelected(RoleOption.STUDENT) },
                accentColor = accent,
                background = cardBg,
                backgroundSelected = cardBgSelected
            )

            SelectableCard(
                title = "Работодатель",
                subtitle = "Ищу студентов для задач",
                icon = { Icon(Icons.Default.Person, contentDescription = null, tint = it) },
                selected = selectedRole == RoleOption.EMPLOYER,
                onClick = { onRoleSelected(RoleOption.EMPLOYER) },
                accentColor = accent,
                background = cardBg,
                backgroundSelected = cardBgSelected
            )
        }

        Button(
            onClick = onContinue,
            enabled = selectedRole != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = "Продолжить", fontSize = 18.sp)
        }
    }
}

@Composable
fun SecondPage(
    selectedRole: RoleOption?,
    onApp: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary

    // Form state - в будущем переместить в ViewModel
    val formState = rememberRegisterFormState()
    val passwordMatch = formState.password == formState.confirmPassword && formState.password.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (selectedRole == RoleOption.STUDENT) {
                StudentFields(formState)
            } else {
                EmployerFields(formState)
            }

            if (formState.confirmPassword.isNotEmpty() && !passwordMatch) {
                Text(
                    "Пароли не совпадают",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onApp,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Зарегистрироваться", fontSize = 18.sp)
            }

            Surface(
                modifier = Modifier
                    .size(55.dp)
                    .clickable { /* TODO: Google sign in */ },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.google_icon),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// Состояние формы регистрации - временно здесь, потом переместить в ViewModel
class RegisterFormState {
    var name: String = ""
    var email: String = ""
    var password: String = ""
    var confirmPassword: String = ""
    var company: String = ""
    var companyPost: String = ""
}

@Composable
fun rememberRegisterFormState(): RegisterFormState {
    return remember { RegisterFormState() }.also { state ->
        val nameState = remember { mutableStateOf(state.name) }
        val emailState = remember { mutableStateOf(state.email) }
        val passwordState = remember { mutableStateOf(state.password) }
        val confirmPasswordState = remember { mutableStateOf(state.confirmPassword) }
        val companyState = remember { mutableStateOf(state.company) }
        val companyPostState = remember { mutableStateOf(state.companyPost) }

        state.name = nameState.value
        state.email = emailState.value
        state.password = passwordState.value
        state.confirmPassword = confirmPasswordState.value
        state.company = companyState.value
        state.companyPost = companyPostState.value
    }
}

@Composable
private fun StudentFields(formState: RegisterFormState) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Синхронизация с formState
    formState.name = name
    formState.email = email
    formState.password = password
    formState.confirmPassword = confirmPassword

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FormField("ФИО") {
            DefaultInput(
                text = name,
                onTextChange = { name = it },
                placeholder = "Путин Владимир Владимирович"
            )
        }
        FormField("Email") {
            EmailInput(
                email = email,
                onEmailChange = { email = it },
                placeholder = "putin.v.v@edu.mirea.ru"
            )
        }
        FormField("Пароль") {
            PasswordInput(password = password, onPasswordChange = { password = it })
        }
        FormField("Подтвердите пароль") {
            PasswordInput(password = confirmPassword, onPasswordChange = { confirmPassword = it })
        }
    }
}

@Composable
private fun EmployerFields(formState: RegisterFormState) {
    var company by remember { mutableStateOf("") }
    var companyPost by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Синхронизация с formState
    formState.company = company
    formState.companyPost = companyPost
    formState.email = email
    formState.password = password
    formState.confirmPassword = confirmPassword

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FormField("Название компании") {
            DefaultInput(
                text = company,
                onTextChange = { company = it },
                placeholder = "РТУ МИРЭА",
                imageVector = Icons.Default.Work
            )
        }
        FormField("Должность") {
            DefaultInput(
                text = companyPost,
                onTextChange = { companyPost = it },
                placeholder = "Team Lead",
                imageVector = Icons.Default.People
            )
        }
        FormField("Email") {
            EmailInput(
                email = email,
                onEmailChange = { email = it },
                placeholder = "hr@company.ru"
            )
        }
        FormField("Пароль") {
            PasswordInput(password = password, onPasswordChange = { password = it })
        }
        FormField("Подтвердите пароль") {
            PasswordInput(password = confirmPassword, onPasswordChange = { confirmPassword = it })
        }
    }
}

@Composable
private fun FormField(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}

@Composable
fun SelectableCard(
    title: String,
    subtitle: String,
    icon: @Composable (Color) -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    background: Color,
    backgroundSelected: Color,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) accentColor else MaterialTheme.colorScheme.outline
    val bg = if (selected) backgroundSelected else background
    val iconColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
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
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (selected) accentColor else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    icon(iconColor)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
