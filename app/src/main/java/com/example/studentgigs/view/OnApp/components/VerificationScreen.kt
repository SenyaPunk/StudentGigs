package com.example.studentgigs.view.OnApp.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.VerificationStatus
import com.example.studentgigs.view.OnApp.HeaderCircleButton
import com.example.studentgigs.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun VerificationScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onVerificationComplete: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Launcher для выбора изображения
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Слушаем успешную верификацию
    LaunchedEffect(uiState.verificationStatus) {
        if (uiState.verificationStatus == VerificationStatus.VERIFIED) {
            showSuccessDialog = true
            delay(2000)
            showSuccessDialog = false
            onVerificationComplete()
        }
    }

    // Запускаем проверку при открытии экрана, если статус PENDING
    LaunchedEffect(Unit) {
        if (uiState.verificationStatus == VerificationStatus.PENDING) {
            authViewModel.checkVerificationStatus()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
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
                        text = "Верификация",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiState.verificationStatus) {
                    VerificationStatus.NOT_VERIFIED -> {
                        NotVerifiedContent(
                            selectedImageUri = selectedImageUri,
                            isLoading = uiState.isVerificationLoading,
                            error = uiState.error,
                            onSelectImage = { imagePickerLauncher.launch("image/*") },
                            onSubmit = {
                                selectedImageUri?.let { uri ->
                                    authViewModel.submitVerification(uri.toString())
                                }
                            }
                        )
                    }
                    VerificationStatus.PENDING -> {
                        PendingVerificationContent(
                            remainingTimeMs = uiState.verificationRemainingTime
                        )
                    }
                    VerificationStatus.VERIFIED -> {
                        VerifiedContent()
                    }
                }
            }

            // Диалог успешной верификации
            AnimatedVisibility(
                visible = showSuccessDialog,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Профиль верифицирован!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Теперь вы можете создавать задания",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotVerifiedContent(
    selectedImageUri: Uri?,
    isLoading: Boolean,
    error: String?,
    onSelectImage: () -> Unit,
    onSubmit: () -> Unit
) {
    // Иконка
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.VerifiedUser,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Подтвердите профиль",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Для публикации заданий необходимо пройти верификацию. Загрузите фото паспорта для подтверждения личности.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Инструкции
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InstructionItem(
                icon = Icons.Outlined.CameraAlt,
                text = "Сфотографируйте разворот паспорта с фото"
            )
            Spacer(modifier = Modifier.height(12.dp))
            InstructionItem(
                icon = Icons.Outlined.Visibility,
                text = "Убедитесь, что все данные читаемы"
            )
            Spacer(modifier = Modifier.height(12.dp))
            InstructionItem(
                icon = Icons.Outlined.Lock,
                text = "Ваши данные защищены и не передаются третьим лицам"
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Область загрузки фото
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 2.dp,
                color = if (selectedImageUri != null)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelectImage() },
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Фото выбрано",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Нажмите, чтобы изменить",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Нажмите, чтобы загрузить фото",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Сообщение об ошибке
    if (error != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Кнопка отправки
    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = selectedImageUri != null && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Отправить на проверку",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PendingVerificationContent(
    remainingTimeMs: Long
) {
    var currentRemaining by remember { mutableStateOf(remainingTimeMs) }

    // Обновляем таймер
    LaunchedEffect(remainingTimeMs) {
        currentRemaining = remainingTimeMs
        while (currentRemaining > 0) {
            delay(1000)
            currentRemaining = (currentRemaining - 1000).coerceAtLeast(0)
        }
    }

    // Анимация вращения
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Проверяем данные",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Ваша заявка на верификацию обрабатывается. Это займет около минуты.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Таймер
    if (currentRemaining > 0) {
        val seconds = (currentRemaining / 1000).toInt()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Осталось примерно",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${seconds} сек",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Статус шаги
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            VerificationStep(
                title = "Документ загружен",
                isCompleted = true
            )
            VerificationStep(
                title = "Проверка данных",
                isCompleted = false,
                isInProgress = true
            )
            VerificationStep(
                title = "Верификация завершена",
                isCompleted = false
            )
        }
    }
}

@Composable
private fun VerifiedContent() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color(0xFF4CAF50).copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(48.dp)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Профиль верифицирован",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Ваш профиль успешно подтвержден. Теперь вы можете создавать и публиковать задания для студентов.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.TaskAlt,
                contentDescription = null,
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Доступно создание заданий",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Нажмите \"Добавить задание\" на главном экране",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF388E3C)
                )
            }
        }
    }
}

@Composable
private fun InstructionItem(
    icon: ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun VerificationStep(
    title: String,
    isCompleted: Boolean,
    isInProgress: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    when {
                        isCompleted -> Color(0xFF4CAF50)
                        isInProgress -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else if (isInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isCompleted -> Color(0xFF4CAF50)
                isInProgress -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isCompleted || isInProgress) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
