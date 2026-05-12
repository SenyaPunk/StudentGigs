package com.example.studentgigs.data.remote

import com.example.studentgigs.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// HTTP клиент для работы с API на сервере
class ApiClient {

    // Регистрация
    suspend fun register(
        email: String,
        password: String,
        role: String,
        fullName: String? = null,
        companyName: String? = null,
        companyPosition: String? = null
    ): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("role", role)
            fullName?.let { put("full_name", it) }
            companyName?.let { put("company_name", it) }
            companyPosition?.let { put("company_position", it) }
        }

        postRequest(ApiConfig.BASE_URL + ApiConfig.REGISTER, json.toString())
    }

    // Вход
    suspend fun login(email: String, password: String): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        postRequest(ApiConfig.BASE_URL + ApiConfig.LOGIN, json.toString())
    }

    // Получение данных по айди
    suspend fun getUser(userId: Long): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("user_id", userId)
        }

        postRequest(ApiConfig.BASE_URL + ApiConfig.GET_USER, json.toString())
    }

    // Обновление данных
    suspend fun updateUser(
        userId: Long,
        fullName: String? = null,
        companyName: String? = null,
        companyPosition: String? = null
    ): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("user_id", userId)
            fullName?.let { put("full_name", it) }
            companyName?.let { put("company_name", it) }
            companyPosition?.let { put("company_position", it) }
        }

        postRequest(ApiConfig.BASE_URL + ApiConfig.UPDATE_USER, json.toString())
    }

    // Верификация работодателя
    suspend fun verifyEmployer(
        userId: Long,
        passportPhotoUrl: String
    ): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("user_id", userId)
            put("passport_photo_url", passportPhotoUrl)
        }

        postRequest(ApiConfig.BASE_URL + ApiConfig.VERIFY_EMPLOYER, json.toString())
    }

    // Проверка статуса верификации
    suspend fun checkVerification(userId: Long): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("user_id", userId)
        }

        postRequest(ApiConfig.BASE_URL + ApiConfig.CHECK_VERIFICATION, json.toString())
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
    ): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("employer_id", employerId)
            put("type", type.name)
            put("title", title)
            put("description", description)
            put("price", price)
            put("requirements", requirements.joinToString("|"))
            put("benefits", benefits.joinToString("|"))
            put("tags", tags.joinToString("|"))
            put("price_type", priceType.name)
            put("duration", duration)
            put("location", location)
            put("location_type", locationType.name)
            deadline?.let { put("deadline", it) }
            put("icon_emoji", iconEmoji)
            employmentType?.let { put("employment_type", it.name) }
            schedule?.let { put("schedule", it) }
            serviceCategory?.let { put("service_category", it) }
        }

        postRequest(ApiConfig.BASE_URL + ApiConfig.CREATE_TASK, json.toString())
    }

    // Получение списка заданий
    suspend fun getTasks(
        type: TaskType? = null,
        employerId: Long? = null,
        status: TaskStatus = TaskStatus.ACTIVE,
        limit: Int = 50,
        offset: Int = 0
    ): ApiResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            type?.let { put("type", it.name) }
            employerId?.let { put("employer_id", it) }
            put("status", status.name)
            put("limit", limit)
            put("offset", offset)
        }

        postRequest(ApiConfig.BASE_URL + ApiConfig.GET_TASKS, json.toString())
    }

    // Выполение POST запроса
    private fun postRequest(urlString: String, jsonBody: String): ApiResponse {
        android.util.Log.d(TAG, "POST запрос: $urlString")
        android.util.Log.d(TAG, "Тело запроса: $jsonBody")

        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                doInput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            // Отправка данных
            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }

            // Чтение ответа
            val responseCode = connection.responseCode
            android.util.Log.d(TAG, "HTTP код ответа: $responseCode")

            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                reader.readText()
            }

            android.util.Log.d(TAG, "Ответ сервера: $response")

            connection.disconnect()

            ApiResponse.fromJson(response)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Ошибка сети: ${e.message}", e)
            ApiResponse(false, "Ошибка сети: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ApiClient"
    }
}
