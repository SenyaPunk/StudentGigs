package com.example.studentgigs.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentgigs.data.model.User
import com.example.studentgigs.data.model.UserRole
import com.example.studentgigs.data.model.VerificationStatus
import com.example.studentgigs.data.repository.AuthRepository
import com.example.studentgigs.data.repository.AuthResult
import com.example.studentgigs.data.repository.VerificationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val registrationSuccess: Boolean = false,
    val loginSuccess: Boolean = false,

    val verificationStatus: VerificationStatus = VerificationStatus.NOT_VERIFIED,
    val isVerificationLoading: Boolean = false,
    val verificationRemainingTime: Long = 0
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository.getInstance(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            val currentUser = if (isLoggedIn) authRepository.getCurrentUser() else null
            _uiState.value = _uiState.value.copy(
                isLoggedIn = isLoggedIn,
                currentUser = currentUser
            )
        }
    }

    fun registerStudent(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Пароли не совпадают")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = withContext(Dispatchers.IO) {
                authRepository.registerStudent(fullName, email, password)
            }

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = result.user,
                        registrationSuccess = true,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun registerEmployer(
        companyName: String,
        companyPosition: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Пароли не совпадают")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = withContext(Dispatchers.IO) {
                authRepository.registerEmployer(companyName, companyPosition, email, password)
            }

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = result.user,
                        registrationSuccess = true,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = withContext(Dispatchers.IO) {
                authRepository.login(email, password)
            }

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = result.user,
                        loginSuccess = true,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                authRepository.logout()
            }
            _uiState.value = AuthUiState(
                isLoggedIn = false,
                currentUser = null
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearRegistrationSuccess() {
        _uiState.value = _uiState.value.copy(registrationSuccess = false)
    }

    fun clearLoginSuccess() {
        _uiState.value = _uiState.value.copy(loginSuccess = false)
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                authRepository.getCurrentUser()
            }
            _uiState.value = _uiState.value.copy(currentUser = user)
        }
    }



    fun submitVerification(imageUri: String) {
        val userId = uiState.value.currentUser?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerificationLoading = true, error = null)

            // Вызываем РЕАЛЬНЫЙ репозиторий
            val result = authRepository.submitVerification(userId, imageUri)

            when (result) {
                is VerificationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isVerificationLoading = false,
                        verificationStatus = result.status,
                        verificationRemainingTime = result.remainingTimeMs
                    )
                    // После отправки начинаем проверку статуса
                    checkVerificationStatus()
                }
                is VerificationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isVerificationLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun checkVerificationStatus() {
        val userId = uiState.value.currentUser?.id ?: return

        viewModelScope.launch {
            val result = authRepository.checkVerificationStatus(userId)

            if (result is VerificationResult.Success) {
                _uiState.value = _uiState.value.copy(
                    verificationStatus = result.status,
                    verificationRemainingTime = result.remainingTimeMs
                )

                // ЕСЛИ ВЕРИФИКАЦИЯ ЗАВЕРШЕНА — ОБНОВЛЯЕМ ПОЛЬЗОВАТЕЛЯ
                if (result.status == VerificationStatus.VERIFIED) {
                    refreshCurrentUser()
                } else if (result.status == VerificationStatus.PENDING) {
                    // Если все еще ждем, проверяем снова через 5 секунд
                    delay(5000)
                    checkVerificationStatus()
                }
            }
        }
    }

    fun clearVerificationError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
