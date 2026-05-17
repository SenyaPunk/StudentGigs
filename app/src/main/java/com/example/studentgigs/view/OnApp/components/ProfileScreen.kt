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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.studentgigs.data.model.Review
import com.example.studentgigs.data.model.User
import com.example.studentgigs.data.model.UserRole
import com.example.studentgigs.data.model.VerificationStatus
import com.example.studentgigs.view.OnApp.TagItem
import com.example.studentgigs.view.OnRegister.LoginAppActivity
import com.example.studentgigs.viewmodel.AuthViewModel
import com.example.studentgigs.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    reviewViewModel: ReviewViewModel,
    activeTasksCount: Int,
    onNavigateToNotifications: () -> Unit,
    onNavigateToVerification: () -> Unit = {},
    onNavigateToMyTasks: () -> Unit,
    onNavigateToReviews: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()
    val reviewUiState by reviewViewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser

    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { reviewViewModel.loadReviews(it) }
    }

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
        ProfileHeaderCard(
            user = currentUser,
            averageRating = reviewUiState.averageRating,
            reviewCount = reviewUiState.totalCount
        )

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
            onReviewsClick = onNavigateToReviews,
            activeTasksCount = activeTasksCount,
            reviewCount = reviewUiState.totalCount,
            isEmployer = currentUser?.role == UserRole.EMPLOYER
        )

        if (reviewUiState.reviews.isNotEmpty() || reviewUiState.isLoadingReviews) {
            Spacer(modifier = Modifier.height(24.dp))
            ReviewsSection(
                reviews = reviewUiState.reviews,
                averageRating = reviewUiState.averageRating,
                isLoading = reviewUiState.isLoadingReviews
            )
        }

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
            onClick = {},
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
fun ProfileHeaderCard(
    user: User?,
    averageRating: Float = 0f,
    reviewCount: Int = 0
) {
    val displayName = user?.profileName ?: "Гость"
    val nameParts = displayName.split(" ")
    val formattedName = if (nameParts.size >= 2) "${nameParts[0]}\n${nameParts[1]}" else displayName

    val roleDescription = when (user?.role) {
        UserRole.STUDENT  -> "Студент"
        UserRole.EMPLOYER -> user.companyPosition ?: "Работодатель"
        null              -> "Не авторизован"
    }

    val emoji = when (user?.role) {
        UserRole.STUDENT  -> "👨‍💻"
        UserRole.EMPLOYER -> "💼"
        null              -> "👤"
    }

    val ratingText = if (averageRating > 0f) {
        "%.1f".format(averageRating)
    } else "—"

    val reviewsText = when {
        reviewCount == 0 -> "Нет отзывов"
        reviewCount == 1 -> "1 отзыв"
        reviewCount in 2..4 -> "$reviewCount отзыва"
        else -> "$reviewCount отзывов"
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
                            tint = if (averageRating > 0f) Color(0xFFFFC107)
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ratingText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "($reviewsText)",
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
                StatItem(value = if (reviewCount > 0) "%.1f".format(averageRating) else "—", label = "Рейтинг")
                StatItem(value = reviewCount.toString(), label = "Отзывы")
                StatItem(value = "—", label = "Проекты")
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

@Composable
fun ReviewsSection(
    reviews: List<Review>,
    averageRating: Float,
    isLoading: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Отзывы",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (averageRating > 0f) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.1f".format(averageRating),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                reviews.take(5).forEach { review ->
                    ReviewCard(review = review)
                }
                if (reviews.size > 5) {
                    Text(
                        text = "Ещё ${reviews.size - 5} отзывов...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (review.reviewerRole == "EMPLOYER") "💼" else "👨‍💻",
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.reviewerName.ifBlank { "Пользователь" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (review.taskTitle.isNotBlank()) {
                        Text(
                            text = review.taskTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    StarRow(rating = review.rating)
                    if (review.createdAt > 0) {
                        Text(
                            text = formatReviewDate(review.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StarRow(rating: Int) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private fun formatReviewDate(ts: Long): String {
    return try {
        SimpleDateFormat("d MMM yyyy", Locale("ru")).format(Date(ts))
    } catch (e: Exception) { "" }
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
                modifier = Modifier.clickable { }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val skills = listOf("React", "TypeScript", "Figma", "Python", "SQL")
            skills.forEach { skill -> TagItem(text = skill) }
        }
    }
}

@Composable
fun VerificationSection(verificationStatus: VerificationStatus, onVerifyClick: () -> Unit) {
    when (verificationStatus) {
        VerificationStatus.NOT_VERIFIED -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).background(Color(0xFFFFE0B2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Warning, null, tint = Color(0xFFE65100), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Профиль не подтвержден", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Для публикации заданий пройдите верификацию", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF57C00))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onVerifyClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                    ) {
                        Icon(Icons.Outlined.VerifiedUser, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Подтвердить профиль", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        VerificationStatus.PENDING -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(Color(0xFFBBDEFB), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Schedule, null, tint = Color(0xFF1565C0), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Проверка документов", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ваша заявка обрабатывается", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1976D2))
                    }
                }
            }
        }
        VerificationStatus.VERIFIED -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(Color(0xFFC8E6C9), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Профиль верифицирован", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Вы можете публиковать задания", style = MaterialTheme.typography.bodySmall, color = Color(0xFF388E3C))
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
    onReviewsClick: () -> Unit = {},
    activeTasksCount: Int,
    reviewCount: Int = 0,
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
            ProfileMenuItem(
                icon = Icons.Outlined.Star,
                title = "Отзывы",
                badgeCount = if (reviewCount > 0) reviewCount else null,
                onClick = onReviewsClick
            )
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
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
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
            Surface(color = MaterialTheme.colorScheme.secondary, shape = CircleShape) {
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
        Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ExitButton(onLogout: () -> Unit) {
    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ExitToApp, null, tint = MaterialTheme.colorScheme.error)
            Text("Выйти из аккаунта", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
