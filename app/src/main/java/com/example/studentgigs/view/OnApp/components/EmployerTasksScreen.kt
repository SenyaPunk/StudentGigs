package com.example.studentgigs.view.OnApp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studentgigs.data.model.Task
import com.example.studentgigs.data.model.TaskStatus
import com.example.studentgigs.view.OnApp.HeaderCircleButton

@Composable
fun EmployerTasksScreen(
    tasks: List<Task>,
    currentEmployerId: Long,
    onBack: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val myTasks = tasks.filter { it.employerId == currentEmployerId }

    // ИСПРАВЛЕНО: TaskStatus.CLOSED вместо строки "COMPLETED" (которой не существует в enum)
    val activeTasks = myTasks.filter { it.status == TaskStatus.ACTIVE }
    val closedTasks = myTasks.filter { it.status == TaskStatus.CLOSED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 15.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCircleButton(
                icon = Icons.Default.ChevronLeft,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurface,
                onClick = onBack
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Мои задания",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            if (activeTasks.isNotEmpty()) {
                item { SectionHeader("Активные", activeTasks.size) }
                items(activeTasks) { task ->
                    SearchResultCard(task = task, onClick = { onTaskClick(task) })
                }
            }

            if (closedTasks.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item { SectionHeader("Завершённые", closedTasks.size) }
                items(closedTasks) { task ->
                    Box(modifier = Modifier.alpha(0.7f)) {
                        SearchResultCard(task = task, onClick = { onTaskClick(task) })
                    }
                }
            }

            if (myTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Вы еще не создали ни одного задания",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "$count",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
