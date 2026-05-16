package com.example.studentgigs.data.remote

import org.json.JSONObject

data class ApiResponse(
    val statusCode: Int,
    val body: String
) {
    private val json: JSONObject? by lazy {
        try { JSONObject(body) } catch (e: Exception) { null }
    }

    val isSuccess: Boolean get() = statusCode in 200..299

    // Обратная совместимость — AuthRepository и TaskRepository используют эти поля
    val success: Boolean get() = json?.optBoolean("success", false) ?: false
    val message: String get() = json?.optString("message", "Неизвестная ошибка") ?: "Неизвестная ошибка"
    val data: JSONObject? get() = json?.optJSONObject("data")

    fun toJsonObject(): JSONObject? = json

    companion object {
        fun error(message: String) = ApiResponse(
            statusCode = -1,
            body = """{"success":false,"message":"$message"}"""
        )
    }
}

data class UserResponse(
    val id: Long,
    val email: String,
    val role: String,
    val fullName: String?,
    val companyName: String?,
    val companyPosition: String?,
    val verificationStatus: String,
    val createdAt: Long
) {
    companion object {
        fun fromJson(json: JSONObject): UserResponse? {
            return try {
                UserResponse(
                    id = json.optLong("id", 0),
                    email = json.optString("email", ""),
                    role = json.optString("role", "STUDENT"),
                    fullName = json.optString("full_name", "").takeIf { it.isNotEmpty() },
                    companyName = json.optString("company_name", "").takeIf { it.isNotEmpty() },
                    companyPosition = json.optString("company_position", "").takeIf { it.isNotEmpty() },
                    verificationStatus = json.optString("verification_status", "NOT_VERIFIED"),
                    createdAt = json.optLong("created_at", System.currentTimeMillis())
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}