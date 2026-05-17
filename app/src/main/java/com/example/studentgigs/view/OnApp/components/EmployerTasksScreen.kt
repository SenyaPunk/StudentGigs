package com.example.studentgigs.view.OnApp.components

  import androidx.compose.foundation.background
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.lazy.LazyColumn
  import androidx.compose.foundation.lazy.items
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.ChevronLeft
  import androidx.compose.material.icons.outlined.CheckCircle
  import androidx.compose.material.icons.outlined.Work
  import androidx.compose.material3.*
  import androidx.compose.runtime.Composable
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.alpha
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
      onTaskClick: (Task) -> Unit
  ) {
      val myTasks     = tasks.filter { it.employerId == currentEmployerId }
      val activeTasks    = myTasks.filter { it.status == TaskStatus.ACTIVE }
      val inWorkTasks    = myTasks.filter { it.status == TaskStatus.CLOSED }     // исполнитель выбран, в работе
      val completedTasks = myTasks.filter { it.status == TaskStatus.COMPLETED }  // оба подтвердили

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
              // ── Активные (принимают отклики) ──────────────────────────────
              if (activeTasks.isNotEmpty()) {
                  item { EmployerSectionHeader("Активные", activeTasks.size, MaterialTheme.colorScheme.primary) }
                  items(activeTasks) { task ->
                      SearchResultCard(task = task, onClick = { onTaskClick(task) })
                  }
              }

              // ── В работе (исполнитель выбран) ─────────────────────────────
              if (inWorkTasks.isNotEmpty()) {
                  if (activeTasks.isNotEmpty()) item { Spacer(Modifier.height(4.dp)) }
                  item { EmployerSectionHeader("В работе", inWorkTasks.size, Color(0xFF4CAF50)) }
                  items(inWorkTasks) { task ->
                      SearchResultCard(task = task, onClick = { onTaskClick(task) })
                  }
              }

              // ── Завершённые ───────────────────────────────────────────────
              if (completedTasks.isNotEmpty()) {
                  if (activeTasks.isNotEmpty() || inWorkTasks.isNotEmpty()) item { Spacer(Modifier.height(4.dp)) }
                  item { EmployerSectionHeader("Завершённые", completedTasks.size, MaterialTheme.colorScheme.primary) }
                  items(completedTasks) { task ->
                      Box(modifier = Modifier.alpha(0.7f)) {
                          SearchResultCard(task = task, onClick = { onTaskClick(task) })
                      }
                  }
              }

              // ── Пусто ─────────────────────────────────────────────────────
              if (myTasks.isEmpty()) {
                  item {
                      Box(
                          modifier = Modifier
                              .fillParentMaxSize()
                              .padding(bottom = 100.dp),
                          contentAlignment = Alignment.Center
                      ) {
                          Column(horizontalAlignment = Alignment.CenterHorizontally) {
                              Text("📋", fontSize = 56.sp)
                              Spacer(Modifier.height(16.dp))
                              Text(
                                  text = "Вы ещё не создали ни одного задания",
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
  fun EmployerSectionHeader(title: String, count: Int, color: Color = MaterialTheme.colorScheme.primary) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
      ) {
          Text(
              title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = color
          )
          Surface(
              color = color.copy(alpha = 0.12f),
              shape = MaterialTheme.shapes.small
          ) {
              Text(
                  "$count",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelMedium,
                  color = color,
                  fontWeight = FontWeight.Bold
              )
          }
      }
  }
  