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
    onTaskClick: (Task) -> Unit = {},
    // НОВОЕ: прямой переход в рабочую зону для принятых откликов
    onWorkspaceClick: (Application) -> Unit = {}
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
                    // ИСПРАВЛЕНО: В работе → сразу в рабочую зону
                    ApplicationCard(
                        application = app,
                        statusColor = Color(0xFF4CAF50),
                        statusLabel = "В работе",
                        statusIcon = { StatusDot(Color(0xFF4CAF50)) },
                        showWorkspaceButton = true,
                        onWorkspaceClick = { onWorkspaceClick(app) },
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
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                items(completed, key = { it.applicationId }) { app ->
                    ApplicationCard(
                        application = app,
                        statusColor = MaterialTheme.colorScheme.secondary,
                        statusLabel = "Завершено",
                        statusIcon = {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                null,
                                tint     = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(10.dp)
                            )
                        },
                        onClick = { app.task?.let { onTaskClick(it) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(color, CircleShape)
    )
}

// ИСПРАВЛЕНО: добавлены параметры showWorkspaceButton и onWorkspaceClick
@Composable
private fun ApplicationCard(
    application: Application,
    statusColor: Color,
    statusLabel: String,
    statusIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    showWorkspaceButton: Boolean = false,
    onWorkspaceClick: () -> Unit = {}
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = application.task?.iconEmoji ?: "📋",
                    fontSize = 28.sp
                )
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
                            text  = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text       = application.task?.title ?: "Задание удалено",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines   = 2
            )

            if (!application.task?.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = application.task!!.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            if (!application.task?.price.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.CurrencyRuble, null,
                        modifier = Modifier.size(14.dp),
                        tint     = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text  = application.task!!.price,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // НОВОЕ: кнопка "Рабочая зона" для принятых откликов
            if (showWorkspaceButton) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onWorkspaceClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(
                        Icons.Outlined.Forum, null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Открыть рабочую зону", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EmptyProjectsState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            Text("📋", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Откликов пока нет",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Откликайтесь на задания,\nчтобы они появились здесь",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
