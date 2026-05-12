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
        // Валидация
        when {
            // Базовая информация
            title.isBlank() -> return@withContext TaskResult.Error("Введите название")
            title.length < 5 -> return@withContext TaskResult.Error("Название слишком короткое (минимум 5 симв.)")

            description.isBlank() -> return@withContext TaskResult.Error("Введите описание")
            description.length < 20 -> return@withContext TaskResult.Error("Описание слишком короткое (минимум 20 симв.)")

            // Списки (Requirements, Benefits, Tags)
            requirements.isEmpty() || requirements.all { it.isBlank() } ->
                return@withContext TaskResult.Error("Добавьте хотя бы одно требование")

            benefits.isEmpty() || benefits.all { it.isBlank() } ->
                return@withContext TaskResult.Error("Укажите преимущества")

            tags.isEmpty() ->
                return@withContext TaskResult.Error("Выберите хотя бы один тег")

            // Оплата и время
            price.isBlank() -> return@withContext TaskResult.Error("Укажите стоимость")
            duration.isBlank() -> return@withContext TaskResult.Error("Укажите длительность выполнения")

            // Местоположение
            // location.isBlank() -> return@withContext TaskResult.Error("Укажите местоположение или адрес")

            // Дополнительные поля (nullable в сигнатуре, но обязательные по вашему требованию)
            // employmentType == null -> return@withContext TaskResult.Error("Выберите тип занятости")
            // schedule.isNullOrBlank() -> return@withContext TaskResult.Error("Укажите график работы")
            // serviceCategory.isNullOrBlank() -> return@withContext TaskResult.Error("Выберите категорию услуг")



            // Идентификатор работодателя
            employerId <= 0 -> return@withContext TaskResult.Error("Ошибка идентификации пользователя")
        }

        // Попытка отправить на сервер
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

        // Создаем задание локально
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
            val serverTaskId = serverResult.data.optLong("task_id", 0)
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
            // Пробуем получить с сервера
            val serverResult = try {
                apiClient.getTasks(employerId = employerId)
            } catch (e: Exception) {
                Log.w(TAG, "Ошибка сервера: ${e.message}")
                null
            }

            // Если сервер доступен, возвращаем его данные
            // Иначе берем из локальной БД
            val tasks = taskDao.getTasksByEmployerId(employerId)
            TaskResult.Success(tasks = tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении заданий: ${e.message}", e)
            TaskResult.Error("Ошибка при получении заданий")
        }
    }

    // Получение всех активных заданий
    suspend fun getAllActiveTasks(): TaskResult = withContext(Dispatchers.IO) {
        try {
            val serverResult = try {
                apiClient.getTasks() // Вызывает ваш POST запрос к get_tasks.php
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка сети: ${e.message}")
                null
            }

            if (serverResult != null && serverResult.success) {
                val tasksList = mutableListOf<Task>()

                // Извлекаем массив "tasks" из поля "data"
                val tasksArray = serverResult.data?.optJSONArray("tasks")

                if (tasksArray != null) {
                    for (i in 0 until tasksArray.length()) {
                        val taskJson = tasksArray.getJSONObject(i)

                        // Вручную создаем объект Task из JSON
                        val task = Task(
                            id = taskJson.optLong("id", 0),
                            employerId = taskJson.optLong("employer_id", 0),
                            employerName = taskJson.optString("employer_name", "Компания не указана"),
                            employerPosition = taskJson.optString("employer_position", null),
                            type = TaskType.valueOf(taskJson.optString("type", "TASK")),
                            title = taskJson.optString("title", ""),
                            description = taskJson.optString("description", ""),
                            requirements = taskJson.optString("requirements", "").split("|").filter { it.isNotBlank() },
                            benefits = taskJson.optString("benefits", "").split("|").filter { it.isNotBlank() },
                            tags = taskJson.optString("tags", "").split("|").filter { it.isNotBlank() },
                            price = taskJson.optString("price", ""),
                            iconEmoji = taskJson.optString("icon_emoji", "📋"),
                            location = taskJson.optString("location", ""),
                            locationType = LocationType.valueOf(taskJson.optString("location_type", "REMOTE")),
                            createdAt = taskJson.optLong("created_at", System.currentTimeMillis()),
                            employmentType = taskJson.optString("employment_type", null)?.let {
                                if (it != "null") EmploymentType.valueOf(it) else null
                            },
                            schedule = taskJson.optString("schedule", null),
                            serviceCategory = taskJson.optString("service_category", null),
                            duration = taskJson.optString("duration", ""),
                            // Добавьте остальные поля, если они нужны для отображения
                        )
                        tasksList.add(task)
                    }

                    // Если данные пришли, обновляем локальную БД (чтобы после перезапуска работало)
                    tasksList.forEach { taskDao.insertTask(it) }

                    return@withContext TaskResult.Success(tasks = tasksList)
                }
            }

            // Если сервер не ответил, берем из локальной БД
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
            if (result > 0) {
                TaskResult.Success()
            } else {
                TaskResult.Error("Задание не найдено")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при удалении задания: ${e.message}", e)
            TaskResult.Error("Ошибка при удалении задания")
        }
    }

    // Закрытие задания
    suspend fun closeTask(taskId: Long): TaskResult = withContext(Dispatchers.IO) {
        try {
            val result = taskDao.updateTaskStatus(taskId, TaskStatus.CLOSED)
            if (result > 0) {
                TaskResult.Success()
            } else {
                TaskResult.Error("Задание не найдено")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при закрытии задания: ${e.message}", e)
            TaskResult.Error("Ошибка при закрытии задания")
        }
    }
}
