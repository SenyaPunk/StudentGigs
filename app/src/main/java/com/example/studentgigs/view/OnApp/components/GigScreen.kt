package com.example.studentgigs.view.OnApp

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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.*
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.LocationType

@Composable
fun GigScreen(task: com.example.studentgigs.data.model.Task, onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Button(
                            onClick = { /* Откликнуться */ },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Откликнуться", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
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
            // Заголовок
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

            // Инфо-карточки
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val itemModifier = Modifier.weight(1f)

                // 1. Срок
                InfoDetailCard(itemModifier, Icons.Outlined.Schedule, "Срок", task.duration.ifEmpty { "Не указан" })

                // 2. Оплата (авто-добавление ₽)
                val displayPrice = if (task.price.contains("₽")) task.price else "${task.price} ₽"
                InfoDetailCard(itemModifier, Icons.Outlined.CurrencyRuble, "Оплата", displayPrice, MaterialTheme.colorScheme.primary)

                // 3. Формат (Локализация)
                val locationTypeRu = when (task.locationType) {
                    LocationType.REMOTE -> "Удалённо"
                    LocationType.OFFICE -> "В офисе"
                    LocationType.HYBRID -> "Гибрид"
                }
                InfoDetailCard(itemModifier, Icons.Outlined.Place, "Формат", locationTypeRu)

                // 4. Компания (Вместо дедлайна)
                InfoDetailCard(itemModifier, Icons.Outlined.Business, "Заказчик", task.employerName)
            }

            // Навыки
            if (task.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Навыки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    task.tags.forEach { tag -> TagItem(tag) }
                }
            }

            // Описание
            Spacer(modifier = Modifier.height(24.dp))
            Text("Описание", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            // Требования (улучшенная проверка на пустоту)
            val validRequirements = task.requirements.filter { it.isNotBlank() }
            if (validRequirements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Требования", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                validRequirements.forEach { req ->
                    RequirementItem(req)
                }
            }

            // Преимущества
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
                    validBenefits.forEach { benefit ->
                        BenefitItem(benefit)
                    }
                }
            }

            // Отклики
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



// Кнопка для TopBar
@Composable
fun HeaderCircleButton(icon: ImageVector, backgroundColor: Color, iconColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = backgroundColor,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    val backgroundColor = if (isSystemInDarkTheme()) {
        Color(0xFF382C1E)
    } else {
        Color(0xFFFFECB3)
    }

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
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
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
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor)
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