package com.example.studentgigs.data.repository

import android.content.Context
import android.util.Log
import com.example.studentgigs.data.local.SessionManager
import com.example.studentgigs.data.local.UserDao
import com.example.studentgigs.data.model.User
import com.example.studentgigs.data.model.UserRole
import com.example.studentgigs.data.model.VerificationStatus
import com.example.studentgigs.data.remote.ApiClient
import com.example.studentgigs.data.remote.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class VerificationResult {
    data class Success(
        val status: VerificationStatus,
        val remainingTimeMs: Long = 0
    ) : VerificationResult()
    data class Error(val message: String) : VerificationResult()
}

class AuthRepository(context: Context) {
    private val userDao = UserDao(context)
    private val sessionManager = SessionManager.getInstance(context)
    private val apiClient = ApiClient()

    companion object {
        private const val TAG = "AuthRepository"

        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Регистрация студента
    suspend fun registerStudent(
        fullName: String,
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        // Валидация
        if (fullName.isBlank()) {
            return@withContext AuthResult.Error("Введите ФИО")
        }
        if (email.isBlank()) {
            return@withContext AuthResult.Error("Введите email")
        }
        if (!isValidEmail(email)) {
            return@withContext AuthResult.Error("Некорректный email")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Пароль должен содержать минимум 6 символов")
        }

        // Проверка локально
        if (userDao.isEmailExists(email)) {
            return@withContext AuthResult.Error("Пользователь с таким email уже существует")
        }

        // Попытка отправить на сервер
        val serverResult = try {
            val result = apiClient.register(
                email = email,
                password = password,
                role = "STUDENT",
                fullName = fullName
            )
            Log.d(TAG, "Ответ сервера на регистрацию: success=${result.success}, message=${result.message}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сервера при регистрации: ${e.message}", e)
            null
        }

        // Создаем пользователя локально
        val user = User(
            email = email,
            passwordHash = hashPassword(password),
            role = UserRole.STUDENT,
            fullName = fullName
        )

        val userId = if (serverResult?.success == true && serverResult.data != null) {
            val serverUserId = serverResult.data!!.optLong("user_id", 0)
            if (serverUserId > 0) {
                userDao.insertUserWithId(user.copy(id = serverUserId))
                serverUserId
            } else {
                userDao.insertUser(user)
            }
        } else {
            userDao.insertUser(user)
        }

        if (userId > 0) {
            val savedUser = user.copy(id = userId)
            saveSession(savedUser)
            AuthResult.Success(savedUser)
        } else {
            AuthResult.Error("Ошибка при регистрации")
        }
    }

    // Регистрация работодателя
    suspend fun registerEmployer(
        companyName: String,
        companyPosition: String,
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        // Валидация
        if (companyName.isBlank()) {
            return@withContext AuthResult.Error("Введите название компании")
        }
        if (companyPosition.isBlank()) {
            return@withContext AuthResult.Error("Введите должность")
        }
        if (email.isBlank()) {
            return@withContext AuthResult.Error("Введите email")
        }
        if (!isValidEmail(email)) {
            return@withContext AuthResult.Error("Некорректный email")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Пароль должен содержать минимум 6 символов")
        }

        // Проверка локально
        if (userDao.isEmailExists(email)) {
            return@withContext AuthResult.Error("Пользователь с таким email уже существует")
        }

        // Попытка отправить на сервер
        val serverResult = try {
            apiClient.register(
                email = email,
                password = password,
                role = "EMPLOYER",
                companyName = companyName,
                companyPosition = companyPosition
            )
        } catch (e: Exception) {
            Log.w(TAG, "Ошибка сервера при регистрации: ${e.message}")
            null
        }

        val user = User(
            email = email,
            passwordHash = hashPassword(password),
            role = UserRole.EMPLOYER,
            companyName = companyName,
            companyPosition = companyPosition
        )

        val userId = if (serverResult?.success == true && serverResult.data != null) {
            val serverUserId = serverResult.data!!.optLong("user_id", 0)
            if (serverUserId > 0) {
                userDao.insertUserWithId(user.copy(id = serverUserId))
                serverUserId
            } else {
                userDao.insertUser(user)
            }
        } else {
            userDao.insertUser(user)
        }

        if (userId > 0) {
            val savedUser = user.copy(id = userId)
            saveSession(savedUser)
            AuthResult.Success(savedUser)
        } else {
            AuthResult.Error("Ошибка при регистрации")
        }
    }

    // Вход в систему
    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext AuthResult.Error("Введите email")
        if (password.isBlank()) return@withContext AuthResult.Error("Введите пароль")

        // Всегда сначала идём на сервер — чтобы получить актуальный ID и статус верификации
        val serverResult = try {
            apiClient.login(email, password)
        } catch (e: Exception) {
            Log.w(TAG, "Сервер недоступен при входе: ${e.message}")
            null
        }

        if (serverResult?.success == true && serverResult.data != null) {
            val userResponse = UserResponse.fromJson(serverResult.data!!)
            if (userResponse != null && userResponse.id > 0) {
                val verificationStatus = try {
                    VerificationStatus.valueOf(userResponse.verificationStatus)
                } catch (e: Exception) {
                    VerificationStatus.NOT_VERIFIED
                }

                val user = User(
                    id = userResponse.id,
                    email = userResponse.email,
                    passwordHash = hashPassword(password),
                    role = UserRole.valueOf(userResponse.role),
                    fullName = userResponse.fullName,
                    companyName = userResponse.companyName,
                    companyPosition = userResponse.companyPosition,
                    verificationStatus = verificationStatus,
                    createdAt = userResponse.createdAt
                )

                // Сохраняем/обновляем локально с серверным ID
                userDao.insertUserWithId(user)
                saveSession(user)
                return@withContext AuthResult.Success(user)
            }
        }

        // Сервер недоступен — пробуем локально (офлайн-режим)
        val localUser = userDao.getUserByEmail(email)
        if (localUser != null) {
            if (localUser.passwordHash != hashPassword(password)) {
                return@withContext AuthResult.Error("Неверный пароль")
            }
            saveSession(localUser)
            Log.w(TAG, "Вход выполнен офлайн для userId=${localUser.id}")
            return@withContext AuthResult.Success(localUser)
        }

        AuthResult.Error("Пользователь не найден. Проверьте подключение к интернету")
    }

