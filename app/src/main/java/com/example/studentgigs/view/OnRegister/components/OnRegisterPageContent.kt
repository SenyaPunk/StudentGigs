package com.example.studentgigs.view.OnRegister.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.R
import com.example.studentgigs.view.OnRegister.EmailInput
import com.example.studentgigs.view.OnRegister.PasswordInput

enum class RoleOption { STUDENT, EMPLOYER }


@Composable
fun FirstPage(
    selectedRole: RoleOption?,
    onRoleSelected: (RoleOption) -> Unit,
    onContinue: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
//    var selected by rememberSaveable { mutableStateOf<RoleOption?>(null) }
    var accent = MaterialTheme.colorScheme.primary
    var cardBg = if (isDark) Color(0xFF0E111B) else Color.White
    val cardBgSelected = if (isDark) Color(0xFF0d1a17) else Color(0xFFDFEAE4)

    Column (
        modifier = Modifier
            .fillMaxSize(),
//        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectableCard(
                title = "Студент",
                subtitle = "Ищу проекты и стажировки",
                icon = { Icon(Icons.Default.School, contentDescription = null) },
                selected = selectedRole  == RoleOption.STUDENT,
                onClick = { onRoleSelected(RoleOption.STUDENT)},
                accentColor = accent,
                background = cardBg,
                backgroundSelected = cardBgSelected
            )

            SelectableCard(
                title = "Работодатель",
                subtitle = "Ищу студентов для задач",
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                selected = selectedRole == RoleOption.EMPLOYER,
                onClick = { onRoleSelected(RoleOption.EMPLOYER) },
                accentColor = accent,
                background = cardBg,
                backgroundSelected = cardBgSelected
            )

        }

        Button(
            onClick = /* { selected?.let { null } } */ onContinue,
            enabled = selectedRole != null,
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
fun SecondPage(
    selectedRole: RoleOption?,
    onApp: () -> Unit,

) {
    var accent = MaterialTheme.colorScheme.primary
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var company by remember { mutableStateOf("") }
    var companyPost by remember { mutableStateOf("") }

    var passwordMatch = password == confirmPassword && password.isNotEmpty()



    //Form
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(modifier = Modifier.fillMaxWidth()) {
            if (selectedRole == RoleOption.STUDENT) {
                StudentFields(name, {name = it}, email, {email = it}, password, {password = it}, confirmPassword, {confirmPassword = it})
            } else {
                EmployerFields(name, {name = it}, company, {company = it}, companyPost, {companyPost = it}, email, {email = it}, password, {password = it}, confirmPassword, {confirmPassword = it})
            }

            if (confirmPassword.isNotEmpty() && !passwordMatch) {
                Text("Пароли не совпадают", color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onApp,
                enabled = selectedRole != null,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(text = "Зарегестрироваться", fontSize = 18.sp)
            }



            Surface(
                modifier = Modifier
                    .size(55.dp)
                    .clickable { /* TODO: вход через гугл поставить страницу */ },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
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


@Composable
private fun StudentFields(name: String, onName: (String) -> Unit, email: String, onEmail: (String) -> Unit, pass: String, onPass: (String) -> Unit, conf: String, onConf: (String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text("ФИО", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            DefualtInput(text = name, onTextChange = onName)
        }
        Column {
            Text("Email", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            EmailInput(email = email, onEmailChange = onEmail)
        }
        Column {
            Text("Пароль", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            PasswordInput(password = pass, onPasswordChange = onPass)
        }
        Column {
            Text("Подтвердите пароль", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            PasswordInput(password = conf, onPasswordChange = onConf)
        }
    }

}

@Composable
private fun EmployerFields(name: String, onName: (String) -> Unit, comp: String, onComp: (String) -> Unit, post: String, onPost: (String) -> Unit, email: String, onEmail: (String) -> Unit, pass: String, onPass: (String) -> Unit, conf: String, onConf: (String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Column {
            Text("Название компании", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            DefualtInput(text = comp, onTextChange = onComp, placeholder = "РТУ МИРЭА", imageVector = Icons.Default.Work)
        }
        Column {
            Text("Должность", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            DefualtInput(text = post, onTextChange = onPost, placeholder = "Team Lead", imageVector = Icons.Default.People)
        }
        Column {
            Text("Email", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            EmailInput(email = email, onEmailChange = onEmail)
        }
        Column {
            Text("Пароль", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            PasswordInput(password = pass, onPasswordChange = onPass)
        }
        Column {
            Text("Подтвердите пароль", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            PasswordInput(password = conf, onPasswordChange = onConf)
        }
    }
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


@Composable
fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.outline
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.outline,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    )
}

@Composable
fun EmailInput(
    email: String,
    onEmailChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PillTextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = modifier,
        placeholder = "putin.v.v@edu.mirea.ru",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                modifier = Modifier.size(20.dp)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
fun DefualtInput(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Путин Владимир Владимирович",
    imageVector: ImageVector = Icons.Default.Person
) {
    PillTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = {
            Icon(
                imageVector = imageVector,
                contentDescription = "Name",
                modifier = Modifier.size(20.dp)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
fun PasswordInput(
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    PillTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = modifier,
        placeholder = "********",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Password",
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (visible) "Скрыть пароль" else "Показать пароль"
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
    )
}

