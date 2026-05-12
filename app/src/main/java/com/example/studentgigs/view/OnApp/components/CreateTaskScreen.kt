package com.example.studentgigs.view.OnApp.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentgigs.data.model.*
import com.example.studentgigs.view.OnApp.HeaderCircleButton
import com.example.studentgigs.viewmodel.TaskViewModel
import kotlinx.coroutines.delay

enum class CreateTaskStep {
    SELECT_TYPE,    // Выбор типа
    FILL_DETAILS,   // Заполнение данных
    SUCCESS         // Успех
}

@Composable
fun CreateTaskScreen(
    taskViewModel: TaskViewModel,
    employerId: Long,
    onBack: () -> Unit,
    onTaskCreated: () -> Unit
) {
    val uiState by taskViewModel.uiState.collectAsState()
    var currentStep by remember { mutableStateOf(CreateTaskStep.SELECT_TYPE) }
    var selectedType by remember { mutableStateOf<TaskType?>(null) }

    // Поля формы
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedLocationType by remember { mutableStateOf(LocationType.REMOTE) }
    var selectedPriceType by remember { mutableStateOf(PriceType.FIXED) }
    var tags by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var benefits by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("📋") }

    // Для вакансии
    var selectedEmploymentType by remember { mutableStateOf<EmploymentType?>(null) }
    var schedule by remember { mutableStateOf("") }

    // Для услуги
    var serviceCategory by remember { mutableStateOf("") }

    // Слушаем успех создания
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            currentStep = CreateTaskStep.SUCCESS
            delay(2000)
            taskViewModel.clearCreateSuccess()
            onTaskCreated()
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
                        onClick = {
                            when (currentStep) {
                                CreateTaskStep.SELECT_TYPE -> onBack()
                                CreateTaskStep.FILL_DETAILS -> currentStep = CreateTaskStep.SELECT_TYPE
                                CreateTaskStep.SUCCESS -> onBack()
                            }
                        }
                    )
                    Text(
                        text = when (currentStep) {
                            CreateTaskStep.SELECT_TYPE -> "Новое задание"
                            CreateTaskStep.FILL_DETAILS -> when (selectedType) {
                                TaskType.SERVICE -> "Новая услуга"
                                TaskType.VACANCY -> "Новая вакансия"
                                TaskType.TASK -> "Новое задание"
                                null -> "Новое задание"
                            }
                            CreateTaskStep.SUCCESS -> "Готово!"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            label = "step_animation"
        ) { step ->
            when (step) {
                CreateTaskStep.SELECT_TYPE -> {
                    SelectTypeContent(
                        modifier = Modifier.padding(innerPadding),
                        onTypeSelected = { type ->
                            selectedType = type
                            selectedEmoji = when (type) {
                                TaskType.SERVICE -> "🛠️"
                                TaskType.VACANCY -> "💼"
                                TaskType.TASK -> "📋"
                            }
                            currentStep = CreateTaskStep.FILL_DETAILS
                        }
                    )
                }
                CreateTaskStep.FILL_DETAILS -> {
                    FillDetailsContent(
                        modifier = Modifier.padding(innerPadding),
                        type = selectedType!!,
                        title = title,
                        onTitleChange = { title = it },
                        description = description,
                        onDescriptionChange = { description = it },
                        price = price,
                        onPriceChange = { price = it },
                        duration = duration,
                        onDurationChange = { duration = it },
                        location = location,
                        onLocationChange = { location = it },
                        selectedLocationType = selectedLocationType,
                        onLocationTypeChange = { selectedLocationType = it },
                        selectedPriceType = selectedPriceType,
                        onPriceTypeChange = { selectedPriceType = it },
                        tags = tags,
                        onTagsChange = { tags = it },
                        requirements = requirements,
                        onRequirementsChange = { requirements = it },
                        benefits = benefits,
                        onBenefitsChange = { benefits = it },
                        selectedEmoji = selectedEmoji,
                        onEmojiChange = { selectedEmoji = it },
                        selectedEmploymentType = selectedEmploymentType,
                        onEmploymentTypeChange = { selectedEmploymentType = it },
                        schedule = schedule,
                        onScheduleChange = { schedule = it },
                        serviceCategory = serviceCategory,
                        onServiceCategoryChange = { serviceCategory = it },
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onSubmit = {
                            taskViewModel.createTask(
                                employerId = employerId,
                                type = selectedType!!,
                                title = title,
                                description = description,
                                price = price,
                                requirements = requirements.split("\n").filter { it.isNotBlank() },
                                benefits = benefits.split("\n").filter { it.isNotBlank() },
                                tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                priceType = selectedPriceType,
                                duration = duration,
                                location = location,
                                locationType = selectedLocationType,
                                iconEmoji = selectedEmoji,
                                employmentType = selectedEmploymentType,
                                schedule = schedule.ifBlank { null },
                                serviceCategory = serviceCategory.ifBlank { null }
                            )
                        }
                    )
                }
                CreateTaskStep.SUCCESS -> {
                    SuccessContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun SelectTypeContent(
    modifier: Modifier = Modifier,
    onTypeSelected: (TaskType) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Что вы хотите разместить?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Выберите тип публикации для вашего предложения",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Карточки выбора типа
        TaskTypeCard(
            emoji = "🛠️",
            title = "Услуга",
            description = "Разовая работа с фиксированным результатом",
            examples = "Создание сайта, дизайн логотипа, перевод текста",
            onClick = { onTypeSelected(TaskType.SERVICE) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TaskTypeCard(
            emoji = "💼",
            title = "Вакансия",
            description = "Постоянная или временная работа в компании",
            examples = "Стажер-маркетолог, Junior разработчик",
            onClick = { onTypeSelected(TaskType.VACANCY) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TaskTypeCard(
            emoji = "📋",
            title = "Задание",
            description = "Конкретная задача с четким ТЗ",
            examples = "Анализ данных, написание статьи, исследование",
            onClick = { onTypeSelected(TaskType.TASK) }
        )
    }
}

@Composable
private fun TaskTypeCard(
    emoji: String,
    title: String,
    description: String,
    examples: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = examples,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FillDetailsContent(
    modifier: Modifier = Modifier,
    type: TaskType,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    selectedLocationType: LocationType,
    onLocationTypeChange: (LocationType) -> Unit,
    selectedPriceType: PriceType,
    onPriceTypeChange: (PriceType) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    requirements: String,
    onRequirementsChange: (String) -> Unit,
    benefits: String,
    onBenefitsChange: (String) -> Unit,
    selectedEmoji: String,
    onEmojiChange: (String) -> Unit,
    selectedEmploymentType: EmploymentType?,
    onEmploymentTypeChange: (EmploymentType?) -> Unit,
    schedule: String,
    onScheduleChange: (String) -> Unit,
    serviceCategory: String,
    onServiceCategoryChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onSubmit: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // Основная информация
        SectionHeader(title = "Основная информация")

        Spacer(modifier = Modifier.height(12.dp))

        // Выбор эмодзи
        Text(
            text = "Иконка",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        EmojiSelector(
            selectedEmoji = selectedEmoji,
            onEmojiSelected = onEmojiChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Название
        FormTextField(
            value = title,
            onValueChange = onTitleChange,
            label = "Название",
            placeholder = when (type) {
                TaskType.SERVICE -> "Например: Разработка мобильного приложения"
                TaskType.VACANCY -> "Например: Junior iOS разработчик"
                TaskType.TASK -> "Например: Анализ конкурентов"
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Описание
        FormTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Описание",
            placeholder = "Подробно опишите, что нужно сделать",
            singleLine = false,
            minLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Оплата
        SectionHeader(title = "Оплата")

        Spacer(modifier = Modifier.height(12.dp))

        // Тип оплаты
        Text(
            text = "Тип оплаты",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        PriceTypeSelector(
            selectedType = selectedPriceType,
            onTypeSelected = onPriceTypeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        FormTextField(
            value = price,
            onValueChange = onPriceChange,
            label = when (selectedPriceType) {
                PriceType.FIXED -> "Сумма (руб.)"
                PriceType.HOURLY -> "Ставка в час (руб.)"
                PriceType.NEGOTIABLE -> "Примерный бюджет"
                PriceType.PER_PROJECT -> "Бюджет проекта"
            },
            placeholder = "15000",
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Условия работы
        SectionHeader(title = "Условия работы")

        Spacer(modifier = Modifier.height(12.dp))

        // Формат работы
        Text(
            text = "Формат работы",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LocationTypeSelector(
            selectedType = selectedLocationType,
            onTypeSelected = onLocationTypeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedLocationType != LocationType.REMOTE) {
            FormTextField(
                value = location,
                onValueChange = onLocationChange,
                label = "Адрес / Город",
                placeholder = "Москва, ул. Примерная, 1"
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        FormTextField(
            value = duration,
            onValueChange = onDurationChange,
            label = "Срок выполнения",
            placeholder = "2 недели"
        )

        // Дополнительные поля для вакансии
        if (type == TaskType.VACANCY) {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Тип занятости")

            Spacer(modifier = Modifier.height(12.dp))
            EmploymentTypeSelector(
                selectedType = selectedEmploymentType,
                onTypeSelected = onEmploymentTypeChange
            )

            Spacer(modifier = Modifier.height(16.dp))
            FormTextField(
                value = schedule,
                onValueChange = onScheduleChange,
                label = "График работы",
                placeholder = "Гибкий график, 20 часов в неделю"
            )
        }

        // Дополнительные поля для услуги
        if (type == TaskType.SERVICE) {
            Spacer(modifier = Modifier.height(16.dp))
            FormTextField(
                value = serviceCategory,
                onValueChange = onServiceCategoryChange,
                label = "Категория услуги",
                placeholder = "Веб-разработка"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Навыки и теги
        SectionHeader(title = "Навыки и теги")

        Spacer(modifier = Modifier.height(12.dp))

        FormTextField(
            value = tags,
            onValueChange = onTagsChange,
            label = "Теги (через запятую)",
            placeholder = "React, TypeScript, Figma"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Требования
        SectionHeader(title = "Требования")

        Spacer(modifier = Modifier.height(12.dp))

        FormTextField(
            value = requirements,
            onValueChange = onRequirementsChange,
            label = "Требования (каждое с новой строки)",
            placeholder = "Опыт работы с React\nЗнание TypeScript\nПонимание UX/UI",
            singleLine = false,
            minLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Преимущества
        SectionHeader(title = "Что вы предлагаете")

        Spacer(modifier = Modifier.height(12.dp))

        FormTextField(
            value = benefits,
            onValueChange = onBenefitsChange,
            label = "Преимущества (каждое с новой строки)",
            placeholder = "Гибкий график\nУдаленная работа\nОбучение",
            singleLine = false,
            minLines = 3
        )

        // Ошибка
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

        // Кнопка публикации
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = title.isNotBlank() && description.isNotBlank() && price.isNotBlank() && !isLoading,
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
                Icon(
                    imageVector = Icons.Outlined.Publish,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Опубликовать",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SuccessContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
            text = "Задание опубликовано!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ваше задание теперь доступно для студентов",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun EmojiSelector(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf("📋", "💻", "🎨", "📊", "📱", "🚀", "💡", "🛠️", "📝", "🎯")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        emojis.forEach { emoji ->
            val isSelected = emoji == selectedEmoji
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onEmojiSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun PriceTypeSelector(
    selectedType: PriceType,
    onTypeSelected: (PriceType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PriceType.values().forEach { type ->
            val isSelected = type == selectedType
            val label = when (type) {
                PriceType.FIXED -> "Фикс"
                PriceType.HOURLY -> "Час"
                PriceType.NEGOTIABLE -> "Договор"
                PriceType.PER_PROJECT -> "Проект"
            }

            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun LocationTypeSelector(
    selectedType: LocationType,
    onTypeSelected: (LocationType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LocationType.values().forEach { type ->
            val isSelected = type == selectedType
            val label = when (type) {
                LocationType.REMOTE -> "Удаленно"
                LocationType.OFFICE -> "Офис"
                LocationType.HYBRID -> "Гибрид"
            }
            val icon = when (type) {
                LocationType.REMOTE -> Icons.Outlined.Home
                LocationType.OFFICE -> Icons.Outlined.Business
                LocationType.HYBRID -> Icons.Outlined.SwapHoriz
            }

            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                label = { Text(label) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun EmploymentTypeSelector(
    selectedType: EmploymentType?,
    onTypeSelected: (EmploymentType?) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmploymentType.values().take(2).forEach { type ->
                val isSelected = type == selectedType
                val label = when (type) {
                    EmploymentType.FULL_TIME -> "Полная"
                    EmploymentType.PART_TIME -> "Частичная"
                    EmploymentType.INTERNSHIP -> "Стажировка"
                    EmploymentType.PROJECT -> "Проект"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeSelected(if (isSelected) null else type) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmploymentType.values().drop(2).forEach { type ->
                val isSelected = type == selectedType
                val label = when (type) {
                    EmploymentType.FULL_TIME -> "Полная"
                    EmploymentType.PART_TIME -> "Частичная"
                    EmploymentType.INTERNSHIP -> "Стажировка"
                    EmploymentType.PROJECT -> "Проект"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeSelected(if (isSelected) null else type) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}
