package com.example.studentgigs.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentgigs.data.model.*
import com.example.studentgigs.data.repository.TaskRepository
import com.example.studentgigs.data.repository.TaskResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TaskUiState(
    val isLoading: Boolean = false,
    // Главный фид — только ACTIVE задания для студентов
    val tasks: List<Task> = emptyList(),
    // ИСПРАВЛЕНО: отдельный список для экрана работодателя (ACTIVE + CLOSED + COMPLETED)
    val employerTasks: List<Task> = emptyList(),
    val error: String? = null,
    val createSuccess: Boolean = false,
    val createdTask: Task? = null
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskRepository = TaskRepository.getInstance(application)

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    // Создание задания
    fun createTask(
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
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = withContext(Dispatchers.IO) {
                taskRepository.createTask(
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
            }

            when (result) {
                is TaskResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createSuccess = true,
                        createdTask = result.task,
                        error = null
                    )
                }
                is TaskResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    // ИСПРАВЛЕНО: результат пишем в employerTasks, не в tasks
    // Так главный фид не загрязняется заданиями работодателя (CLOSED/COMPLETED)
    fun loadEmployerTasks(employerId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = withContext(Dispatchers.IO) {
                taskRepository.getEmployerTasks(employerId)
            }

            when (result) {
                is TaskResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        employerTasks = result.tasks,
                        error = null
                    )
                }
                is TaskResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    // Загрузка активных заданий для главного фида (студенты видят только ACTIVE)
    fun loadAllActiveTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = withContext(Dispatchers.IO) {
                taskRepository.getAllActiveTasks()
            }

            when (result) {
                is TaskResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tasks = result.tasks,
                        error = null
                    )
                }
                is TaskResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearCreateSuccess() {
        _uiState.value = _uiState.value.copy(createSuccess = false, createdTask = null)
    }
}
