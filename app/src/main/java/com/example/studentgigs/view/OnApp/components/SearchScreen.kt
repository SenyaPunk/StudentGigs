package com.example.studentgigs.view.OnApp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.view.OnApp.HeaderCircleButton
import com.example.studentgigs.view.OnApp.SearchInput

@Composable
fun SearchScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val recentSearches = remember {
        mutableListOf(
            "React разработка",
            "UI/UX дизайн",
            "Python",
            "Стажировка"
        )
    }

    var isRemoteOnly by remember { mutableStateOf(false) }
    var isShortTerm by remember { mutableStateOf(false) }
    var minPrice by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 15.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Панель поиска
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeaderCircleButton(
                icon = Icons.Default.ChevronLeft,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurface,
                onClick = onBack
            )

            SearchInput(
                search = searchQuery,
                onSearchChange = { searchQuery = it },
                modifier = Modifier.weight(1f)
            )

            HeaderCircleButton(
                icon = Icons.Default.Tune,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    minPrice = if (minPrice == 0) 15000 else 0
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Быстрые фильтры
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    "Удалённо",
                    isRemoteOnly,
                    { isRemoteOnly = !isRemoteOnly}
                )
            }
            item {
                FilterChip(
                    "До 2 недель",
                    isShortTerm,
                    { isShortTerm = !isShortTerm}
                )
            }
            if (minPrice > 0) {
                item {
                    FilterChip(
                        "от $minPrice ₽",
                        true,
                        { minPrice = 0}
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Недавние поиски",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Список истории
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentSearches) { text ->
                HistoryItem(
                    text = text,
                    onDelete = { recentSearches.remove(text) },
                    onSelect = { searchQuery = text }
                )
            }
        }
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable {onClick},
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HistoryItem(text: String, onSelect: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { onSelect() }
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

        }
    }
}