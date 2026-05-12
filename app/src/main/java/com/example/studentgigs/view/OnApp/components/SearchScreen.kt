package com.example.studentgigs.view.OnApp.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CurrencyRuble
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studentgigs.data.model.LocationType
import com.example.studentgigs.data.model.Task
import com.example.studentgigs.view.OnApp.HeaderCircleButton
import com.example.studentgigs.view.OnApp.Gig
import com.example.studentgigs.view.OnApp.PillTextField

// Класс для хранения состояния фильтров
data class SearchFilters(
    val isRemote: Boolean = false,
    val isOffice: Boolean = false,
    val isShortTerm: Boolean = false,      // До 2 недель
    val isMediumTerm: Boolean = false,     // 2-4 недели
    val isLongTerm: Boolean = false,       // Более месяца
    val minPrice: Int = 0,
    val isNewOnly: Boolean = false,
    val selectedTags: Set<String> = emptySet(),
    val selectedCities: Set<String> = emptySet()
) {
    fun hasActiveFilters(): Boolean {
        return isRemote || isOffice || isShortTerm || isMediumTerm || isLongTerm ||
                minPrice > 0 || isNewOnly || selectedTags.isNotEmpty() || selectedCities.isNotEmpty()
    }

    fun getActiveFilterChips(): List<FilterChipData> {
        val chips = mutableListOf<FilterChipData>()

        if (isRemote) chips.add(FilterChipData("Удалённо", FilterType.REMOTE))
        if (isOffice) chips.add(FilterChipData("В офисе", FilterType.OFFICE))
        if (isShortTerm) chips.add(FilterChipData("До 2 недель", FilterType.SHORT_TERM))
        if (isMediumTerm) chips.add(FilterChipData("2-4 недели", FilterType.MEDIUM_TERM))
        if (isLongTerm) chips.add(FilterChipData("Более месяца", FilterType.LONG_TERM))
        if (minPrice > 0) chips.add(FilterChipData("от ${formatPrice(minPrice.toString())} ₽", FilterType.MIN_PRICE))
        if (isNewOnly) chips.add(FilterChipData("Только новые", FilterType.NEW_ONLY))
        selectedTags.forEach { tag ->
            chips.add(FilterChipData(tag, FilterType.TAG, tag))
        }
        selectedCities.forEach { city ->
            chips.add(FilterChipData(city, FilterType.CITY, city))
        }

        return chips
    }
}

data class FilterChipData(
    val label: String,
    val type: FilterType,
    val value: String = ""
)

enum class FilterType {
    REMOTE, OFFICE, SHORT_TERM, MEDIUM_TERM, LONG_TERM, MIN_PRICE, NEW_ONLY, TAG, CITY
}

// Доступные теги и города для фильтрации
val availableTags = listOf("React", "Python", "Kotlin", "Figma", "UI/UX", "JavaScript", "TypeScript", "SQL", "Django", "Android")
val availableCities = listOf("Москва", "Санкт-Петербург", "Удалённо")

// Данные для поиска - все доступные гиги

