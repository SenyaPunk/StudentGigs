package com.example.studentgigs.view.OnApp.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ReviewDialog(
    revieweeName: String,
    taskTitle: String,
    isSubmitting: Boolean,
    onSubmit: (rating: Int, comment: String) -> Unit,
    onSkip: () -> Unit
) {
    var selectedRating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    val ratingLabels = listOf("Ужасно", "Плохо", "Нормально", "Хорошо", "Отлично")

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Emoji-аватар
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⭐", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Оставьте отзыв",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = revieweeName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    if (taskTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = taskTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Звёзды
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            StarButton(
                                filled = i <= selectedRating,
                                onClick = { selectedRating = i }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedRating > 0) {
                        Text(
                            text = ratingLabels[selectedRating - 1],
                            style = MaterialTheme.typography.labelLarge,
                            color = starColor(selectedRating),
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "Выберите оценку",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Поле комментария
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { if (it.length <= 500) comment = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Расскажите подробнее (необязательно)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    if (comment.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${comment.length}/500",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Кнопка отправки
                    Button(
                        onClick = {
                            if (selectedRating > 0) onSubmit(selectedRating, comment)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = selectedRating > 0 && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                "Отправить отзыв",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Пропустить",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StarButton(filled: Boolean, onClick: () -> Unit) {
    val color by animateColorAsState(
        targetValue = if (filled) Color(0xFFFFC107) else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(),
        label = "starColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (filled) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "starScale"
    )
    Icon(
        imageVector = Icons.Rounded.Star,
        contentDescription = null,
        tint = color,
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}

private fun starColor(rating: Int): Color = when (rating) {
    1 -> Color(0xFFE53935)
    2 -> Color(0xFFFF7043)
    3 -> Color(0xFFFFC107)
    4 -> Color(0xFF66BB6A)
    5 -> Color(0xFF43A047)
    else -> Color.Gray
}
