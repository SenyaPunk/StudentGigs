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
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.Application
import com.example.studentgigs.data.model.ApplicationStatus
import com.example.studentgigs.data.model.Task
import com.example.studentgigs.viewmodel.ApplicationViewModel

@Composable
fun StudentMyProjectsScreen(
    applicationViewModel: ApplicationViewModel,
    onBack: () -> Unit,
    onTaskClick: (Task) -> Unit = {},
    onWorkspaceClick: (Application) -> Unit = {},
    onLeaveReview: (Application) -> Unit = {},
    // Набор applicationId, для которых студент уже оставил отзыв
    reviewedApplicationIds: Set<Long> = emptySet()
) {
    val uiState by applicationViewModel.uiState.collectAsState()

    val inProgress = uiState.applications.filter { it.status == ApplicationStatus.IN_PROGRESS }
    val pending    = uiState.applications.filter { it.status == ApplicationStatus.PENDING }
    val completed  = uiState.applications.filter { it.status == ApplicationStatus.COMPLETED }
    val rejected   = uiState.applications.filter { it.status == ApplicationStatus.REJECTED }

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
                        shape    = CircleShape,
                        color    = MaterialTheme.colorScheme.surfaceVariant,
                        onClick  = onBack
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Назад", modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Мои проекты", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.applications.isEmpty()) {
            EmptyProjectsState(modifier = Modifier.fillMaxSize().padding(innerPadding))
            return@Scaffold
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── В работе ───────────────────────────────────────────────────
            if (inProgress.isNotEmpty()) {
                item { SectionHeader("В работе", inProgress.size, Color(0xFF4CAF50)) }
                items(inProgress, key = { it.applicationId }) { app ->
                    ApplicationCard(
                        application         = app,
                        statusColor         = Color(0xFF4CAF50),
                        statusLabel         = "В работе",
                        statusIcon          = { StatusDot(Color(0xFF4CAF50)) },
                        showWorkspaceButton = true,
                        onWorkspaceClick    = { onWorkspaceClick(app) },
                        onClick             = { app.task?.let { onTaskClick(it) } },
                        reviewedApplicationIds = reviewedApplicationIds
                    )
                }
            }

            // ── Вы откликнулись ────────────────────────────────────────────
            if (pending.isNotEmpty()) {
                item { SectionHeader("Вы откликнулись", pending.size, MaterialTheme.colorScheme.primary) }
                items(pending, key = { it.applicationId }) { app ->
                    ApplicationCard(
                        application  = app,
                        statusColor  = MaterialTheme.colorScheme.primary,
                        statusLabel  = "Ожидает ответа",
                        statusIcon   = { StatusDot(MaterialTheme.colorScheme.primary) },
                        onClick      = { app.task?.let { onTaskClick(it) } },
                        reviewedApplicationIds = reviewedApplicationIds
                    )
                }
            }

            // ── Завершённые ────────────────────────────────────────────────
            if (completed.isNotEmpty()) {
                item { SectionHeader("Завершённые", completed.size, MaterialTheme.colorScheme.secondary) }
                items(completed, key = { it.applicationId }) { app ->
                    ApplicationCard(
                        application    = app,
                        statusColor    = MaterialTheme.colorScheme.secondary,
                        statusLabel    = "Завершено",
                        statusIcon     = {
                            Icon(
                                Icons.Rounded.CheckCircle, null,
                                tint     = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(10.dp)
                            )
                        },
                        showReviewButton      = true,
                        onLeaveReviewClick    = { onLeaveReview(app) },
                        onClick               = { app.task?.let { onTaskClick(it) } },
                        reviewedApplicationIds = reviewedApplicationIds
                    )
                }
            }

            // ── Отклонено ──────────────────────────────────────────────────
            if (rejected.isNotEmpty()) {
                item { SectionHeader("Отклонено", rejected.size, MaterialTheme.colorScheme.error) }
                items(rejected, key = { "rej_${it.applicationId}" }) { app ->
                    ApplicationCard(
                        application  = app,
                        statusColor  = MaterialTheme.colorScheme.error,
                        statusLabel  = "Отклонено",
                        statusIcon   = {
                            Icon(
                                Icons.Outlined.Cancel, null,
                                tint     = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(10.dp)
                            )
                        },
                        onClick      = { app.task?.let { onTaskClick(it) } },
                        reviewedApplicationIds = reviewedApplicationIds
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier              = Modifier.padding(bottom = 4.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        Text("($count)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatusDot(color: Color) {
    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
}

@Composable
private fun ApplicationCard(
    application: Application,
    statusColor: Color,
    statusLabel: String,
    statusIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    showWorkspaceButton: Boolean = false,
    onWorkspaceClick: () -> Unit = {},
    showReviewButton: Boolean = false,
    onLeaveReviewClick: () -> Unit = {},
    reviewedApplicationIds: Set<Long> = emptySet()
) {
    val alreadyReviewed = application.applicationId in reviewedApplicationIds

    Card(
        modifier  = Modifier.fillMaxWidth().animateContentSize().clickable(onClick = onClick),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(text = application.task?.iconEmoji ?: "📋", fontSize = 28.sp)
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        statusIcon()
                        Text(
                            statusLabel,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = statusColor,
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.CurrencyRuble, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(application.task!!.price, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }

            // Кнопка рабочей зоны
            if (showWorkspaceButton) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick  = onWorkspaceClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Outlined.Forum, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Открыть рабочую зону", fontWeight = FontWeight.SemiBold)
                }
            }

            // Блок отзыва для завершённых заданий
            if (showReviewButton) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(10.dp))

                if (alreadyReviewed) {
                    // Отзыв уже оставлен — показываем метку вместо кнопки
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
                        onClick  = onLeaveReviewClick,
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
}

@Composable
private fun EmptyProjectsState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📋", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text("Откликов пока нет", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Откликайтесь на задания,\nчтобы они появились здесь",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
