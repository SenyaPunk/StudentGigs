package com.example.studentgigs.data.remote

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

    // ВЫполение POST запроса
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
