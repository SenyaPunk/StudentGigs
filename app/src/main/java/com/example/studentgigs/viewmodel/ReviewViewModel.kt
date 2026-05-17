package com.example.studentgigs.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentgigs.data.model.Review
import com.example.studentgigs.data.repository.ReviewRepository
import com.example.studentgigs.data.repository.ReviewResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewUiState(
    val isSubmitting: Boolean = false,
    val isLoadingReviews: Boolean = false,
    val isLoadingSubmittedReviews: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val averageRating: Float = 0f,
    val totalCount: Int = 0,
    val submitSuccess: Boolean = false,
    val lastSubmittedApplicationId: Long = 0L,
    // Набор applicationId, для которых текущий пользователь уже оставил отзыв
    val reviewedApplicationIds: Set<Long> = emptySet(),
    // Набор taskId, для которых текущий пользователь уже оставил отзыв (для работодателя)
    val reviewedTaskIds: Set<Long> = emptySet(),
    val error: String? = null
)

class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReviewRepository.getInstance()

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun submitReview(
        reviewerId: Long,
        revieweeId: Long,
        applicationId: Long,
        taskId: Long,
        rating: Int,
        comment: String,
        reviewerRole: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            when (val result = repository.submitReview(
                reviewerId, revieweeId, applicationId, taskId, rating, comment, reviewerRole
            )) {
                is ReviewResult.Success -> {
                    // Локально помечаем как отрецензированное — нет необходимости
                    // перезагружать весь список с сервера
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submitSuccess = true,
                        lastSubmittedApplicationId = applicationId,
                        reviewedApplicationIds = _uiState.value.reviewedApplicationIds + applicationId,
                        reviewedTaskIds = _uiState.value.reviewedTaskIds + taskId
                    )
                }
                is ReviewResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                }
            }
        }
    }

    fun loadReviews(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingReviews = true, error = null)
            when (val result = repository.getReviews(userId)) {
                is ReviewResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingReviews = false,
                        reviews          = result.reviews,
                        averageRating    = result.averageRating,
                        totalCount       = result.totalCount
                    )
                }
                is ReviewResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingReviews = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoadingReviews = false)
                }
            }
        }
    }

    /**
     * Загрузить список applicationId/taskId, на которые текущий пользователь
     * уже оставил отзыв. Вызывать при входе в приложение (после авторизации)
     * и при открытии экранов со списком завершённых заданий.
     */
    fun loadMySubmittedReviews(reviewerId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingSubmittedReviews = true)
            when (val result = repository.getMySubmittedReviews(reviewerId)) {
                is ReviewResult.SubmittedReviews -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingSubmittedReviews = false,
                        reviewedApplicationIds = result.applicationIds,
                        reviewedTaskIds = result.taskIds
                    )
                }
                is ReviewResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingSubmittedReviews = false)
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoadingSubmittedReviews = false)
                }
            }
        }
    }

    fun clearSubmitSuccess() {
        _uiState.value = _uiState.value.copy(
            submitSuccess = false,
            lastSubmittedApplicationId = 0L
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
