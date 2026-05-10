package com.example.studentgigs.view.OnApp.components

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
val allGigs = listOf(
    Gig("Landing Page для стартапа", "TechStart", "2 недели", "Удалённо", "15000", listOf("React", "Figma"), isNew = true, iconEmoji = "🚀"),
    Gig("Анализ данных пользователей", "DataCorp", "3 недели", "Москва", "20000", listOf("Python", "SQL"), isSaved = true, iconEmoji = "📊"),
    Gig("Мобильное приложение", "AppStudio", "1 месяц", "Удалённо", "45000", listOf("Kotlin", "Android"), isNew = true, iconEmoji = "📱"),
    Gig("UI/UX дизайн интернет-магазина", "ShopDesign", "2 недели", "Удалённо", "25000", listOf("Figma", "UI/UX"), iconEmoji = "🎨"),
    Gig("Backend разработка API", "ServerPro", "3 недели", "Санкт-Петербург", "35000", listOf("Python", "Django"), iconEmoji = "⚙️"),
    Gig("React разработка панели", "AdminPanel", "2 недели", "Удалённо", "30000", listOf("React", "TypeScript"), isNew = true, iconEmoji = "💻"),
    Gig("Телеграм бот для бизнеса", "BotMakers", "1 неделя", "Удалённо", "12000", listOf("Python", "Telegram"), iconEmoji = "🤖"),
    Gig("Стажировка Frontend", "BigTech", "3 месяца", "Москва", "40000", listOf("JavaScript", "React"), isNew = true, iconEmoji = "🎓"),
    Gig("Парсинг данных", "DataMine", "1 неделя", "Удалённо", "8000", listOf("Python", "Scrapy"), iconEmoji = "🔍"),
    Gig("Верстка email рассылки", "MailPro", "3 дня", "Удалённо", "5000", listOf("HTML", "CSS"), iconEmoji = "✉️")
)

@Composable
fun SearchScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val recentSearches = remember {
        mutableStateListOf(
            "React разработка",
            "UI/UX дизайн",
            "Python",
            "Стажировка"
        )
    }

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
    var searchResults by remember { mutableStateOf<List<Gig>>(emptyList()) }

    fun addToSearchHistory(query: String) {
        if (query.isNotBlank() && query.length >= 2 && !recentSearches.contains(query)) {
            recentSearches.add(0, query)
            if (recentSearches.size > 10) {
                recentSearches.removeLast()
            }
        }
    }

    fun performSearch(query: String) {
        if (query.isBlank() && !filters.hasActiveFilters()) {
            isSearching = false
            searchResults = emptyList()
            return
        }

        isSearching = true
        val queryLower = query.lowercase().trim()

        searchResults = allGigs.filter { gig ->
            val matchesQuery = query.isBlank() ||
                    gig.title.lowercase().startsWith(queryLower) ||
                    gig.title.lowercase().contains(queryLower) ||
                    gig.company.lowercase().startsWith(queryLower) ||
                    gig.tags.any { it.lowercase().startsWith(queryLower) } ||
                    gig.tags.any { it.lowercase().contains(queryLower) }

            val matchesLocation = when {
                filters.isRemote && filters.isOffice -> true // Оба выбраны = любая
                filters.isRemote -> gig.location == "Удалённо"
                filters.isOffice -> gig.location != "Удалённо"
                else -> true
            }

            val isShort = gig.duration.contains("неделя") ||
                    gig.duration.contains("недели") ||
                    gig.duration.contains("дня") ||
                    gig.duration.contains("дней")
            val isMedium = gig.duration.contains("2 недели") ||
                    gig.duration.contains("3 недели") ||
                    gig.duration.contains("4 недели")
            val isLong = gig.duration.contains("месяц")

            val matchesDuration = when {
                !filters.isShortTerm && !filters.isMediumTerm && !filters.isLongTerm -> true
                else -> (filters.isShortTerm && isShort && !isMedium) ||
                        (filters.isMediumTerm && isMedium) ||
                        (filters.isLongTerm && isLong)
            }

            val gigPrice = gig.price.replace(" ", "").toIntOrNull() ?: 0
            val matchesPrice = filters.minPrice == 0 || gigPrice >= filters.minPrice

            val matchesNew = !filters.isNewOnly || gig.isNew

            val matchesTags = filters.selectedTags.isEmpty() ||
                    gig.tags.any { it in filters.selectedTags }

            val matchesCities = filters.selectedCities.isEmpty() ||
                    gig.location in filters.selectedCities

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
                    performSearch(searchQuery)
                    addToSearchHistory(searchQuery)
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
                    items(searchResults) { gig ->
                        SearchResultCard(gig = gig)
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
                        TextButton(onClick = { recentSearches.clear() }) {
                            Text(
                                text = "Очистить всё",
                                style = MaterialTheme.typography.bodyMedium,
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
                            items = recentSearches.toList(),
                            key = { it }
                        ) { text ->
                            HistoryItem(
                                text = text,
                                onDelete = { recentSearches.remove(text) },
                                onSelect = {
                                    searchQuery = text
                                    performSearch(text)
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
fun SearchResultCard(gig: Gig) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                    Text(gig.iconEmoji, style = MaterialTheme.typography.headlineSmall)
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = gig.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (gig.isNew) {
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
                        text = gig.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoRowItem(Icons.Outlined.Schedule, gig.duration)
                InfoRowItem(Icons.Outlined.Place, gig.location)
                InfoRowItem(
                    Icons.Outlined.CurrencyRuble,
                    formatPrice(gig.price),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gig.tags.forEach { tag ->
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