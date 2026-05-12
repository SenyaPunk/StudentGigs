package com.example.studentgigs.view.OnApp.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.User
import com.example.studentgigs.data.model.UserRole
import com.example.studentgigs.data.model.VerificationStatus
import com.example.studentgigs.view.OnApp.TagItem
import com.example.studentgigs.view.OnRegister.LoginAppActivity
import com.example.studentgigs.viewmodel.AuthViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    activeTasksCount: Int,
    onNavigateToNotifications: () -> Unit,
    onNavigateToVerification: () -> Unit = {},
    onNavigateToMyTasks: () -> Unit
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 15.dp)
            .padding(bottom = 96.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        ProfileTopBar()

        Spacer(modifier = Modifier.height(24.dp))
        ProfileHeaderCard(user = currentUser)

        // Секция верификации для работодателей
        if (currentUser?.role == UserRole.EMPLOYER) {
            Spacer(modifier = Modifier.height(24.dp))
            VerificationSection(
                verificationStatus = currentUser.verificationStatus,
                onVerifyClick = onNavigateToVerification
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        if (currentUser?.role == UserRole.STUDENT) {
            SkillsSection()
            Spacer(modifier = Modifier.height(24.dp))
        }

        MenuSection(
            onNotificationClick = onNavigateToNotifications,
            onMyTasksClick = onNavigateToMyTasks,
            activeTasksCount = activeTasksCount,
            isEmployer = currentUser?.role == UserRole.EMPLOYER
        )

        Spacer(modifier = Modifier.height(24.dp))
        ExitButton(
            onLogout = {
                authViewModel.logout()
                val intent = Intent(context, LoginAppActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        )
    }
}

@Composable
fun ProfileTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
        )

        IconButton(
            onClick = { /* Настройки */ },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Настройки",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ProfileHeaderCard(user: User?) {
    val displayName = user?.profileName ?: "Гость"
    val nameParts = displayName.split(" ")
    val formattedName = if (nameParts.size >= 2) {
        "${nameParts[0]}\n${nameParts[1]}"
    } else {
        displayName
    }

    val roleDescription = when (user?.role) {
        UserRole.STUDENT -> "Студент"
        UserRole.EMPLOYER -> user.companyPosition ?: "Работодатель"
        null -> "Не авторизован"
    }

    val emoji = when (user?.role) {
        UserRole.STUDENT -> "👨‍💻"
        UserRole.EMPLOYER -> "💼"
        null -> "👤"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = formattedName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = roleDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (user?.email != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "4.8",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(12 отзывов)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(value = "5", label = "Проекты")
                StatItem(value = "4.8", label = "Отзывы")
                StatItem(value = "12", label = "Отклики")
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Навыки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Редактировать",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { /* Редактировать навыки */ }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val skills = listOf("React", "TypeScript", "Figma", "Python", "SQL")
            skills.forEach { skill ->
                TagItem(text = skill)
            }
        }
    }
}

@Composable
fun VerificationSection(
    verificationStatus: VerificationStatus,
    onVerifyClick: () -> Unit
) {
    when (verificationStatus) {
        VerificationStatus.NOT_VERIFIED -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFFFE0B2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Профиль не подтвержден",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Для публикации заданий пройдите верификацию",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF57C00)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onVerifyClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE65100)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Подтвердить профиль",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        VerificationStatus.PENDING -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFBBDEFB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Проверка документов",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ваша заявка обрабатывается",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }
        }
        VerificationStatus.VERIFIED -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFC8E6C9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Профиль верифицирован",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Вы можете публиковать задания",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF388E3C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuSection(
    onNotificationClick: () -> Unit,
    onMyTasksClick: () -> Unit,
    activeTasksCount: Int,
    isEmployer: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            ProfileMenuItem(
                icon = Icons.Outlined.WorkOutline,
                title = if (isEmployer) "Мои задания" else "Мои проекты",
                badgeCount = if (activeTasksCount > 0) activeTasksCount else null,
                onClick = onMyTasksClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            if (!isEmployer) {
                ProfileMenuItem(icon = Icons.Outlined.Article, title = "Портфолио")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            }

            ProfileMenuItem(icon = Icons.Outlined.Star, title = "Отзывы", badgeCount = 5)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            ProfileMenuItem(icon = Icons.Outlined.Notifications, title = "Уведомления", onClick = onNotificationClick)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    badgeCount: Int? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (badgeCount != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ) {
                Text(
                    text = badgeCount.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Icon(
            imageVector = Icons.Rounded.KeyboardArrowRight,
            contentDescription = "Перейти",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ExitButton(onLogout: () -> Unit) {
    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Выйти из аккаунта",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
