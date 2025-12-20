package com.example.studentgigs.view.OnRegister

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.R

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
    singleLine: Boolean = true,
    isDark: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = 2.dp,
                color = if (isFocused) Color(0xFF57CB60) else Color(0xFF9AA0A6),
                shape = RoundedCornerShape(16.dp)
            ),
        textStyle = TextStyle(fontSize = 15.sp),
        placeholder = { Text(placeholder, color = Color(0xFF9AA0A6)) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.textFieldColors(
            textColor = if (isDark) Color.White else Color.Black,
            cursorColor = Color(0xFF57CB60),
            leadingIconColor = Color(0xFF9AA0A6),
            trailingIconColor = Color(0xFF9AA0A6),
            placeholderColor = Color(0xFF9AA0A6),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            backgroundColor = if (isDark) Color(0xFF171A24) else Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EmailInput(
    email: String,
    onEmailChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
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
        ),
        isDark = isDark
    )
}

@Composable
fun PasswordInput(
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
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
                    contentDescription = if (visible) "Показать пароль" else "Скрыть пароль"
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        isDark = isDark
    )
}

@Composable
fun LoginApp(innerPadding: PaddingValues) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF05070F) else Color(0xFFF5F5F5))
            .padding(innerPadding)
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        LoginContent(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun LoginContent(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Image(
                painter = painterResource(
                    if (isDark) R.drawable.dark_theme_logo_1
                    else R.drawable.light_theme_logo_1
                ),
                contentDescription = "Logo",
                modifier = Modifier
                    .widthIn(max = 180.dp)
                    .height(100.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Добро пожаловать",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Войдите в свой аккаунт",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(0xFF89898A)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Email",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                EmailInput(
                    email = email,
                    onEmailChange = { email = it },
                    isDark = isDark
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Пароль",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                PasswordInput(
                    password = password,
                    onPasswordChange = { password = it },
                    isDark = isDark
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { /* TODO: Implement login logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF57CB60),
                    contentColor = if (isDark) Color(0xFF05070F) else Color(0xFFFFFFFF)
                )
            ) {
                Text(
                    text = "Войти",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    color = Color(0xFF89898A),
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp),
                    thickness = 1.dp
                )

                Text(
                    text = "или",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider(
                    color = Color(0xFF89898A),
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp),
                    thickness = 1.dp
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { /* TODO: Implement Google login */ },
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF1e212b) else Color(0xFFFFFFFF),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.google_icon),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Войти через Google",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Нет аккаунта? ",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(0xFF89898A)
            )

            Text(
                text = "Зарегистрироваться",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(0xFF57CB60),
                modifier = Modifier.clickable { /* TODO: Navigate to registration */ }
            )
        }
    }
}
