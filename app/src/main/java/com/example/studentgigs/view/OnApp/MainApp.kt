package com.example.studentgigs.view.OnApp

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CurrencyRuble
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.studentgigs.data.model.Application
import com.example.studentgigs.data.model.ApplicationStatus
import com.example.studentgigs.data.model.UserRole
import com.example.studentgigs.data.model.VerificationStatus
import com.example.studentgigs.view.OnApp.components.CreateTaskScreen
import com.example.studentgigs.view.OnApp.components.EmployerTaskApplicationsScreen
import com.example.studentgigs.view.OnApp.components.EmployerTasksScreen
import com.example.studentgigs.view.OnApp.components.NotificationScreen
import com.example.studentgigs.view.OnApp.components.ProfileScreen
import com.example.studentgigs.view.OnApp.components.SearchScreen
import com.example.studentgigs.view.OnApp.components.VerificationScreen
import com.example.studentgigs.view.OnApp.components.StudentMyProjectsScreen
import com.example.studentgigs.view.OnApp.components.StudentProfileViewScreen
import com.example.studentgigs.viewmodel.ApplicationViewModel
import com.example.studentgigs.viewmodel.AuthViewModel
import com.example.studentgigs.viewmodel.TaskViewModel

data class Category(
    val name: String,
    val icon: String
)
data class Gig(
    val title: String,
    val company: String,
    val duration: String,
    val location: String,
    val price: String,
    val tags: List<String>,
    val isNew: Boolean = false,
    val isSaved: Boolean = false,
    val iconEmoji: String
)

sealed class BottomNavItem(val title: String, val icon: ImageVector, val route: String) {
    object Home    : BottomNavItem("Главная",  Icons.Rounded.Home,    "home")
    object Search  : BottomNavItem("Поиск",    Icons.Rounded.Search,  "search")
    object Saved   : BottomNavItem("Избранное",Icons.Rounded.Bookmark,"saved")
    object Profile : BottomNavItem("Профиль",  Icons.Rounded.Person,  "profile")
}

