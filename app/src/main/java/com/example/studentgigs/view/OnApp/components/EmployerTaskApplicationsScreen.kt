package com.example.studentgigs.view.OnApp.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.Application
import com.example.studentgigs.data.model.ApplicationStatus
import com.example.studentgigs.data.model.Task
import com.example.studentgigs.data.model.User
import com.example.studentgigs.view.OnApp.HeaderCircleButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Экран со списком всех откликов на конкретное задание (вид работодателя).
 *
 * @param task           Задание, для которого показываем отклики.
 * @param applications   Список откликов (с вложенным [Application.student]).
 * @param isLoading      true пока идёт загрузка с сервера.
 * @param errorMessage   Ошибка (Snackbar).
 * @param onBack         Назад.
 * @param onStudentClick Нажатие на карточку студента → переход к его профилю.
 */
@Composable
fun EmployerTaskApplicationsScreen(
    task: Task,
    applications: List<Application>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit,
    onStudentClick: (Application) -> Unit,
    onClearError: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage, duration = SnackbarDuration.Short)
            onClearError()
        }
    }

    // Сортировка: принятый (IN_PROGRESS) — вверху, остальные по дате
    val sorted = remember(applications) {
        applications.sortedWith(
            compareByDescending<Application> { it.status == ApplicationStatus.IN_PROGRESS }
                .thenByDescending { it.appliedAt }
        )
    }

    val acceptedCount = applications.count { it.status == ApplicationStatus.IN_PROGRESS }
    val pendingCount  = applications.count { it.status == ApplicationStatus.PENDING }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderCircleButton(
                        icon = Icons.Default.ChevronLeft,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconColor = MaterialTheme.colorScheme.onSurface,
                        onClick = onBack
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Отклики",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (applications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Откликов пока нет",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Студенты ещё не откликнулись\nна это задание",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Статистика
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatChip(
                        modifier = Modifier.weight(1f),
                        label = "Всего",
                        count = applications.size,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        label = "Ожидают",
                        count = pendingCount,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        label = "Принят",
                        count = acceptedCount,
                        color = Color(0xFF4CAF50)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Принятый студент — отдельный заголовок
            if (acceptedCount > 0) {
                item {
                    Text(
                        text = "В работе",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            val accepted = sorted.filter { it.status == ApplicationStatus.IN_PROGRESS }
            items(accepted, key = { "accepted_${it.applicationId}" }) { app ->
                ApplicantCard(
                    application = app,
                    onClick = { onStudentClick(app) }
                )
            }

            // ИСПРАВЛЕНО: разделяем PENDING и REJECTED — раньше оба шли в "Ожидают"
            val pending  = sorted.filter { it.status == ApplicationStatus.PENDING }
            val rejected = sorted.filter { it.status == ApplicationStatus.REJECTED }

            if (pending.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ожидают рассмотрения",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(pending, key = { "pending_${it.applicationId}" }) { app ->
                    ApplicantCard(
                        application = app,
                        onClick = { onStudentClick(app) }
                    )
                }
            }

            if (rejected.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Отклонённые",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(rejected, key = { "rejected_${it.applicationId}" }) { app ->
                    ApplicantCard(
                        application = app,
                        onClick = { onStudentClick(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ApplicantCard(
    application: Application,
    onClick: () -> Unit
) {
    val student = application.student
    val isAccepted = application.status == ApplicationStatus.IN_PROGRESS
    val isRejected = application.status == ApplicationStatus.REJECTED

    val cardColor = when {
        isAccepted -> Color(0xFF4CAF50).copy(alpha = 0.08f)
        isRejected -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isAccepted) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар студента
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (isAccepted) Color(0xFF4CAF50).copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isAccepted) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student?.displayName ?: "Студент",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Откликнулся ${formatDate(application.appliedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Статус badge
                StatusBadge(status = application.status)
            }

            Icon(
                Icons.Outlined.KeyboardArrowRight,
                contentDescription = "Подробнее",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StatusBadge(status: ApplicationStatus) {
    val (label, color) = when (status) {
        ApplicationStatus.PENDING -> "Ожидает" to MaterialTheme.colorScheme.primary
        ApplicationStatus.IN_PROGRESS -> "Принят ✓" to Color(0xFF4CAF50)
        ApplicationStatus.COMPLETED -> "Завершён" to MaterialTheme.colorScheme.onSurfaceVariant
        ApplicationStatus.REJECTED -> "Отклонён" to MaterialTheme.colorScheme.error
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("d MMM", Locale("ru"))
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}
