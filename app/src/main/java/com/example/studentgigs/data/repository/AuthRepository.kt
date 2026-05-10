package com.example.studentgigs.data.repository

import android.content.Context
import android.util.Log
import com.example.studentgigs.data.local.SessionManager
import com.example.studentgigs.data.local.UserDao
import com.example.studentgigs.data.model.User
import com.example.studentgigs.data.model.UserRole
import com.example.studentgigs.data.remote.ApiClient
import com.example.studentgigs.data.remote.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
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
            val serverUserId = serverResult.data.optLong("user_id", 0)
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
            val serverUserId = serverResult.data.optLong("user_id", 0)
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
        if (email.isBlank()) {
            return@withContext AuthResult.Error("Введите email")
        }
        if (password.isBlank()) {
            return@withContext AuthResult.Error("Введите пароль")
        }

        // Сначала пробуем локально
        val localUser = userDao.getUserByEmail(email)

        if (localUser != null) {
            if (localUser.passwordHash != hashPassword(password)) {
                return@withContext AuthResult.Error("Неверный пароль")
            }
            saveSession(localUser)
            return@withContext AuthResult.Success(localUser)
        }

        // Если локально нет - пробуем с сервера
        val serverResult = try {
            apiClient.login(email, password)
        } catch (e: Exception) {
            Log.w(TAG, "Ошибка сервера при входе: ${e.message}")
            null
        }

        if (serverResult?.success == true && serverResult.data != null) {
            val userResponse = UserResponse.fromJson(serverResult.data)
            if (userResponse != null) {
                val user = User(
                    id = userResponse.id,
                    email = userResponse.email,
                    passwordHash = hashPassword(password),
                    role = UserRole.valueOf(userResponse.role),
                    fullName = userResponse.fullName,
                    companyName = userResponse.companyName,
                    companyPosition = userResponse.companyPosition,
                    createdAt = userResponse.createdAt
                )
                // Сохраняем локально для оффлайн доступа
                userDao.insertUserWithId(user)
                saveSession(user)
                return@withContext AuthResult.Success(user)
            }
        }

        AuthResult.Error("Пользователь не найден")
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