@Composable
fun MainApp(
    innerPadding: PaddingValues,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    applicationViewModel: ApplicationViewModel
) {
    var currentRoute by remember { mutableStateOf("home") }
    var selectedTask by remember { mutableStateOf<com.example.studentgigs.data.model.Task?>(null) }

    // ── НОВОЕ: флаг "работодатель смотрит своё задание" и выбранный отклик
    var isViewingOwnTask    by remember { mutableStateOf(false) }
    var selectedApplication by remember { mutableStateOf<com.example.studentgigs.data.model.Application?>(null) }

    val uiState by authViewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser
    val userName  = currentUser?.shortName ?: "гость"
    val isEmployer = currentUser?.role == UserRole.EMPLOYER
    val isStudent  = currentUser?.role == UserRole.STUDENT
    val isVerified = currentUser?.verificationStatus == VerificationStatus.VERIFIED

    val taskUiState by taskViewModel.uiState.collectAsState()
    val appUiState  by applicationViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        taskViewModel.loadAllActiveTasks()
    }

    LaunchedEffect(currentUser?.id) {
        if (isStudent && currentUser != null) {
            applicationViewModel.loadApplications(currentUser.id)
        }
    }

    LaunchedEffect(taskUiState.createSuccess) {
        if (taskUiState.createSuccess) {
            taskViewModel.loadAllActiveTasks()
            taskViewModel.clearCreateSuccess()
        }
    }

    // ── НОВОЕ: загружаем отклики когда входим на экран списка откликов
    LaunchedEffect(currentRoute) {
        if (currentRoute == "employer_task_applications") {
            val task = selectedTask ?: return@LaunchedEffect
            val user = currentUser ?: return@LaunchedEffect
            applicationViewModel.loadTaskApplications(task.id, user.id)
        }
    }

    BackHandler(enabled = currentRoute != "home") {
        when (currentRoute) {
            "student_profile_view"          -> currentRoute = "employer_task_applications"
            "employer_task_applications"    -> currentRoute = "gig_details"
            "gig_details"                   -> {
                val wasOwn = isViewingOwnTask
                isViewingOwnTask = false
                currentRoute = if (wasOwn) "employer_tasks" else "home"
            }
            "employer_tasks"                -> currentRoute = "profile"
            "student_my_projects"           -> currentRoute = "profile"
            else                            -> currentRoute = "home"
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        AnimatedContent(
            targetState = currentRoute,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f)) togetherWith
                        (fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.95f))
            },
            label = "route_transition"
        ) { route ->

            when (route) {
                // ── Главная ────────────────────────────────────────────────
                "home" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        TopContainer(
                            name = userName,
                            onSearchClick = { currentRoute = "search" },
                            onNotificationClick = { currentRoute = "notification" },
                            onProfileClick = { currentRoute = "profile" },
                            isEmployer = isEmployer,
                            isVerified = isVerified,
                            onAddTaskClick = {
                                if (isVerified) currentRoute = "create_task"
                                else currentRoute = "verification"
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MediumContainer(
                            tasks = taskUiState.tasks,
                            onGigClick = { clickedTask ->
                                selectedTask = clickedTask
                                isViewingOwnTask = false   // с главной — не своё задание
                                currentRoute = "gig_details"
                            }
                        )
                    }
                }

                // ── Карточка задания ───────────────────────────────────────
                // ИЗМЕНЕНО: добавлены isEmployerOwner и onGoToTask
                "gig_details" -> {


                    selectedTask?.let { task ->
                        GigScreen(
                            task = task,
                            onBack = {
                                if (isViewingOwnTask) {
                                    currentRoute = "employer_tasks"
                                } else {
                                    isViewingOwnTask = false
                                    currentRoute = "home"
                                }
                            },
                            isStudent = isStudent,
                            isApplied = appUiState.appliedTaskIds.contains(task.id),
                            isApplying = appUiState.applyingTaskId == task.id,
                            errorMessage = appUiState.error,
                            onApply = {
                                currentUser?.let { user ->
                                    applicationViewModel.applyToTask(user.id, task.id)
                                }
                            },
                            onClearError = { applicationViewModel.clearError() },
                            isEmployerOwner = isViewingOwnTask,                        // НОВОЕ
                            onGoToTask = { currentRoute = "employer_task_applications" } // НОВОЕ
                        )
                    }
                }

                // ── Поиск ─────────────────────────────────────────────────
                "search" -> {
                    SearchScreen(
                        tasks = taskUiState.tasks,
                        onBack = { currentRoute = "home" },
                        onTaskClick = { clickedTask ->
                            selectedTask = clickedTask
                            isViewingOwnTask = false
                            currentRoute = "gig_details"
                        }
                    )
                }

                "saved" -> {
                    Text("Сохраненные проекты", modifier = Modifier.padding(16.dp))
                }

                // ── Профиль ───────────────────────────────────────────────
                "profile" -> {
                    val activeCount = taskUiState.tasks.count {
                        it.employerId == currentUser?.id && it.status.toString() == "ACTIVE"
                    }
                    ProfileScreen(
                        authViewModel = authViewModel,
                        activeTasksCount = if (isEmployer) activeCount else appUiState.applications.size,
                        onNavigateToNotifications = { currentRoute = "notification" },
                        onNavigateToVerification  = { currentRoute = "verification" },
                        onNavigateToMyTasks = {
                            if (isStudent) currentRoute = "student_my_projects"
                            else currentRoute = "employer_tasks"
                        }
                    )
                }

                // ── Мои проекты (студент) ─────────────────────────────────
                "student_my_projects" -> {
                    StudentMyProjectsScreen(
                        applicationViewModel = applicationViewModel,
                        onBack = { currentRoute = "profile" },
                        onTaskClick = { clickedTask ->
                            selectedTask = clickedTask
                            isViewingOwnTask = false
                            currentRoute = "gig_details"
                        }
                    )
                }

                // ── Уведомления ───────────────────────────────────────────
                "notification" -> {
                    NotificationScreen(onBack = { currentRoute = "home" })
                }

                // ── Верификация ───────────────────────────────────────────
                "verification" -> {
                    VerificationScreen(
                        authViewModel = authViewModel,
                        onBack = { currentRoute = "profile" },
                        onVerificationComplete = {
                            authViewModel.refreshCurrentUser()
                            currentRoute = "home"
                        }
                    )
                }

                // ── Создать задание ────────────────────────────────────────
                "create_task" -> {
                    currentUser?.let { user ->
                        CreateTaskScreen(
                            taskViewModel = taskViewModel,
                            employerId = user.id,
                            onBack = { currentRoute = "home" },
                            onTaskCreated = { currentRoute = "home" }
                        )
                    }
                }

                // ── Мои задания (работодатель) ────────────────────────────
                // ИЗМЕНЕНО: ставим isViewingOwnTask = true при клике
                "employer_tasks" -> {
                    currentUser?.let { user ->
                        EmployerTasksScreen(
                            tasks = taskUiState.tasks,
                            currentEmployerId = user.id,
                            onBack = { currentRoute = "profile" },
                            onTaskClick = { clickedTask ->
                                selectedTask = clickedTask
                                isViewingOwnTask = true   // ← КЛЮЧЕВОЕ ИЗМЕНЕНИЕ
                                currentRoute = "gig_details"
                            }
                        )
                    }
                }

                // ── НОВЫЙ: Список откликов на задание (работодатель) ──────
                "employer_task_applications" -> {
                    selectedTask?.let { task ->
                        EmployerTaskApplicationsScreen(
                            task = task,
                            applications = appUiState.taskApplications,
                            isLoading = appUiState.isLoadingTaskApplications,
                            errorMessage = appUiState.error,
                            onBack = { currentRoute = "gig_details" },
                            onStudentClick = { application ->
                                selectedApplication = application
                                currentRoute = "student_profile_view"
                            },
                            onClearError = { applicationViewModel.clearError() }
                        )
                    }
                }

                // ── НОВЫЙ: Профиль студента (работодатель смотрит) ────────
                "student_profile_view" -> {
                    val application = selectedApplication
                    val task = selectedTask
                    if (application != null && task != null && currentUser != null) {
                        StudentProfileViewScreen(
                            application = application,
                            hasAcceptedApplicant = applicationViewModel.hasAcceptedApplicant(task.id)
                                    && application.status != ApplicationStatus.IN_PROGRESS,
                            isAccepting = appUiState.acceptingApplicationId == application.applicationId,
                            isRejecting = appUiState.rejectingApplicationId == application.applicationId,
                            errorMessage = appUiState.error,
                            onBack = { currentRoute = "employer_task_applications" },
                            onAccept = {
                                applicationViewModel.acceptApplication(
                                    applicationId = application.applicationId,
                                    employerId = currentUser.id,
                                    taskId = task.id
                                )
                            },
                            onReject = {
                                applicationViewModel.rejectApplication(
                                    applicationId = application.applicationId,
                                    employerId = currentUser.id,
                                    taskId = task.id
                                )
                            },
                            onClearError = { applicationViewModel.clearError() }
                        )
                    }
                }
            }
        }

        // ── Нижняя навигация (скрыта на экранах без nav bar) ──────────────
        AnimatedVisibility(
            visible = currentRoute != "search"
                    && currentRoute != "gig_details"
                    && currentRoute != "notification"
                    && currentRoute != "verification"
                    && currentRoute != "create_task"
                    && currentRoute != "student_my_projects"
                    && currentRoute != "employer_tasks"
                    && currentRoute != "employer_task_applications"  // НОВОЕ
                    && currentRoute != "student_profile_view",       // НОВОЕ
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit  = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomContainer(
                currentRoute = currentRoute,
                onNavigate = { currentRoute = it }
            )
        }
    }
}

