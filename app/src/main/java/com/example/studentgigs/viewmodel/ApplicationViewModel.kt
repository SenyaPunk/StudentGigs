package com.example.studentgigs.viewmodel

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentgigs.data.model.Application as StudentApplication
import com.example.studentgigs.data.model.PendingReview
import com.example.studentgigs.data.model.ApplicationStatus
import com.example.studentgigs.data.repository.ApplicationRepository
import com.example.studentgigs.data.repository.ApplicationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ApplicationUiState(
    val isLoading: Boolean = false,

    // ── Студент ──────────────────────────────────────
    val applications: List<StudentApplication> = emptyList(),
    val appliedTaskIds: Set<Long> = emptySet(),
    val applyingTaskId: Long? = null,

    // ── Работодатель ─────────────────────────────────
    /** Список откликов на конкретное задание */
    val taskApplications: List<StudentApplication> = emptyList(),
    /** true — идёт загрузка откликов на задание */
    val isLoadingTaskApplications: Boolean = false,
    /** id заявки, которая прямо сейчас принимается */
    val acceptingApplicationId: Long? = null,
    /** id заявки, которая прямо сейчас отклоняется */
    val rejectingApplicationId: Long? = null,
    /** Операция принятия/отклонения прошла успешно — можно обновить список */
    val operationSuccess: Boolean = false,

    // ── Общие ────────────────────────────────────────
    val error: String? = null,

    // ── Завершение задания (работодатель) ─────────────────────────────
    val isConfirmingCompletion: Boolean = false,
    val completionMessage: String? = null,
    val pendingReview: PendingReview? = null
)

class ApplicationViewModel(application: android.app.Application) : AndroidViewModel(application) {

