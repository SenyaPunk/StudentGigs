package com.example.studentgigs.view.OnApp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.LocationType
import com.example.studentgigs.data.model.Task

/**
 * Экран детальной карточки задания.
 *
 * @param task              Задание.
 * @param onBack            Назад.
 * @param isStudent         true — показывать кнопку "Откликнуться".
 * @param isApplied         true — студент уже откликнулся.
 * @param isApplying        true — идёт отправка отклика.
 * @param errorMessage      Ошибка (Snackbar).
 * @param onApply           Callback отклика студента.
 * @param onClearError      Сбросить ошибку.
 * @param isEmployerOwner   true — задание принадлежит текущему работодателю.
 *                          Вместо кнопки "Откликнуться" покажет "Перейти к заданию".
 * @param onGoToTask        Callback кнопки "Перейти к заданию" (только для работодателя-владельца).
 */
@Composable
fun GigScreen(
    task: Task,
    onBack: () -> Unit,
    isStudent: Boolean = false,
    isApplied: Boolean = false,
    isApplying: Boolean = false,
    isInProgress: Boolean = false,
    errorMessage: String? = null,
    onApply: () -> Unit = {},
    onGoToWorkspace: () -> Unit = {},
    onClearError: () -> Unit = {},
    isEmployerOwner: Boolean = false,
    onGoToTask: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val buttonColor by animateColorAsState(
        targetValue = if (isApplied) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 400),
        label = "buttonColor"
    )

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            onClearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 4.dp) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HeaderCircleButton(
                            icon = Icons.Rounded.IosShare,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = { /* Share */ }
                        )
                        HeaderCircleButton(
                            icon = Icons.Outlined.BookmarkBorder,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = { /* Save */ }
                        )
                    }
                }
            }
        },
        bottomBar = {
            // ── Студент — кнопка "Откликнуться" ────────────────────────────
            if (isStudent) {
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            when {
                                isInProgress -> {
                                    Button(
                                        onClick  = onGoToWorkspace,
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape    = RoundedCornerShape(16.dp),
                                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                                    ) {
                                        Icon(
                                            imageVector        = Icons.Outlined.Construction,
                                            contentDescription = null,
                                            tint               = Color.White,
                                            modifier           = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text       = "Выполнить задание",
                                            fontSize   = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = Color.White
                                        )
                                    }
                                }
                                isApplying -> {
                                    Button(
                                        onClick  = {},
                                        enabled  = false,
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape    = RoundedCornerShape(16.dp),
                                        colors   = ButtonDefaults.buttonColors(containerColor = buttonColor)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier    = Modifier.size(22.dp),
                                            color       = Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                                isApplied -> {
                                    Button(
                                        onClick  = {},
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape    = RoundedCornerShape(16.dp),
                                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Icon(
                                            imageVector        = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint               = Color.White,
                                            modifier           = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text       = "Вы откликнулись",
                                            fontSize   = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = Color.White
                                        )
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick  = onApply,
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape    = RoundedCornerShape(16.dp),
                                        colors   = ButtonDefaults.buttonColors(containerColor = buttonColor)
                                    ) {
                                        Text(
                                            text       = "Откликнуться",
                                            fontSize   = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Работодатель-владелец — кнопка "Перейти к заданию" ─────────
            if (isEmployerOwner) {
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Button(
                                onClick = onGoToTask,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (task.responsesCount > 0)
                                        "Перейти к заданию  •  ${task.responsesCount}"
                                    else
                                        "Перейти к заданию",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(task.iconEmoji.ifEmpty { "📋" }, fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = task.employerPosition ?: task.employerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val itemModifier = Modifier.weight(1f)
                InfoDetailCard(itemModifier, Icons.Outlined.Schedule, "Срок", task.duration.ifEmpty { "Не указан" })
                val displayPrice = if (task.price.contains("₽")) task.price else "${task.price} ₽"
                InfoDetailCard(itemModifier, Icons.Outlined.CurrencyRuble, "Оплата", displayPrice, MaterialTheme.colorScheme.primary)
                val locationTypeRu = when (task.locationType) {
                    LocationType.REMOTE -> "Удалённо"
                    LocationType.OFFICE -> "В офисе"
                    LocationType.HYBRID -> "Гибрид"
                }
                InfoDetailCard(itemModifier, Icons.Outlined.Place, "Формат", locationTypeRu)
                InfoDetailCard(itemModifier, Icons.Outlined.Business, "Заказчик", task.employerName)
            }

            if (task.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Навыки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    task.tags.forEach { tag -> TagItem(tag) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Описание", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            val validRequirements = task.requirements.filter { it.isNotBlank() }
            if (validRequirements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Требования", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                validRequirements.forEach { req -> RequirementItem(req) }
            }

            val validBenefits = task.benefits.filter { it.isNotBlank() }
            if (validBenefits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Что вы получите", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    validBenefits.forEach { benefit -> BenefitItem(benefit) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Group, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${task.responsesCount} откликов",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    val backgroundColor = if (isSystemInDarkTheme()) Color(0xFF382C1E) else Color(0xFFFFECB3)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun InfoDetailCard(modifier: Modifier, icon: ImageVector, label: String, value: String, valueColor: Color = Color.Unspecified) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
            )
        }
    }
}

@Composable
fun RequirementItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
