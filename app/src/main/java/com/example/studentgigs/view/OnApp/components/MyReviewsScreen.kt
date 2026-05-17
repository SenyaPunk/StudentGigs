package com.example.studentgigs.view.OnApp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.Review
import com.example.studentgigs.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyReviewsScreen(
    reviewViewModel: ReviewViewModel,
    onBack: () -> Unit
) {
    val uiState by reviewViewModel.uiState.collectAsState()

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
                    Text("Мои отзывы", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    if (uiState.reviews.isNotEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            color = Color(0xFFFFC107).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier              = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                Text(
                                    text       = "%.1f".format(uiState.averageRating),
                                    style      = MaterialTheme.typography.labelMedium,
                                    color      = Color(0xFFFFC107),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text  = "· ${uiState.totalCount} отз.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoadingReviews -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.reviews.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(32.dp)
                    ) {
                        Text("⭐", fontSize = 56.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("Отзывов пока нет", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Отзывы появятся после\nзавершения заданий",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.reviews, key = { it.id }) { review ->
                        ReviewCard(review = review)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = review.reviewerName.ifBlank { "Аноним" },
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    val roleLabel = when (review.reviewerRole) {
                        "STUDENT"  -> "Студент"
                        "EMPLOYER" -> "Работодатель"
                        else       -> review.reviewerRole
                    }
                    Text(
                        text  = roleLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StarRating(rating = review.rating)
            }

            if (review.taskTitle.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text     = "📋 ${review.taskTitle}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (review.comment.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text  = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (review.createdAt > 0) {
                Spacer(Modifier.height(8.dp))
                // createdAt уже в миллисекундах — НЕ умножаем на 1000
                val dateStr = try {
                    SimpleDateFormat("d MMM yyyy", Locale("ru")).format(Date(review.createdAt))
                } catch (e: Exception) { "" }
                Text(
                    text  = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StarRating(rating: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { i ->
            Icon(
                imageVector        = if (i < rating) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint               = if (i < rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.outlineVariant,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}