    private val repository = ApplicationRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ApplicationUiState())
    val uiState: StateFlow<ApplicationUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "ApplicationViewModel"
    }

    // ────────────────────────────────────────────────
    // Студент
    // ────────────────────────────────────────────────

    fun loadApplications(studentId: Long) {
        Log.d(TAG, "loadApplications: studentId=$studentId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getApplications(studentId)) {
                is ApplicationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        applications = result.applications,
                        appliedTaskIds = result.appliedTaskIds
                    )
                }
                is ApplicationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun applyToTask(studentId: Long, taskId: Long) {
        if (_uiState.value.appliedTaskIds.contains(taskId)) return
        if (_uiState.value.applyingTaskId != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(applyingTaskId = taskId, error = null)
            when (val result = repository.applyToTask(studentId, taskId)) {
                is ApplicationResult.Success -> {
                    val newAppliedIds = _uiState.value.appliedTaskIds + taskId
                    _uiState.value = _uiState.value.copy(
                        applyingTaskId = null,
                        appliedTaskIds = newAppliedIds
                    )
                    loadApplications(studentId)
                }
                is ApplicationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        applyingTaskId = null,
                        error = result.message
                    )
                }
            }
        }
    }

    fun isApplied(taskId: Long): Boolean = _uiState.value.appliedTaskIds.contains(taskId)

    // ────────────────────────────────────────────────
    // Работодатель
    // ────────────────────────────────────────────────

    /** Загрузить все отклики на конкретное задание */
    fun loadTaskApplications(taskId: Long, employerId: Long) {
        Log.d(TAG, "loadTaskApplications: taskId=$taskId, employerId=$employerId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingTaskApplications = true,
                error = null,
                operationSuccess = false
            )
            when (val result = repository.getTaskApplications(taskId, employerId)) {
                is ApplicationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingTaskApplications = false,
                        taskApplications = result.applications
                    )
                }
                is ApplicationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingTaskApplications = false,
                        error = result.message
                    )
                }
            }
        }
    }

    /**
     * Принять отклик студента.
     * Перед вызовом убедиться, что нет другого IN_PROGRESS — это проверяет и сервер,
     * но на клиенте проверка ускоряет UX (диалог подтверждения показываем только если можно).
     */
    fun acceptApplication(applicationId: Long, employerId: Long, taskId: Long) {
        Log.d(TAG, "acceptApplication: applicationId=$applicationId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(acceptingApplicationId = applicationId, error = null)
            when (val result = repository.acceptApplication(applicationId, employerId, taskId)) {
                is ApplicationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        acceptingApplicationId = null,
                        operationSuccess = true
                    )
                    // Обновляем список откликов
                    loadTaskApplications(taskId, employerId)
                }
                is ApplicationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        acceptingApplicationId = null,
                        error = result.message
                    )
                }
            }
        }
    }

    /**
     * Отозвать/отклонить принятый отклик.
     * После этого работодатель может принять другого студента.
     */
    fun rejectApplication(applicationId: Long, employerId: Long, taskId: Long) {
        Log.d(TAG, "rejectApplication: applicationId=$applicationId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(rejectingApplicationId = applicationId, error = null)
            when (val result = repository.rejectApplication(applicationId, employerId)) {
                is ApplicationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        rejectingApplicationId = null,
                        operationSuccess = true
                    )
                    loadTaskApplications(taskId, employerId)
                }
                is ApplicationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        rejectingApplicationId = null,
                        error = result.message
                    )
                }
            }
        }
    }

    /** Есть ли уже принятый студент для данного taskId? */
    fun hasAcceptedApplicant(taskId: Long): Boolean {
        return _uiState.value.taskApplications.any {
            it.taskId == taskId && it.status == ApplicationStatus.IN_PROGRESS
        }
    }

    /** Сбросить флаг успешной операции */
    fun clearOperationSuccess() {
        _uiState.value = _uiState.value.copy(operationSuccess = false)
    }

    /** Очистить список откликов при уходе с экрана */
    fun clearTaskApplications() {
        _uiState.value = _uiState.value.copy(taskApplications = emptyList(), operationSuccess = false)
    }

    // ────────────────────────────────────────────────
    // Общие
    // ────────────────────────────────────────────────

    /** Работодатель подтверждает завершение задания */
    fun confirmCompletionAsEmployer(applicationId: Long, userId: Long, taskId: Long, employerId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConfirmingCompletion = true, error = null)
            when (val result = repository.confirmCompletion(applicationId, userId)) {
                is ApplicationResult.Success -> {
                    val bothConfirmed = result.bothConfirmed
                    val pendingReview: PendingReview? = if (bothConfirmed) {
                        val studentApp = _uiState.value.taskApplications.firstOrNull {
                            it.status == ApplicationStatus.IN_PROGRESS
                        }
                        PendingReview(
                            revieweeId   = studentApp?.studentId ?: 0L,
                            revieweeName = studentApp?.student?.displayName ?: "Студент",
                            applicationId = applicationId,
                            taskId       = taskId,
                            reviewerRole = "EMPLOYER"
                        )
                    } else null
                    _uiState.value = _uiState.value.copy(
                        isConfirmingCompletion = false,
                        completionMessage = if (bothConfirmed) "Задание завершено!" else "Ожидаем подтверждения от студента",
                        pendingReview = pendingReview
                    )
                    loadTaskApplications(taskId, employerId)
                }
                is ApplicationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isConfirmingCompletion = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearCompletionMessage() {
        _uiState.value = _uiState.value.copy(completionMessage = null)
    }

    fun clearPendingReview() {
        _uiState.value = _uiState.value.copy(pendingReview = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    val pendingApplications: List<StudentApplication>
        get() = _uiState.value.applications.filter { it.status == ApplicationStatus.PENDING }

    val inProgressApplications: List<StudentApplication>
        get() = _uiState.value.applications.filter { it.status == ApplicationStatus.IN_PROGRESS }

    val completedApplications: List<StudentApplication>
        get() = _uiState.value.applications.filter { it.status == ApplicationStatus.COMPLETED }
}