@Composable
fun SearchScreen(
    tasks: List<Task>,
    onBack: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val context = androidx.compose.ui.platform.LocalContext.current
    val historyManager = remember { SearchHistoryManager(context) }

    var recentSearches by remember { mutableStateOf(historyManager.getHistory()) }

    var filters by remember { mutableStateOf(SearchFilters()) }
    var showFiltersDialog by remember { mutableStateOf(false) }




    fun removeFilter(chipData: FilterChipData) {
        filters = when (chipData.type) {
            FilterType.REMOTE -> filters.copy(isRemote = false)
            FilterType.OFFICE -> filters.copy(isOffice = false)
            FilterType.SHORT_TERM -> filters.copy(isShortTerm = false)
            FilterType.MEDIUM_TERM -> filters.copy(isMediumTerm = false)
            FilterType.LONG_TERM -> filters.copy(isLongTerm = false)
            FilterType.MIN_PRICE -> filters.copy(minPrice = 0)
            FilterType.NEW_ONLY -> filters.copy(isNewOnly = false)
            FilterType.TAG -> filters.copy(selectedTags = filters.selectedTags - chipData.value)
            FilterType.CITY -> filters.copy(selectedCities = filters.selectedCities - chipData.value)
        }
    }

    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Task>>(emptyList()) }


    fun performSearch(query: String) {

//        if (query.isNotBlank()) {
//            historyManager.saveSearch(query)
//            recentSearches = historyManager.getHistory()
//        }

        if (query.isBlank() && !filters.hasActiveFilters()) {
            isSearching = false
            searchResults = emptyList()
            return
        }

        isSearching = true
        val queryLower = query.lowercase().trim()
        val currentTime = System.currentTimeMillis()

        searchResults = tasks.filter { task ->

            // 1. Поиск по тексту
            val matchesQuery = query.isBlank() ||
                    task.title.lowercase().contains(queryLower) ||
                    task.employerName.lowercase().contains(queryLower) ||
                    task.tags.any { it.lowercase().contains(queryLower) }

            // 2. Локация (используем enum LocationType)
            val matchesLocation = when {
                filters.isRemote && filters.isOffice -> true
                filters.isRemote -> task.locationType == LocationType.REMOTE
                filters.isOffice -> task.locationType == LocationType.OFFICE || task.locationType == LocationType.HYBRID
                else -> true
            }

            // 3. Длительность (оставляем твою логику поиска по строке)
            val durationLower = task.duration?.lowercase() ?: ""
            val isShort = durationLower.contains("недел") || durationLower.contains("дн") || durationLower.contains("дня")
            val isMedium = durationLower.contains("2 недели") || durationLower.contains("3 недели") || durationLower.contains("4 недели")
            val isLong = durationLower.contains("месяц")

            val matchesDuration = when {
                !filters.isShortTerm && !filters.isMediumTerm && !filters.isLongTerm -> true
                else -> (filters.isShortTerm && isShort && !isMedium) ||
                        (filters.isMediumTerm && isMedium) ||
                        (filters.isLongTerm && isLong)
            }

            // 4. Оплата (очищаем строку от букв и пробелов, оставляем только цифры)
            val taskPrice = task.price.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
            val matchesPrice = filters.minPrice == 0 || taskPrice >= filters.minPrice

            // 5. Новизна (проверка на 15 минут = 900_000 мс)
            val isNewTask = (currentTime - task.createdAt) < 900_000L
            val matchesNew = !filters.isNewOnly || isNewTask

            // 6. Теги
            val matchesTags = filters.selectedTags.isEmpty() ||
                    task.tags.any { it in filters.selectedTags }

            // 7. Города (используем поле location)
            val matchesCities = filters.selectedCities.isEmpty() ||
                    task.location in filters.selectedCities

            // Итоговая проверка
            matchesQuery && matchesLocation && matchesDuration &&
                    matchesPrice && matchesNew && matchesTags && matchesCities
        }
    }

    LaunchedEffect(filters) {
        performSearch(searchQuery)
    }

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

            SearchInputWithAction(
                search = searchQuery,
                onSearchChange = {
                    searchQuery = it
                    if (it.length >= 2) {
                        performSearch(it)
                    } else if (it.isEmpty()) {
                        isSearching = false
                        searchResults = emptyList()
                    }
                },
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        historyManager.saveSearch(searchQuery)
                        recentSearches = historyManager.getHistory()
                    }
                    performSearch(searchQuery)
                    focusManager.clearFocus()
                },
                modifier = Modifier.weight(1f)
            )

            HeaderCircleButton(
                icon = Icons.Default.Tune,
                backgroundColor = if (filters.hasActiveFilters()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                iconColor = if (filters.hasActiveFilters()) Color.Black else MaterialTheme.colorScheme.onSurface,
                onClick = { showFiltersDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val activeChips = filters.getActiveFilterChips()

        if (activeChips.isEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    QuickAddFilterChip(
                        text = "Удалённо",
                        onClick = { filters = filters.copy(isRemote = true) }
                    )
                }
                item {
                    QuickAddFilterChip(
                        text = "До 2 недель",
                        onClick = { filters = filters.copy(isShortTerm = true) }
                    )
                }
                item {
                    QuickAddFilterChip(
                        text = "Только новые",
                        onClick = { filters = filters.copy(isNewOnly = true) }
                    )
                }
                item {
                    QuickAddFilterChip(
                        text = "Все фильтры",
                        onClick = { showFiltersDialog = true },
                        isHighlighted = true
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeChips, key = { "${it.type}_${it.value}" }) { chip ->
                    ActiveFilterChip(
                        text = chip.label,
                        onRemove = { removeFilter(chip) }
                    )
                }
                item {
                    QuickAddFilterChip(
                        text = "+",
                        onClick = { showFiltersDialog = true },
                        isHighlighted = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = isSearching && searchResults.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Результаты поиска",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${searchResults.size} найдено",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { task ->
                        SearchResultCard(
                            task = task,
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    historyManager.saveSearch(searchQuery)
                                    recentSearches = historyManager.getHistory()
                                }
                                onTaskClick(task)
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isSearching && searchResults.isEmpty() && searchQuery.length >= 2,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "😕",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ничего не найдено",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Попробуйте изменить запрос или фильтры",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = !isSearching || searchQuery.length < 2,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Недавние поиски",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (recentSearches.isNotEmpty()) {
                        TextButton(onClick = {
                            historyManager.clearHistory()
                            recentSearches = emptyList()
                        }) {
                            Text(
                                text = "Очистить всё",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (recentSearches.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔍",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "История поиска пуста",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            recentSearches
                        ) { text ->
                            HistoryItem(
                                text = text,
                                onDelete = {
                                    historyManager.deleteSearch(text)
                                    recentSearches = historyManager.getHistory()
                                },
                                onSelect = {
                                    searchQuery = text
                                    historyManager.saveSearch(text)
                                    recentSearches = historyManager.getHistory()

                                    performSearch(text)
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFiltersDialog) {
        FullFilterDialog(
            currentFilters = filters,
            onDismiss = { showFiltersDialog = false },
            onApply = { newFilters ->
                filters = newFilters
                showFiltersDialog = false
            },
            onReset = {
                filters = SearchFilters()
            }
        )
    }
}

@Composable
fun SearchInputWithAction(
    search: String,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    PillTextField(
        value = search,
        onValueChange = onSearchChange,
        modifier = modifier,
        placeholder = "Поиск прое��тов...",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (search.isNotEmpty()) {
            {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        )
    )
}

@Composable
fun QuickAddFilterChip(
    text: String,
    onClick: () -> Unit,
    isHighlighted: Boolean = false
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isHighlighted) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ActiveFilterChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onRemove() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Удалить фильтр",
                modifier = Modifier.size(16.dp),
                tint = Color.Black.copy(alpha = 0.7f)
            )
        }
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

@Composable
fun SearchResultCard(task: Task, onClick: () -> Unit) {
    val currentTime = System.currentTimeMillis()
    val isNew = (currentTime - task.createdAt) < 900_000L

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // <-- Добавили клик
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(task.iconEmoji.ifEmpty { "📋" }, style = MaterialTheme.typography.headlineSmall)
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isNew) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            ) {
                                Text(
                                    "Новое",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    Text(
                        text = task.employerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val displayCity = task.location.split(",").first().trim().ifEmpty { "Удаленно" }

                InfoRowItem(Icons.Outlined.Schedule, task.duration ?: "Не указано")
                InfoRowItem(Icons.Outlined.LocationOn, displayCity)
                InfoRowItem(
                    Icons.Outlined.CurrencyRuble,
                    formatPrice(task.price),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Рендер тегов
            if (task.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    task.tags.take(3).forEach { tag -> // берем максимум 3 тега, чтобы не ломать верстку
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
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
fun InfoRowItem(
    icon: ImageVector,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullFilterDialog(
    currentFilters: SearchFilters,
    onDismiss: () -> Unit,
    onApply: (SearchFilters) -> Unit,
    onReset: () -> Unit
) {
    var tempFilters by remember { mutableStateOf(currentFilters) }

    val priceOptions = listOf(
        0 to "Любая",
        5000 to "от 5 000 ₽",
        10000 to "от 10 000 ₽",
        20000 to "от 20 000 ₽",
        30000 to "от 30 000 ₽",
        50000 to "от 50 000 ₽"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Фильтры",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    tempFilters = SearchFilters()
                    onReset()
                }) {
                    Text(
                        "Сбросить",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Секция: Тип работы
                item {
                    FilterSection(
                        title = "Тип работы",
                        icon = Icons.Outlined.Home
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterToggleChip(
                                text = "Удалённо",
                                isSelected = tempFilters.isRemote,
                                onClick = { tempFilters = tempFilters.copy(isRemote = !tempFilters.isRemote) }
                            )
                            FilterToggleChip(
                                text = "В офисе",
                                isSelected = tempFilters.isOffice,
                                onClick = { tempFilters = tempFilters.copy(isOffice = !tempFilters.isOffice) }
                            )
                        }
                    }
                }

                // Секция: Длительность
                item {
                    FilterSection(
                        title = "Длительность",
                        icon = Icons.Outlined.AccessTime
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterToggleChip(
                                text = "До 2 недель",
                                isSelected = tempFilters.isShortTerm,
                                onClick = { tempFilters = tempFilters.copy(isShortTerm = !tempFilters.isShortTerm) }
                            )
                            FilterToggleChip(
                                text = "2-4 недели",
                                isSelected = tempFilters.isMediumTerm,
                                onClick = { tempFilters = tempFilters.copy(isMediumTerm = !tempFilters.isMediumTerm) }
                            )
                            FilterToggleChip(
                                text = "Месяц+",
                                isSelected = tempFilters.isLongTerm,
                                onClick = { tempFilters = tempFilters.copy(isLongTerm = !tempFilters.isLongTerm) }
                            )
                        }
                    }
                }

                // Секция: Оплата
                item {
                    FilterSection(
                        title = "Минимальная оплата",
                        icon = Icons.Outlined.AttachMoney
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            priceOptions.chunked(3).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { (price, label) ->
                                        FilterToggleChip(
                                            text = label,
                                            isSelected = tempFilters.minPrice == price,
                                            onClick = { tempFilters = tempFilters.copy(minPrice = price) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Секция: Только новые
                item {
                    FilterSection(
                        title = "Дополнительно",
                        icon = Icons.Outlined.Star
                    ) {
                        FilterToggleChip(
                            text = "Только новые",
                            isSelected = tempFilters.isNewOnly,
                            onClick = { tempFilters = tempFilters.copy(isNewOnly = !tempFilters.isNewOnly) }
                        )
                    }
                }

                // Секция: Навыки/Теги
                item {
                    FilterSection(
                        title = "Навыки",
                        icon = Icons.Outlined.Category
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableTags.chunked(4).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { tag ->
                                        FilterToggleChip(
                                            text = tag,
                                            isSelected = tag in tempFilters.selectedTags,
                                            onClick = {
                                                tempFilters = if (tag in tempFilters.selectedTags) {
                                                    tempFilters.copy(selectedTags = tempFilters.selectedTags - tag)
                                                } else {
                                                    tempFilters.copy(selectedTags = tempFilters.selectedTags + tag)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Секция: Города
                item {
                    FilterSection(
                        title = "Город",
                        icon = Icons.Outlined.LocationOn
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            availableCities.forEach { city ->
                                FilterToggleChip(
                                    text = city,
                                    isSelected = city in tempFilters.selectedCities,
                                    onClick = {
                                        tempFilters = if (city in tempFilters.selectedCities) {
                                            tempFilters.copy(selectedCities = tempFilters.selectedCities - city)
                                        } else {
                                            tempFilters.copy(selectedCities = tempFilters.selectedCities + city)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка применить
            Button(
                onClick = { onApply(tempFilters) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Применить фильтры",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        content()
    }
}

@Composable
fun FilterToggleChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = if (!isSelected) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ) else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

fun formatPrice(price: String): String {
    val num = price.replace(" ", "").toIntOrNull() ?: return price
    return "%,d".format(num).replace(",", " ")
}

class SearchHistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

    fun getHistory(): List<String> {
        val historyString = prefs.getString("search_history", "") ?: ""
        return if (historyString.isEmpty()) emptyList() else historyString.split("|")
    }

    fun saveSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        val currentHistory = getHistory().toMutableList()
        currentHistory.remove(trimmed)
        currentHistory.add(0, trimmed)

        val limitedHistory = currentHistory.take(10)
        prefs.edit().putString("search_history", limitedHistory.joinToString("|")).apply()
    }

    fun deleteSearch(query: String) {
        val currentHistory = getHistory().toMutableList()
        currentHistory.remove(query)
        prefs.edit().putString("search_history", currentHistory.joinToString("|")).apply()
    }

    fun clearHistory() {
        prefs.edit().remove("search_history").apply()
    }
}