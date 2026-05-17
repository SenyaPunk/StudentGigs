package com.example.studentgigs.data.remote

import com.example.studentgigs.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ApiClient {

    suspend fun register(
        email: String, password: String, role: String,
        fullName: String? = null, companyName: String? = null, companyPosition: String? = null
    ): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("email", email); put("password", password); put("role", role)
            fullName?.let { put("full_name", it) }
            companyName?.let { put("company_name", it) }
            companyPosition?.let { put("company_position", it) }
        }
        postRequest(ApiConfig.BASE_URL + ApiConfig.REGISTER, json.toString())
    }

    suspend fun login(email: String, password: String): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("email", email); put("password", password) }
        postRequest(ApiConfig.BASE_URL + ApiConfig.LOGIN, json.toString())
    }

    suspend fun getUser(userId: Long): ApiResponse = withContext(Dispatchers.IO) {
        postRequest(ApiConfig.BASE_URL + ApiConfig.GET_USER,
            JSONObject().apply { put("user_id", userId) }.toString())
    }

    suspend fun updateUser(
        userId: Long, fullName: String? = null,
        companyName: String? = null, companyPosition: String? = null
    ): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("user_id", userId)
            fullName?.let { put("full_name", it) }
            companyName?.let { put("company_name", it) }
            companyPosition?.let { put("company_position", it) }
        }
        postRequest(ApiConfig.BASE_URL + ApiConfig.UPDATE_USER, json.toString())
    }

    suspend fun verifyEmployer(userId: Long, passportPhotoUrl: String): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("user_id", userId); put("passport_photo_url", passportPhotoUrl) }
        postRequest(ApiConfig.BASE_URL + ApiConfig.VERIFY_EMPLOYER, json.toString())
    }

    suspend fun checkVerification(userId: Long): ApiResponse = withContext(Dispatchers.IO) {
        postRequest(ApiConfig.BASE_URL + ApiConfig.CHECK_VERIFICATION,
            JSONObject().apply { put("user_id", userId) }.toString())
    }

    suspend fun createTask(
        employerId: Long, type: TaskType, title: String, description: String, price: String,
        requirements: List<String> = emptyList(), benefits: List<String> = emptyList(),
        tags: List<String> = emptyList(), priceType: PriceType = PriceType.FIXED,
        duration: String = "", location: String = "", locationType: LocationType = LocationType.REMOTE,
        deadline: Long? = null, iconEmoji: String = "📋", employmentType: EmploymentType? = null,
        schedule: String? = null, serviceCategory: String? = null
    ): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("employer_id", employerId); put("type", type.name)
            put("title", title); put("description", description); put("price", price)
            put("requirements", requirements.joinToString("|"))
            put("benefits", benefits.joinToString("|"))
            put("tags", tags.joinToString("|"))
            put("price_type", priceType.name); put("duration", duration)
            put("location", location); put("location_type", locationType.name)
            deadline?.let { put("deadline", it) }
            put("icon_emoji", iconEmoji)
            employmentType?.let { put("employment_type", it.name) }
            schedule?.let { put("schedule", it) }
            serviceCategory?.let { put("service_category", it) }
        }
        postRequest(ApiConfig.BASE_URL + ApiConfig.CREATE_TASK, json.toString())
    }

    // ИСПРАВЛЕНО: status теперь nullable — null означает "не фильтровать по статусу"
    // Для студентов передаём status = "ACTIVE", для работодателей — null (все статусы)
    suspend fun getTasks(
        type: String? = null, employerId: Long? = null,
        status: String? = "ACTIVE", limit: Int = 50, offset: Int = 0
    ): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            type?.let { put("type", it) }
            employerId?.let { put("employer_id", it) }
            status?.let { put("status", it) }   // Если null — не отправляем, сервер покажет все статусы
            put("limit", limit); put("offset", offset)
        }
        postRequest(ApiConfig.BASE_URL + ApiConfig.GET_TASKS, json.toString())
    }

    suspend fun applyToTask(studentId: Long, taskId: Long): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("student_id", studentId); put("task_id", taskId) }
        postRequest(ApiConfig.BASE_URL + ApiConfig.APPLY_TASK, json.toString())
    }

    suspend fun getApplications(studentId: Long): ApiResponse = withContext(Dispatchers.IO) {
        postRequest(ApiConfig.BASE_URL + ApiConfig.GET_APPLICATIONS,
            JSONObject().apply { put("student_id", studentId) }.toString())
    }

    suspend fun getTaskApplications(taskId: Long, employerId: Long): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("task_id", taskId); put("employer_id", employerId) }
        postRequest(ApiConfig.BASE_URL + ApiConfig.GET_TASK_APPLICATIONS, json.toString())
    }

    suspend fun acceptApplication(applicationId: Long, employerId: Long, taskId: Long): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("application_id", applicationId); put("employer_id", employerId); put("task_id", taskId)
        }
        postRequest(ApiConfig.BASE_URL + ApiConfig.ACCEPT_APPLICATION, json.toString())
    }

    suspend fun rejectApplication(applicationId: Long, employerId: Long): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("application_id", applicationId); put("employer_id", employerId) }
        postRequest(ApiConfig.BASE_URL + ApiConfig.REJECT_APPLICATION, json.toString())
    }

    suspend fun getStudentProfile(studentId: Long): ApiResponse = withContext(Dispatchers.IO) {
        postRequest(ApiConfig.BASE_URL + ApiConfig.GET_STUDENT_PROFILE,
            JSONObject().apply { put("student_id", studentId) }.toString())
    }

    fun postJsonRaw(url: String, jsonBody: String): ApiResponse = postRequest(url, jsonBody)

    suspend fun getWorkspace(applicationId: Long, userId: Long): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("application_id", applicationId); put("user_id", userId) }
        postRequest(ApiConfig.BASE_URL + ApiConfig.GET_WORKSPACE, json.toString())
    }

    suspend fun sendMessage(applicationId: Long, senderId: Long, senderName: String, message: String): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("application_id", applicationId); put("sender_id", senderId)
            put("sender_name", senderName); put("message", message)
        }
        postRequest(ApiConfig.BASE_URL + ApiConfig.SEND_MESSAGE, json.toString())
    }

    suspend fun confirmCompletion(applicationId: Long, userId: Long): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("application_id", applicationId); put("user_id", userId) }
        postRequest(ApiConfig.BASE_URL + ApiConfig.CONFIRM_COMPLETION, json.toString())
    }

    private fun postRequest(url: String, jsonBody: String): ApiResponse {
        return try {
            val bodyBytes = jsonBody.toByteArray(Charsets.UTF_8)
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Content-Length", bodyBytes.size.toString())
                setRequestProperty("Connection", "close")
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 15_000
            }

            connection.outputStream.use { it.write(bodyBytes) }

            val responseCode = connection.responseCode
            val responseBody = try {
                val stream = if (responseCode in 200..299) connection.inputStream
                else (connection.errorStream ?: connection.inputStream)
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
            } catch (e: Exception) {
                ""
            }
            connection.disconnect()
            ApiResponse(responseCode, responseBody)
        } catch (e: Exception) {
            ApiResponse(-1, """{"success":false,"message":"${e.message?.replace("\"", "'")}"}""")
        }
    }
}
