package com.example.studentgigs.view.OnApp.components
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.view.OnApp.HeaderCircleButton

enum class NotificationCategory {
    RESPONSE,    // Ответ на заявку
    NEW_PROJECT, // Новый проект
    DEADLINE,    // Дедлайн
    REVIEW,      // Отзыв
    SYSTEM       // Системное (заполнение профиля)
}

data class NotificationItemData(
    val id: Int,
    val category: NotificationCategory,
    val title: String,
    val description: String,
    val time: String,
    val isUnread: Boolean = false,
    val rating: Int? = null // Только для категории REVIEW
)

@Composable
fun NotificationScreen(onBack: () -> Unit) {
    val notifications = remember {
        listOf(
            NotificationItemData(1, NotificationCategory.RESPONSE, "Новый ответ на заявку", "TechStart ответил на вашу заявку на проект 'Landing Page'", "5 мин назад", true),
            NotificationItemData(2, NotificationCategory.NEW_PROJECT, "Новый проект в вашей области", "Появился новый проект по React разработке", "1 час назад", true),
            NotificationItemData(3, NotificationCategory.DEADLINE, "Приближается дедлайн", "До сдачи проекта 'Анализ данных' осталось 2 дня", "3 часа назад"),
            NotificationItemData(4, NotificationCategory.REVIEW, "Новый отзыв", "DataCorp оставил вам отзыв:", "Вчера", rating = 5),
            NotificationItemData(5, NotificationCategory.SYSTEM, "Профиль заполнен на 80%", "Добавьте портфолио для большего количества откликов", "2 дня назад"),
            NotificationItemData(6, NotificationCategory.RESPONSE, "Новый ответ на заявку", "TechStart ответил на вашу заявку на проект 'Landing Page'", "5 мин назад", true),
            NotificationItemData(7, NotificationCategory.NEW_PROJECT, "Новый проект в вашей области", "Появился новый проект по React разработке", "1 час назад", true),
            NotificationItemData(8, NotificationCategory.DEADLINE, "Приближается дедлайн", "До сдачи проекта 'Анализ данных' осталось 2 дня", "3 часа назад"),
            NotificationItemData(9, NotificationCategory.REVIEW, "Новый отзыв", "DataCorp оставил вам отзыв:", "Вчера", rating = 5),
            NotificationItemData(10, NotificationCategory.SYSTEM, "Профиль заполнен на 80%", "Добавьте портфолио для большего количества откликов", "2 дня назад")
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderCircleButton(
                    icon = Icons.Default.ChevronLeft,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onBack
                )

                Text(
                    text = "Уведомления",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,

                )


                HeaderCircleButton(
                    icon = Icons.Rounded.DoneAll,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { /* Пометить все как прочитанные */ }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { item ->
                NotificationCard(item)
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun NotificationCard(item: NotificationItemData) {
    val icon = when (item.category) {
        NotificationCategory.RESPONSE -> Icons.Rounded.ChatBubble
        NotificationCategory.NEW_PROJECT -> Icons.Rounded.Notifications
        NotificationCategory.DEADLINE -> Icons.Rounded.Alarm
        NotificationCategory.REVIEW -> Icons.Rounded.Star
        NotificationCategory.SYSTEM -> Icons.Rounded.EditNote
    }

    // Используем семантические цвета темы вместо хардкода
    val iconTint = when (item.category) {
        NotificationCategory.RESPONSE -> MaterialTheme.colorScheme.primary
        NotificationCategory.DEADLINE -> MaterialTheme.colorScheme.error
        NotificationCategory.REVIEW -> Color(0xFFFFC107) // Золотой обычно статичен для рейтингов
        else -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            // 1. Полоска слева (Primary цвет темы)
            if (item.isUnread) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // 2. Иконка со статичным фоном onSurfaceVariant
                Surface(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (item.isUnread) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Адаптивный серый текст
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    if (item.category == NotificationCategory.REVIEW && item.rating != null) {
                        Row(modifier = Modifier.padding(top = 6.dp)) {
                            repeat(item.rating) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = item.time,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}