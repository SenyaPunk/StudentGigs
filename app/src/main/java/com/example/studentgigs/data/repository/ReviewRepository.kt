package com.example.studentgigs.data.repository

import android.util.Log
import com.example.studentgigs.data.model.Review
import com.example.studentgigs.data.remote.ApiClient
import com.example.studentgigs.data.remote.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class ReviewResult {
    data class Success(
        val reviews: List<Review> = emptyList(),
        val averageRating: Float = 0f,
        val totalCount: Int = 0
    ) : ReviewResult()

    data class SubmittedReviews(
        val applicationIds: Set<Long> = emptySet(),
        val taskIds: Set<Long> = emptySet()
    ) : ReviewResult()

    data class Error(val message: String) : ReviewResult()
}

class ReviewRepository {

    private val apiClient = ApiClient()

    companion object {
        private const val TAG = "ReviewRepository"

        @Volatile
        private var INSTANCE: ReviewRepository? = null

        fun getInstance(): ReviewRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ReviewRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun submitReview(
        reviewerId: Long,
        revieweeId: Long,
        applicationId: Long,
        taskId: Long,
        rating: Int,
        comment: String,
        reviewerRole: String
    ): ReviewResult = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("reviewer_id",    reviewerId)
                put("reviewee_id",    revieweeId)
                put("application_id", applicationId)
                put("task_id",        taskId)
                put("rating",         rating)
                put("comment",        comment)
                put("reviewer_role",  reviewerRole)
            }.toString()

            val response = apiClient.postJsonRaw(ApiConfig.BASE_URL + ApiConfig.SUBMIT_REVIEW, body)
            if (response.body.isBlank()) return@withContext ReviewResult.Error("Нет ответа от сервера")
            val json = JSONObject(response.body)
            if (json.optBoolean("success", false)) {
                ReviewResult.Success()
            } else {
                ReviewResult.Error(json.optString("message", "Ошибка отправки отзыва"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitReview error", e)
            ReviewResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    suspend fun getReviews(userId: Long): ReviewResult = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply { put("user_id", userId) }.toString()
            val response = apiClient.postJsonRaw(ApiConfig.BASE_URL + ApiConfig.GET_REVIEWS, body)
            if (response.body.isBlank()) return@withContext ReviewResult.Error("Нет ответа от сервера")
            val json = JSONObject(response.body)
            if (!json.optBoolean("success", false)) {
                return@withContext ReviewResult.Error(json.optString("message", "Ошибка загрузки отзывов"))
            }
            val data = json.optJSONObject("data") ?: return@withContext ReviewResult.Success()
            val arr = data.optJSONArray("reviews")
            val reviews = mutableListOf<Review>()
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val r = arr.getJSONObject(i)
                    reviews.add(
                        Review(
                            id             = r.optLong("id"),
                            reviewerId     = r.optLong("reviewer_id"),
                            revieweeId     = r.optLong("reviewee_id"),
                            applicationId  = r.optLong("application_id"),
                            taskId         = r.optLong("task_id"),
                            rating         = r.optInt("rating", 5),
                            comment        = r.optString("comment", ""),
                            reviewerRole   = r.optString("reviewer_role", ""),
                            reviewerName   = r.optString("reviewer_name", "Пользователь"),
                            taskTitle      = r.optString("task_title", ""),
                            createdAt      = r.optLong("created_at", 0L)
                        )
                    )
                }
            }
            ReviewResult.Success(
                reviews       = reviews,
                averageRating = data.optDouble("average_rating", 0.0).toFloat(),
                totalCount    = data.optInt("total_count", 0)
            )
        } catch (e: Exception) {
            Log.e(TAG, "getReviews error", e)
            ReviewResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    /**
     * Получить список applicationId и taskId, на которые текущий пользователь
     * уже оставил отзыв (как рецензент). Используется для отображения статуса
     * "Отзыв оставлен" на экранах завершённых заданий.
     */
    suspend fun getMySubmittedReviews(reviewerId: Long): ReviewResult = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply { put("reviewer_id", reviewerId) }.toString()
            val response = apiClient.postJsonRaw(
                ApiConfig.BASE_URL + ApiConfig.GET_MY_SUBMITTED_REVIEWS, body
            )
            if (response.body.isBlank()) return@withContext ReviewResult.Error("Нет ответа от сервера")
            val json = JSONObject(response.body)
            if (!json.optBoolean("success", false)) {
                return@withContext ReviewResult.Error(json.optString("message", "Ошибка"))
            }
            val data = json.optJSONObject("data") ?: return@withContext ReviewResult.SubmittedReviews()
            val arr = data.optJSONArray("reviewed_applications")
            val appIds = mutableSetOf<Long>()
            val taskIds = mutableSetOf<Long>()
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    appIds.add(obj.optLong("application_id"))
                    taskIds.add(obj.optLong("task_id"))
                }
            }
            ReviewResult.SubmittedReviews(applicationIds = appIds, taskIds = taskIds)
        } catch (e: Exception) {
            Log.e(TAG, "getMySubmittedReviews error", e)
            ReviewResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }
}
