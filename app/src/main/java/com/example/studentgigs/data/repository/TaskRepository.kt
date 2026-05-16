package com.example.studentgigs.data.repository

import android.content.Context
import android.util.Log
import com.example.studentgigs.data.local.TaskDao
import com.example.studentgigs.data.model.*
import com.example.studentgigs.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class TaskResult {
    data class Success(val task: Task? = null, val tasks: List<Task> = emptyList()) : TaskResult()
    data class Error(val message: String) : TaskResult()
}

class TaskRepository(context: Context) {
    private val taskDao = TaskDao(context)
    private val apiClient = ApiClient()

    companion object {
        private const val TAG = "TaskRepository"

        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Создание задания
    suspend fun createTask(
        employerId: Long,
        type: TaskType,
        title: String,
        description: String,
        price: String,
        requirements: List<String> = emptyList(),
        benefits: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        priceType: PriceType = PriceType.FIXED,
        duration: String = "",
        location: String = "",
        locationType: LocationType = LocationType.REMOTE,
        deadline: Long? = null,
        iconEmoji: String = "📋",
        employmentType: EmploymentType? = null,
        schedule: String? = null,
        serviceCategory: String? = null
    ): TaskResult = withContext(Dispatchers.IO) {
        when {
            title.isBlank() -> return@withContext TaskResult.Error("Введите название")
            title.length < 5 -> return@withContext TaskResult.Error("Название слишком короткое (минимум 5 симв.)")
            description.isBlank() -> return@withContext TaskResult.Error("Введите описание")
            description.length < 20 -> return@withContext TaskResult.Error("Описание слишком короткое (минимум 20 симв.)")
            requirements.isEmpty() || requirements.all { it.isBlank() } ->
                return@withContext TaskResult.Error("Добавьте хотя бы одно требование")
            benefits.isEmpty() || benefits.all { it.isBlank() } ->
                return@withContext TaskResult.Error("Укажите преимущества")
            tags.isEmpty() ->
                return@withContext TaskResult.Error("Выберите хотя бы один тег")
            price.isBlank() -> return@withContext TaskResult.Error("Укажите стоимость")
            duration.isBlank() -> return@withContext TaskResult.Error("Укажите длительность выполнения")
            employerId <= 0 -> return@withContext TaskResult.Error("Ошибка идентификации пользователя")
        }

        val serverResult = try {
            apiClient.createTask(
                employerId = employerId,
                type = type,
                title = title,
                description = description,
                price = price,
                requirements = requirements,
                benefits = benefits,
                tags = tags,
                priceType = priceType,
                duration = duration,
                location = location,
                locationType = locationType,
                deadline = deadline,
                iconEmoji = iconEmoji,
                employmentType = employmentType,
                schedule = schedule,
                serviceCategory = serviceCategory
            )
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сервера при создании задания: ${e.message}", e)
            null
        }

        if (serverResult?.success == false) {
            return@withContext TaskResult.Error(serverResult.message ?: "Ошибка при создании задания")
        }

        val task = Task(
            employerId = employerId,
            employerName = "Заказчик",
            type = type,
            title = title,
            description = description,
            requirements = requirements,
            benefits = benefits,
            tags = tags,
            price = price,
            priceType = priceType,
            duration = duration,
            location = location,
            locationType = locationType,
            deadline = deadline,
            iconEmoji = iconEmoji,
            employmentType = employmentType,
            schedule = schedule,
            serviceCategory = serviceCategory
        )

        val taskId = if (serverResult?.success == true && serverResult.data != null) {
            val serverTaskId = serverResult.data!!.optLong("task_id", 0)
            if (serverTaskId > 0) serverTaskId else taskDao.insertTask(task)
        } else {
            taskDao.insertTask(task)
        }

        if (taskId > 0) {
            TaskResult.Success(task = task.copy(id = taskId))
        } else {
            TaskResult.Error("Ошибка при создании задания")
        }
    }

    // Получение заданий работодателя
    suspend fun getEmployerTasks(employerId: Long): TaskResult = withContext(Dispatchers.IO) {
        try {
            val serverResult = try {
                apiClient.getTasks(employerId = employerId)
            } catch (e: Exception) { null }

            if (serverResult?.success == true && serverResult.data != null) {
                val tasksArray = serverResult.data!!.optJSONArray("tasks")
                if (tasksArray != null && tasksArray.length() > 0) {
                    val tasksList = mutableListOf<Task>()
                    for (i in 0 until tasksArray.length()) {
                        tasksList.add(parseTaskJson(tasksArray.getJSONObject(i)))
                    }
                    // ИСПРАВЛЕНО: НЕ сохраняем в локальную БД — это вызывало дубликаты
                    return@withContext TaskResult.Success(tasks = tasksList)
                }
            }

            // Фолбэк: локальная БД
            val tasks = taskDao.getTasksByEmployerId(employerId)
            TaskResult.Success(tasks = tasks)
        } catch (e: Exception) {
            TaskResult.Error("Ошибка при получении заданий")
        }
    }

    // Получение всех активных заданий
    suspend fun getAllActiveTasks(): TaskResult = withContext(Dispatchers.IO) {
        try {
            val serverResult = try {
                apiClient.getTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка сети: ${e.message}")
                null
            }

            if (serverResult != null && serverResult.success) {
                val tasksArray = serverResult.data?.optJSONArray("tasks")
                if (tasksArray != null) {
                    val tasksList = mutableListOf<Task>()
                    for (i in 0 until tasksArray.length()) {
                        tasksList.add(parseTaskJson(tasksArray.getJSONObject(i)))
                    }
                    // ИСПРАВЛЕНО: НЕ сохраняем в локальную БД чтобы избежать дубликатов.
                    // Локальная БД используется только как фолбэк при отсутствии сети.
                    return@withContext TaskResult.Success(tasks = tasksList)
                }
            }

            // Фолбэк: локальная БД
            val localTasks = taskDao.getAllActiveTasks()
            TaskResult.Success(tasks = localTasks)

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга: ${e.message}")
            TaskResult.Error("Не удалось обработать список заданий")
        }
    }

    // Удаление задания
    suspend fun deleteTask(taskId: Long): TaskResult = withContext(Dispatchers.IO) {
        try {
            val result = taskDao.deleteTask(taskId)
            if (result > 0) TaskResult.Success()
            else TaskResult.Error("Задание не найдено")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при удалении задания: ${e.message}", e)
            TaskResult.Error("Ошибка при удалении задания")
        }
    }

    // Закрытие задания
    suspend fun closeTask(taskId: Long): TaskResult = withContext(Dispatchers.IO) {
        try {
            val result = taskDao.updateTaskStatus(taskId, TaskStatus.CLOSED)
            if (result > 0) TaskResult.Success()
            else TaskResult.Error("Задание не найдено")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при закрытии задания: ${e.message}", e)
            TaskResult.Error("Ошибка при закрытии задания")
        }
    }

    // ─────────────────────────────────────────────
    // Общий парсер JSON → Task
    // ─────────────────────────────────────────────
    private fun parseTaskJson(taskJson: org.json.JSONObject): Task {
        val type = try {
            TaskType.valueOf(taskJson.optString("type", "TASK"))
        } catch (e: Exception) { TaskType.TASK }

        val priceType = try {
            PriceType.valueOf(taskJson.optString("price_type", "FIXED"))
        } catch (e: Exception) { PriceType.FIXED }

        val locationType = try {
            LocationType.valueOf(taskJson.optString("location_type", "REMOTE"))
        } catch (e: Exception) { LocationType.REMOTE }

        val status = try {
            TaskStatus.valueOf(taskJson.optString("status", "ACTIVE"))
        } catch (e: Exception) { TaskStatus.ACTIVE }

        val empType = taskJson.optString("employment_type", "").let {
            if (it.isBlank() || it == "null") null
            else try { EmploymentType.valueOf(it) } catch (e: Exception) { null }
        }

        return Task(
            id              = taskJson.optLong("id", 0),
            employerId      = taskJson.optLong("employer_id", 0),
            employerName    = taskJson.optString("employer_name", "Компания не указана"),
            employerPosition= taskJson.optString("employer_position", "").takeIf { it.isNotBlank() },
            type            = type,
            title           = taskJson.optString("title", ""),
            description     = taskJson.optString("description", ""),
            requirements    = taskJson.optString("requirements", "").split("|").filter { it.isNotBlank() },
            benefits        = taskJson.optString("benefits", "").split("|").filter { it.isNotBlank() },
            tags            = taskJson.optString("tags", "").split("|").filter { it.isNotBlank() },
            price           = taskJson.optString("price", ""),
            priceType       = priceType,
            duration        = taskJson.optString("duration", ""),
            location        = taskJson.optString("location", ""),
            locationType    = locationType,
            deadline        = taskJson.optLong("deadline", 0).takeIf { it != 0L },
            status          = status,
            createdAt       = taskJson.optLong("created_at", System.currentTimeMillis()),
            responsesCount  = taskJson.optInt("responses_count", 0),
            iconEmoji       = taskJson.optString("icon_emoji", "📋"),
            employmentType  = empType,
            schedule        = taskJson.optString("schedule", "").takeIf { it.isNotBlank() },
            serviceCategory = taskJson.optString("service_category", "").takeIf { it.isNotBlank() }
        )
    }
}
