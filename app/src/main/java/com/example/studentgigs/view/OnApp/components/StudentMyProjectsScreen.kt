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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.Application
import com.example.studentgigs.data.model.ApplicationStatus
import com.example.studentgigs.data.model.Task
import com.example.studentgigs.viewmodel.ApplicationViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Экран "Мои проекты" для студента
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StudentMyProjectsScreen(
    applicationViewModel: ApplicationViewModel,
    onBack: () -> Unit,
    onTaskClick: (Task) -> Unit = {}
) {
    val uiState by applicationViewModel.uiState.collectAsState()

    val inProgress  = uiState.applications.filter { it.status == ApplicationStatus.IN_PROGRESS }
    val pending     = uiState.applications.filter { it.status == ApplicationStatus.PENDING }
    val completed   = uiState.applications.filter { it.status == ApplicationStatus.COMPLETED }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = onBack
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Назад",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Мои проекты",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.applications.isEmpty()) {
            EmptyProjectsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── В работе ──────────────────────────────────────────────────
            if (inProgress.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "В работе",
                        count = inProgress.size,
                        color = Color(0xFF4CAF50)
                    )
                }
                items(inProgress, key = { it.applicationId }) { app ->
                    ApplicationCard(
                        application = app,
                        statusColor = Color(0xFF4CAF50),
                        statusLabel = "В работе",
                        statusIcon = { StatusDot(Color(0xFF4CAF50)) },
                        onClick = { app.task?.let { onTaskClick(it) } }
                    )
                }
            }

            // ── Вы откликнулись ───────────────────────────────────────────
            if (pending.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Вы откликнулись",
                        count = pending.size,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(pending, key = { it.applicationId }) { app ->
                    ApplicationCard(
                        application = app,
                        statusColor = MaterialTheme.colorScheme.primary,
                        statusLabel = "Ожидает ответа",
                        statusIcon = { StatusDot(MaterialTheme.colorScheme.primary) },
                        onClick = { app.task?.let { onTaskClick(it) } }
                    )
                }
            }

            // ── Завершённые ───────────────────────────────────────────────
            if (completed.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Завершённые",
                        count = completed.size,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(completed, key = { it.applicationId }) { app ->
                    ApplicationCard(
                        application = app,
                        statusColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        statusLabel = "Завершено",
                        statusIcon = {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        onClick = { app.task?.let { onTaskClick(it) } }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Карточка отклика
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ApplicationCard(
    application: Application,
    statusColor: Color,
    statusLabel: String,
    statusIcon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val task = application.task

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Иконка задания
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = task?.iconEmoji ?: "📋", fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task?.title ?: "Задание",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task?.employerName ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Цена
                task?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CurrencyRuble,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val displayPrice = if (it.price.contains("₽")) it.price else "${it.price} ₽"
                        Text(
                            text = displayPrice,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Статус
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        statusIcon()
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Срок
            task?.duration?.takeIf { it.isNotBlank() }?.let { duration ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Заголовок секции
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = CircleShape
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Точка статуса
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Пустое состояние
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EmptyProjectsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📋", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Пока нет откликов",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Откликайтесь на задания, чтобы они\nпоявились здесь",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