@Composable
fun TopContainer(
    name: String?,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    isEmployer: Boolean = false,
    isVerified: Boolean = false,
    onAddTaskClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(horizontal = 15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Привет, ${name ?: "гость"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
                Text(
                    if (isEmployer) "Найдите исполнителя" else "Найди свой проект",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeaderCircleButton(
                    icon = Icons.Outlined.Notifications,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onNotificationClick,
                    hasBadge = true
                )

                HeaderCircleButton(
                    icon = Icons.Outlined.Person,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = onProfileClick
                )
            }
        }

        if (!isEmployer) SearchInput(onClick = onSearchClick)

        if (isEmployer) {
            val strokeColor = if (isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            val dashEffect = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )

            Surface(
                onClick = onAddTaskClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .drawWithContent {
                        drawContent()
                        drawRoundRect(
                            color = strokeColor,
                            style = dashEffect,
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    },
                color = if (isVerified)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Outlined.Add else Icons.Outlined.Info,
                        contentDescription = null,
                        tint = strokeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isVerified) "Добавить задание" else "Нужна верификация",
                        style = MaterialTheme.typography.titleMedium,
                        color = strokeColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MediumContainer(
    tasks: List<com.example.studentgigs.data.model.Task>,
    onGigClick: (com.example.studentgigs.data.model.Task) -> Unit
) {

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(60_000) // ждать 60 секунд
            currentTime = System.currentTimeMillis()
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 15.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item { CategorySelector() }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Новые проекты", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${tasks.size} доступно", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            items(tasks) { task ->
                val isNewLabelVisible = (currentTime - task.createdAt) < 900_000L

                val subtitle = if (!task.employerPosition.isNullOrBlank()) {
                    "${task.employerPosition} • ${task.employerName}"
                } else {
                    task.employerName
                }


                val gigDisplay = Gig(
                    title = task.title,
                    company = subtitle,
                    duration = task.duration ?: "Срок не указан",
                    location = task.location.split(",").first().trim().ifEmpty { "Удаленно" },
                    price = task.price,
                    tags = task.tags,
                    isNew = isNewLabelVisible,
                    iconEmoji = task.iconEmoji,
                    isSaved = false
                )

                GigCard(gig = gigDisplay, onClick = { onGigClick(task) })
            }

            if (tasks.isEmpty()) {
                item {
                    Text("Заданий пока нет", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun BottomContainer(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = remember {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Search,
            BottomNavItem.Saved,
            BottomNavItem.Profile
        )
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(60.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach {
                val isSelected = currentRoute == it.route

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(it.route) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Surface(
                            modifier = Modifier
                                .fillMaxHeight(0.85f)
                                .fillMaxWidth(0.85f),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface
                        ) {}
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TagItem(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

@Composable
fun GigCard(gig: Gig, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable{ onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Text(
                        text = gig.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = gig.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    IconButton(onClick = { /* Save */ }) {
                        Icon(
                            imageVector = if (gig.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = if (gig.isSaved) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val displayPrice = if (gig.price.contains("₽")) gig.price else "${gig.price} ₽"

                InfoRowItem(Icons.Outlined.Schedule, gig.duration)
                InfoRowItem(Icons.Outlined.Place, gig.location)
                InfoRowItem(
                    Icons.Outlined.CurrencyRuble,
                    displayPrice,
                    color = MaterialTheme.colorScheme.primary
                )
            }



            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gig.tags.forEach { tag ->
                    TagItem(tag)
                }
            }
        }
    }
}

@Composable
fun HeaderCircleButton(
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    hasBadge: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        if (hasBadge) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-10).dp, y = 10.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
                    .padding(2.dp)
            )
        }
    }
}

@Composable
fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        readOnly = readOnly,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.outline
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.outline,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    )
}

@Composable
fun SearchInput(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        PillTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            placeholder = "Поиск проектов...",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        )
    }
}

@Composable
fun CategorySelector() {
    val categories = remember {
        listOf(
            Category("Все", "🔥"),
            Category("Разработка", "💻"),
            Category("Дизайн", "🎨"),
            Category("Маркетинг", "📈")
        )
    }

    var selectedCategory by remember { mutableStateOf(categories[0]) }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory

            CategoryShip(
                category = category,
                isSelected = isSelected,
                onClick = { selectedCategory = category }
            )
        }
    }
}

@Composable
fun CategoryShip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = category.icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                color = contentColor,
                fontSize = 12.sp
            )
        }
    }
}



