package com.example.studentgigs.view.OnApp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.Task
import com.example.studentgigs.data.model.TaskStatus
import com.example.studentgigs.view.OnApp.HeaderCircleButton

@Composable
fun EmployerTasksScreen(
    tasks: List<Task>,
    currentEmployerId: Long,
    onBack: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onLeaveReviewForTask: (Task) -> Unit = {},
    // Набор taskId, для которых работодатель уже оставил отзыв
    reviewedTaskIds: Set<Long> = emptySet()
) {
    val myTasks        = tasks.filter { it.employerId == currentEmployerId }
    val activeTasks    = myTasks.filter { it.status == TaskStatus.ACTIVE }
    val inWorkTasks    = myTasks.filter { it.status == TaskStatus.CLOSED }
    val completedTasks = myTasks.filter { it.status == TaskStatus.COMPLETED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 15.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCircleButton(
                icon            = Icons.Default.ChevronLeft,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                iconColor       = MaterialTheme.colorScheme.onSurface,
                onClick         = onBack
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text  = "Мои задания",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding      = PaddingValues(bottom = 32.dp)
        ) {
            // ── Активные ──────────────────────────────────────────────────
            if (activeTasks.isNotEmpty()) {
                item { EmployerSectionHeader("Активные", activeTasks.size, MaterialTheme.colorScheme.primary) }
                items(activeTasks) { task ->
                    SearchResultCard(task = task, onClick = { onTaskClick(task) })
                }
            }

            // ── В работе ──────────────────────────────────────────────────
            if (inWorkTasks.isNotEmpty()) {
                if (activeTasks.isNotEmpty()) item { Spacer(Modifier.height(4.dp)) }
                item { EmployerSectionHeader("В работе", inWorkTasks.size, Color(0xFF4CAF50)) }
                items(inWorkTasks) { task ->
                    SearchResultCard(task = task, onClick = { onTaskClick(task) })
                }
            }

            // ── Завершённые (с индикатором отзыва) ───────────────────────
            if (completedTasks.isNotEmpty()) {
                if (activeTasks.isNotEmpty() || inWorkTasks.isNotEmpty()) item { Spacer(Modifier.height(4.dp)) }
                item { EmployerSectionHeader("Завершённые", completedTasks.size, MaterialTheme.colorScheme.secondary) }
                items(completedTasks) { task ->
                    CompletedTaskCard(
                        task           = task,
                        alreadyReviewed = task.id in reviewedTaskIds,
                        onTaskClick    = { onTaskClick(task) },
                        onLeaveReview  = { onLeaveReviewForTask(task) }
                    )
                }
            }

            // ── Пусто ─────────────────────────────────────────────────────
            if (myTasks.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillParentMaxSize().padding(bottom = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📋", fontSize = 56.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text  = "Вы ещё не создали ни одного задания",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedTaskCard(
    task: Task,
    alreadyReviewed: Boolean,
    onTaskClick: () -> Unit,
    onLeaveReview: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick   = onTaskClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = task.iconEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = task.title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            null,
                            tint     = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text  = "Завершено",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            if (alreadyReviewed) {
                // Отзыв уже оставлен — показываем метку
                Surface(
                    color    = Color(0xFF4CAF50).copy(alpha = 0.10f),
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint     = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Отзыв оставлен",
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = Color(0xFF388E3C),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                // Отзыв ещё не оставлен — кнопка
                OutlinedButton(
                    onClick  = onLeaveReview,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFC107)),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107))
                ) {
                    Icon(Icons.Rounded.Star, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFC107))
                    Spacer(Modifier.width(8.dp))
                    Text("Оставить отзыв", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun EmployerSectionHeader(title: String, count: Int, color: Color = MaterialTheme.colorScheme.primary) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = color)
        Surface(color = color.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small) {
            Text(
                "$count",
                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style      = MaterialTheme.typography.labelMedium,
                color      = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
