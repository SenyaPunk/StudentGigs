package com.example.studentgigs.data.repository

import android.content.Context
import android.util.Log
import com.example.studentgigs.data.model.*
import com.example.studentgigs.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed class ApplicationResult {
    data class Success(
        val applied: Boolean = false,
        val applications: List<Application> = emptyList(),
        val appliedTaskIds: Set<Long> = emptySet(),
        val bothConfirmed: Boolean = false
    ) : ApplicationResult()
    data class Error(val message: String) : ApplicationResult()
}

class ApplicationRepository(context: Context) {

    private val apiClient = ApiClient()

    companion object {
        private const val TAG = "ApplicationRepository"

        @Volatile
        private var INSTANCE: ApplicationRepository? = null

        fun getInstance(context: Context): ApplicationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApplicationRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /** Откликнуться на задание (студент) */
    suspend fun applyToTask(studentId: Long, taskId: Long): ApplicationResult =
        withContext(Dispatchers.IO) {
            try {
                val response = apiClient.applyToTask(studentId, taskId)
                if (response.body.isBlank()) return@withContext ApplicationResult.Error("Сервер не ответил (пустой ответ)")
                if (!response.body.trimStart().startsWith("{")) return@withContext ApplicationResult.Error("Неверный ответ сервера: ${response.body.take(200)}")
                val json = JSONObject(response.body)
                if (json.optBoolean("success", false)) {
                    val data = json.optJSONObject("data")
                    ApplicationResult.Success(applied = data?.optBoolean("applied", false) ?: false)
                } else {
                    ApplicationResult.Error(json.optString("message", "Ошибка отклика"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "applyToTask error", e)
                ApplicationResult.Error(e.message ?: "Неизвестная ошибка")
            }
        }

    /** Получить все отклики студента */
    suspend fun getApplications(studentId: Long): ApplicationResult =
        withContext(Dispatchers.IO) {
            try {
                val response = apiClient.getApplications(studentId)
                if (response.body.isBlank()) return@withContext ApplicationResult.Error("Сервер не ответил (пустой ответ)")
                if (!response.body.trimStart().startsWith("{")) return@withContext ApplicationResult.Error("Неверный ответ сервера: ${response.body.take(200)}")
                val json = JSONObject(response.body)

                if (!json.optBoolean("success", false)) {
                    return@withContext ApplicationResult.Error(
                        json.optString("message", "Ошибка загрузки откликов")
                    )
                }

                val data = json.optJSONObject("data")
                    ?: return@withContext ApplicationResult.Error("Нет данных")
                val appsArray = data.optJSONArray("applications") ?: JSONArray()
                val appliedIdsArray = data.optJSONArray("applied_task_ids") ?: JSONArray()

                val applications = mutableListOf<Application>()
                for (i in 0 until appsArray.length()) {
                    val obj = appsArray.getJSONObject(i)
                    val taskObj = obj.optJSONObject("task")
                    val task = taskObj?.let { parseTask(it) }
                    val status = parseApplicationStatus(obj.optString("application_status", "PENDING"))
                    applications.add(
                        Application(
                            applicationId = obj.optLong("application_id"),
                            studentId = obj.optLong("student_id"),
                            taskId = obj.optLong("task_id"),
                            status = status,
                            appliedAt = obj.optLong("applied_at"),
                            task = task
                        )
                    )
                }

                val appliedTaskIds = mutableSetOf<Long>()
                for (i in 0 until appliedIdsArray.length()) {
                    appliedTaskIds.add(appliedIdsArray.getLong(i))
                }

                ApplicationResult.Success(
                    applications = applications,
                    appliedTaskIds = appliedTaskIds
                )
            } catch (e: Exception) {
                Log.e(TAG, "getApplications error", e)
                ApplicationResult.Error(e.message ?: "Неизвестная ошибка")
            }
        }

    // ─────────────────────────────────────────────
    // Работодатель
    // ─────────────────────────────────────────────

    /** Получить все отклики на конкретное задание (работодатель) */
    suspend fun getTaskApplications(taskId: Long, employerId: Long): ApplicationResult =
        withContext(Dispatchers.IO) {
            try {
                val response = apiClient.getTaskApplications(taskId, employerId)
                val rawBody = response.body.trim()
                Log.d(TAG, "getTaskApplications rawBody(300): '${rawBody.take(300)}'")
                if (rawBody.isEmpty()) {
                    return@withContext ApplicationResult.Error("Пустой ответ сервера. Убедитесь, что PHP файлы загружены и config.php доступен.")
                }
                if (!rawBody.startsWith("{")) {
                    return@withContext ApplicationResult.Error("Неверный формат ответа: ${rawBody.take(200)}")
                }
                val json = JSONObject(rawBody)

                if (!json.optBoolean("success", false)) {
                    return@withContext ApplicationResult.Error(
                        json.optString("message", "Ошибка загрузки откликов")
                    )
                }

                val data = json.optJSONObject("data")
                    ?: return@withContext ApplicationResult.Error("Нет данных в ответе")
                val appsArray = data.optJSONArray("applications") ?: JSONArray()

                val applications = mutableListOf<Application>()
                for (i in 0 until appsArray.length()) {
                    val obj = appsArray.getJSONObject(i)
                    val studentObj = obj.optJSONObject("student")
                    val student = studentObj?.let { parseStudent(it) }
                    val status = parseApplicationStatus(obj.optString("application_status", "PENDING"))
                    applications.add(
                        Application(
                            applicationId = obj.optLong("application_id"),
                            studentId = obj.optLong("student_id"),
                            taskId = obj.optLong("task_id"),
                            status = status,
                            appliedAt = obj.optLong("applied_at"),
                            student = student
                        )
                    )
                }

                ApplicationResult.Success(applications = applications)
            } catch (e: Exception) {
                Log.e(TAG, "getTaskApplications error", e)
                ApplicationResult.Error(e.message ?: "Неизвестная ошибка")
            }
        }

    /** Принять отклик студента */
    suspend fun acceptApplication(applicationId: Long, employerId: Long, taskId: Long): ApplicationResult =
        withContext(Dispatchers.IO) {
            try {
                val response = apiClient.acceptApplication(applicationId, employerId, taskId)
                if (response.body.isBlank()) return@withContext ApplicationResult.Error("Сервер не ответил")
                val json = JSONObject(response.body)
                if (json.optBoolean("success", false)) {
                    ApplicationResult.Success()
                } else {
                    ApplicationResult.Error(json.optString("message", "Ошибка принятия отклика"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "acceptApplication error", e)
                ApplicationResult.Error(e.message ?: "Неизвестная ошибка")
            }
        }

    /** Отозвать/отклонить принятый отклик */
    suspend fun rejectApplication(applicationId: Long, employerId: Long): ApplicationResult =
        withContext(Dispatchers.IO) {
            try {
                val response = apiClient.rejectApplication(applicationId, employerId)
                if (response.body.isBlank()) return@withContext ApplicationResult.Error("Сервер не ответил")
                val json = JSONObject(response.body)
                if (json.optBoolean("success", false)) {
                    ApplicationResult.Success()
                } else {
                    ApplicationResult.Error(json.optString("message", "Ошибка отклонения"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "rejectApplication error", e)
                ApplicationResult.Error(e.message ?: "Неизвестная ошибка")
            }
        }

    // ─────────────────────────────────────────────
    // Парсинг
    // ─────────────────────────────────────────────

    private fun parseApplicationStatus(str: String): ApplicationStatus =
        try { ApplicationStatus.valueOf(str) } catch (e: Exception) { ApplicationStatus.PENDING }

    private fun parseStudent(json: JSONObject): User {
        val role = try { UserRole.valueOf(json.optString("role", "STUDENT")) } catch (e: Exception) { UserRole.STUDENT }
        val verStatus = try {
            VerificationStatus.valueOf(json.optString("verification_status", "NOT_VERIFIED"))
        } catch (e: Exception) { VerificationStatus.NOT_VERIFIED }

        return User(
            id = json.optLong("id"),
            email = json.optString("email", ""),
            passwordHash = "",
            role = role,
            fullName = json.optString("full_name", "").takeIf { it.isNotBlank() },
            companyName = json.optString("company_name", "").takeIf { it.isNotBlank() },
            companyPosition = json.optString("company_position", "").takeIf { it.isNotBlank() },
            verificationStatus = verStatus,
            createdAt = json.optLong("created_at", System.currentTimeMillis())
        )
    }

    private fun parseTask(json: JSONObject): Task {
        val type = try { TaskType.valueOf(json.optString("type", "TASK")) } catch (e: Exception) { TaskType.TASK }
        val priceType = try { PriceType.valueOf(json.optString("price_type", "FIXED")) } catch (e: Exception) { PriceType.FIXED }
        val locationType = try { LocationType.valueOf(json.optString("location_type", "REMOTE")) } catch (e: Exception) { LocationType.REMOTE }
        val status = try { TaskStatus.valueOf(json.optString("status", "ACTIVE")) } catch (e: Exception) { TaskStatus.ACTIVE }
        val empType = json.optString("employment_type", "").takeIf { it.isNotBlank() }?.let {
            try { EmploymentType.valueOf(it) } catch (e: Exception) { null }
        }

        fun splitPipe(str: String?) = str?.split("|")?.filter { it.isNotBlank() } ?: emptyList()

        return Task(
            id = json.optLong("id"),
            employerId = json.optLong("employer_id"),
            employerName = json.optString("employer_name", ""),
            employerPosition = json.optString("employer_position", "").takeIf { it.isNotBlank() },
            type = type,
            title = json.optString("title", ""),
            description = json.optString("description", ""),
            requirements = splitPipe(json.optString("requirements", "")),
            benefits = splitPipe(json.optString("benefits", "")),
            tags = splitPipe(json.optString("tags", "")),
            price = json.optString("price", ""),
            priceType = priceType,
            duration = json.optString("duration", ""),
            location = json.optString("location", ""),
            locationType = locationType,
            deadline = json.optLong("deadline").takeIf { it != 0L },
            status = status,
            createdAt = json.optLong("created_at"),
            responsesCount = json.optInt("responses_count"),
            iconEmoji = json.optString("icon_emoji", "📋"),
            employmentType = empType,
            schedule = json.optString("schedule", "").takeIf { it.isNotBlank() },
            serviceCategory = json.optString("service_category", "").takeIf { it.isNotBlank() }
        )
    }

    /** Подтвердить завершение задания (студент или работодатель) */
    suspend fun confirmCompletion(applicationId: Long, userId: Long): ApplicationResult =
        withContext(Dispatchers.IO) {
            try {
                val json = org.json.JSONObject().apply {
                    put("application_id", applicationId)
                    put("user_id", userId)
                }
                val response = apiClient.postJsonRaw(
                    com.example.studentgigs.data.remote.ApiConfig.BASE_URL +
                            com.example.studentgigs.data.remote.ApiConfig.CONFIRM_COMPLETION,
                    json.toString()
                )
                if (response.body.isBlank()) return@withContext ApplicationResult.Error("Сервер не ответил")
                val respJson = org.json.JSONObject(response.body)
                if (respJson.optBoolean("success", false)) {
                    val data = respJson.optJSONObject("data")
                    val bothConfirmed = data?.optBoolean("both_confirmed", false) ?: false
                    ApplicationResult.Success(bothConfirmed = bothConfirmed)
                } else {
                    ApplicationResult.Error(respJson.optString("message", "Ошибка подтверждения"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "confirmCompletion error", e)
                ApplicationResult.Error(e.message ?: "Неизвестная ошибка")
            }
        }
}