    // ВЫход из системы
    fun logout() {
        sessionManager.clearSession()
    }

    // Проверка авторизации
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    // Получение текущего пользователя
    fun getCurrentUser(): User? {
        val userId = sessionManager.getUserId()
        return if (userId > 0) {
            userDao.getUserById(userId)
        } else null
    }

    // Получение данных сессии
    fun getSessionManager(): SessionManager = sessionManager

    // Верификация работодателя - отправка фото паспорта
    suspend fun submitVerification(
        userId: Long,
        passportPhotoUrl: String
    ): VerificationResult = withContext(Dispatchers.IO) {
        if (passportPhotoUrl.isBlank()) {
            return@withContext VerificationResult.Error("Загрузите фото паспорта")
        }

        // Проверяем локально
        val user = userDao.getUserById(userId)
        if (user == null) {
            return@withContext VerificationResult.Error("Пользователь не найден")
        }
        if (user.role != UserRole.EMPLOYER) {
            return@withContext VerificationResult.Error("Верификация доступна только для работодателей")
        }
        if (user.verificationStatus == VerificationStatus.VERIFIED) {
            return@withContext VerificationResult.Error("Профиль уже верифицирован")
        }

        // Попытка отправить на сервер
        val serverResult = try {
            apiClient.verifyEmployer(userId, passportPhotoUrl)
        } catch (e: Exception) {
            Log.w(TAG, "Ошибка сервера при верификации: ${e.message}")
            null
        }

        if (serverResult?.success == false) {
            return@withContext VerificationResult.Error(serverResult.message ?: "Ошибка при верификации")
        }

        // Обновляем локально
        userDao.updateVerificationStatus(userId, VerificationStatus.PENDING, passportPhotoUrl)

        VerificationResult.Success(
            status = VerificationStatus.PENDING,
            remainingTimeMs = 60 * 1000L // 1 минута
        )
    }

    // Проверка статуса верификации
    suspend fun checkVerificationStatus(userId: Long): VerificationResult = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: run {
            return@withContext VerificationResult.Error("Пользователь не найден")
        }

        // Пробуем проверить на сервере
        val serverResult = try {
            apiClient.checkVerification(userId)
        } catch (e: Exception) {
            Log.w(TAG, "Ошибка сервера при проверке верификации: ${e.message}")
            null
        }

        if (serverResult?.success == true && serverResult.data != null) {
            val statusStr = serverResult.data!!.optString("verification_status", "NOT_VERIFIED")
            val remainingTime = serverResult.data!!.optLong("remaining_time_ms", 0)
            val status = try {
                VerificationStatus.valueOf(statusStr)
            } catch (e: Exception) {
                VerificationStatus.NOT_VERIFIED
            }

            // Обновляем локально
            if (status != user.verificationStatus) {
                userDao.updateVerificationStatus(userId, status)
            }

            return@withContext VerificationResult.Success(
                status = status,
                remainingTimeMs = remainingTime
            )
        }

        // Локальная проверка (если сервер недоступен)
        if (user.verificationStatus == VerificationStatus.PENDING && user.verificationRequestedAt != null) {
            val elapsed = System.currentTimeMillis() - user.verificationRequestedAt
            val oneMinuteMs = 60 * 1000L

            if (elapsed >= oneMinuteMs) {
                // Автоматически подтверждаем
                userDao.updateVerificationStatus(userId, VerificationStatus.VERIFIED)
                return@withContext VerificationResult.Success(
                    status = VerificationStatus.VERIFIED,
                    remainingTimeMs = 0
                )
            } else {
                return@withContext VerificationResult.Success(
                    status = VerificationStatus.PENDING,
                    remainingTimeMs = oneMinuteMs - elapsed
                )
            }
        }

        VerificationResult.Success(
            status = user.verificationStatus,
            remainingTimeMs = 0
        )
    }

    private fun saveSession(user: User) {
        sessionManager.saveUserSession(
            userId = user.id,
            email = user.email,
            role = user.role,
            displayName = user.displayName
        )
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
