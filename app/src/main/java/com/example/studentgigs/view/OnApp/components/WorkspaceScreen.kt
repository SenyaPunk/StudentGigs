package com.example.studentgigs.view.OnApp.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.*
import com.example.studentgigs.view.OnApp.HeaderCircleButton
import com.example.studentgigs.viewmodel.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkspaceScreen(
    application: Application,
    task: Task,
    currentUser: User,
    workspaceViewModel: WorkspaceViewModel,
    onBack: () -> Unit,
    // Вызывается один раз, когда задание переходит в COMPLETED.
    // Передаёт данные для показа диалога отзыва (или null, если данных нет).
    onTaskCompleted: (PendingReview) -> Unit = {}
) {
    val uiState by workspaceViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isStudent = currentUser.role == UserRole.STUDENT

    var selectedTab by remember { mutableIntStateOf(0) }
    var messageText by remember { mutableStateOf("") }
    var showCompletionDialog by remember { mutableStateOf(false) }

    // Флаг, предотвращающий повторный вызов onTaskCompleted
    var reviewPromptTriggered by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            workspaceViewModel.uploadFile(
                context       = context,
                uri           = it,
                applicationId = application.applicationId,
                uploaderId    = currentUser.id,
                uploaderName  = currentUser.displayName
            )
        }
    }

    LaunchedEffect(uiState.error) {
        if (!uiState.error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(uiState.error!!, duration = SnackbarDuration.Long)
            workspaceViewModel.clearError()
        }
    }

    LaunchedEffect(uiState.uploadError) {
        if (!uiState.uploadError.isNullOrBlank()) {
            snackbarHostState.showSnackbar("Ошибка загрузки: ${uiState.uploadError}", duration = SnackbarDuration.Long)
            workspaceViewModel.clearUploadError()
        }
    }

    LaunchedEffect(application.applicationId, currentUser.id) {
        if (application.applicationId > 0) {
            workspaceViewModel.startWorkspace(application.applicationId, currentUser.id)
        }
    }

    DisposableEffect(Unit) {
        onDispose { workspaceViewModel.stopWorkspace() }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty() && selectedTab == 0) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Когда задание завершается — вызываем onTaskCompleted ровно один раз,
    // чтобы MainApp мог показать диалог отзыва поверх WorkspaceCompletedScreen.
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted && !reviewPromptTriggered) {
            reviewPromptTriggered = true
            val revieweeId = if (isStudent) uiState.employerId else uiState.studentId
            val revieweeName = if (isStudent) {
                task.employerName.ifBlank { "Работодатель" }
            } else {
                application.student?.displayName ?: "Студент"
            }
            if (revieweeId > 0) {
                onTaskCompleted(
                    PendingReview(
                        revieweeId    = revieweeId,
                        revieweeName  = revieweeName,
                        applicationId = application.applicationId,
                        taskId        = task.id,
                        taskTitle     = task.title,
                        reviewerRole  = if (isStudent) "STUDENT" else "EMPLOYER"
                    )
                )
            }
        }
    }

    val myConfirmed    = if (isStudent) uiState.studentConfirmed  else uiState.employerConfirmed
    val otherConfirmed = if (isStudent) uiState.employerConfirmed else uiState.studentConfirmed
    val otherTitle     = if (isStudent) "работодателя" else "студента"

    // Задание завершено — показываем экран завершения (диалог отзыва
    // показывается поверх него через MainApp)
    if (uiState.isCompleted) {
        WorkspaceCompletedScreen(task = task, onBack = onBack)
        return
    }

    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            icon = {
                Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Завершить задание?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Вы подтверждаете завершение задания «${task.title}».",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Задание будет завершено, когда обе стороны нажмут эту кнопку.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompletionDialog = false
                        workspaceViewModel.confirmCompletion(application.applicationId, currentUser.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Да, завершить", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCompletionDialog = false }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 4.dp) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderCircleButton(
                            icon            = Icons.Default.ChevronLeft,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconColor       = MaterialTheme.colorScheme.onSurface,
                            onClick         = onBack
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text       = "Рабочая зона",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text      = task.title,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines  = 1,
                                overflow  = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        when {
                            uiState.isConfirmingCompletion -> {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                            }
                            myConfirmed -> {
                                Surface(
                                    color  = MaterialTheme.colorScheme.secondaryContainer,
                                    shape  = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.HourglassBottom, null,
                                            modifier = Modifier.size(14.dp),
                                            tint     = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text  = "Ждём $otherTitle",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { showCompletionDialog = true },
                                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    shape   = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Завершить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (otherConfirmed && !myConfirmed) {
                        Surface(color = Color(0xFF4CAF50).copy(alpha = 0.12f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                Text(
                                    text  = if (isStudent) "Работодатель подтвердил завершение. Нажмите «Завершить» для окончания."
                                    else "Студент подтвердил завершение. Нажмите «Завершить» для окончания.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF388E3C),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (selectedTab == 0) {
                Surface(
                    modifier   = Modifier.fillMaxWidth(),
                    color      = MaterialTheme.colorScheme.background,
                    tonalElevation = 4.dp
                ) {
                    Column {
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick  = { filePickerLauncher.launch("*/*") },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.AttachFile, "Прикрепить",
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            OutlinedTextField(
                                value         = messageText,
                                onValueChange = { messageText = it },
                                modifier      = Modifier.weight(1f),
                                placeholder   = { Text("Сообщение...", style = MaterialTheme.typography.bodyMedium) },
                                shape         = RoundedCornerShape(24.dp),
                                maxLines      = 4,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (messageText.isNotBlank() && !uiState.isSendingMessage) {
                                        workspaceViewModel.sendMessage(
                                            applicationId = application.applicationId,
                                            senderId      = currentUser.id,
                                            senderName    = currentUser.displayName,
                                            message       = messageText.trim()
                                        )
                                        messageText = ""
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (messageText.isNotBlank()) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isSendingMessage) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color       = Color.White
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            if (messageText.isNotBlank() && !uiState.isSendingMessage) {
                                                workspaceViewModel.sendMessage(
                                                    applicationId = application.applicationId,
                                                    senderId      = currentUser.id,
                                                    senderName    = currentUser.displayName,
                                                    message       = messageText.trim()
                                                )
                                                messageText = ""
                                            }
                                        },
                                        enabled = messageText.isNotBlank()
                                    ) {
                                        Icon(
                                            Icons.Default.Send, "Отправить",
                                            tint     = if (messageText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("💬  Чат", "📎  Файлы").forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick  = { selectedTab = idx },
                        text     = {
                            Text(title, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal)
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> ChatContent(
                    messages      = uiState.messages,
                    currentUserId = currentUser.id,
                    listState     = listState
                )
                1 -> FilesContent(
                    files       = uiState.files,
                    isUploading = uiState.isUploadingFile,
                    onUpload    = { filePickerLauncher.launch("*/*") },
                    onOpenFile  = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) { /* ignore */ }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatContent(
    messages: List<WorkspaceMessage>,
    currentUserId: Long,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    if (messages.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💬", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Чат пуст", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Напишите первое сообщение",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        state          = listState,
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(messages, key = { it.id }) { msg ->
            val isMine = msg.senderId == currentUserId
            MessageBubble(message = msg, isMine = isMine)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun MessageBubble(message: WorkspaceMessage, isMine: Boolean) {
    val bubbleColor = if (isMine) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant
    val textColor   = if (isMine) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface
    val bubbleShape = if (isMine)
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    else
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)

    Column(
        modifier            = Modifier.fillMaxWidth().animateContentSize(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        if (!isMine) {
            Text(
                text       = message.senderName,
                style      = MaterialTheme.typography.labelSmall,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bubbleColor, bubbleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text  = message.message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
        Text(
            text     = formatMessageTime(message.createdAt),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun FilesContent(
    files: List<WorkspaceFile>,
    isUploading: Boolean,
    onUpload: () -> Unit,
    onOpenFile: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isUploading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier        = Modifier.weight(1f),
            contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (files.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("📂", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Файлов нет", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Загрузите файлы, которые нужно\nпередать работодателю",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(files, key = { it.id }) { file ->
                    FileItem(file = file, onOpen = { onOpenFile(file.downloadUrl) })
                }
            }
        }

        Surface(
            modifier       = Modifier.fillMaxWidth(),
            color          = MaterialTheme.colorScheme.background,
            tonalElevation = 4.dp
        ) {
            Column {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Button(
                    onClick  = onUpload,
                    enabled  = !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Outlined.Upload, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Загрузить файл", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FileItem(file: WorkspaceFile, onOpen: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val emoji = when {
                    file.mimeType.startsWith("image/")                                -> "🖼️"
                    file.mimeType.contains("pdf")                                     -> "📄"
                    file.mimeType.contains("zip") || file.mimeType.contains("rar")   -> "🗜️"
                    file.mimeType.contains("word") || file.mimeType.contains("doc")  -> "📝"
                    file.mimeType.contains("excel") || file.mimeType.contains("sheet") -> "📊"
                    file.mimeType.startsWith("video/")                               -> "🎬"
                    file.mimeType.startsWith("audio/")                               -> "🎵"
                    else                                                              -> "📎"
                }
                Text(emoji, fontSize = 22.sp)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text       = file.originalName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "${formatFileSize(file.fileSize)} • ${file.uploaderName} • ${formatMessageTime(file.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpen) {
                Icon(
                    Icons.Outlined.OpenInNew, "Открыть",
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkspaceCompletedScreen(task: Task, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🎉", fontSize = 72.sp)
            Text(
                "Задание завершено!",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = "«${task.title}» успешно выполнено. Обе стороны подтвердили завершение.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick  = onBack,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Rounded.CheckCircle, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Вернуться назад", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String = try {
    val now  = System.currentTimeMillis()
    val diff = now - timestamp
    if (diff < 24 * 60 * 60 * 1000) {
        SimpleDateFormat("HH:mm", Locale("ru")).format(Date(timestamp))
    } else {
        SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(timestamp))
    }
} catch (e: Exception) { "" }

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024        -> "$bytes Б"
    bytes < 1024 * 1024 -> "${bytes / 1024} КБ"
    else                -> "${"%.1f".format(bytes / 1024.0 / 1024.0)} МБ"
}
