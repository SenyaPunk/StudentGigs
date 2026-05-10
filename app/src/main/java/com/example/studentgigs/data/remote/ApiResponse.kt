package com.example.studentgigs.data.remote

import org.json.JSONObject

// Модель ответов от сервера
data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: JSONObject? = null
) {
    companion object {
        fun fromJson(json: String): ApiResponse {
            return try {
                val jsonObject = JSONObject(json)
                ApiResponse(
                    success = jsonObject.optBoolean("success", false),
                    message = jsonObject.optString("message", "Неизвестная ошибка"),
                    data = jsonObject.optJSONObject("data")
                )
            } catch (e: Exception) {
                ApiResponse(false, "Ошибка парсинга ответа: ${e.message}")
            }
        }
    }
}

data class UserResponse(
    val id: Long,
    val email: String,
    val role: String,
    val fullName: String?,
    val companyName: String?,
    val companyPosition: String?,
    val createdAt: Long
) {
    companion object {
        fun fromJson(json: JSONObject): UserResponse? {
            return try {
                UserResponse(
                    id = json.optLong("id", 0),
                    email = json.optString("email", ""),
                    role = json.optString("role", "STUDENT"),
                    fullName = json.optString("full_name", null),
                    companyName = json.optString("company_name", null),
                    companyPosition = json.optString("company_position", null),
                    createdAt = json.optLong("created_at", System.currentTimeMillis())
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
