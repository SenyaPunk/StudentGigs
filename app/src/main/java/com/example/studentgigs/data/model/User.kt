package com.example.studentgigs.data.model

enum class UserRole {
    STUDENT,
    EMPLOYER
}

// Статус верификации работодателя
enum class VerificationStatus {
    NOT_VERIFIED,      // Не верифицирован
    PENDING,           // Ожидает проверки
    VERIFIED           // Верифицирован
}

data class User(
    val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    // Общие поля
    val createdAt: Long = System.currentTimeMillis(),
    // Поля для студента
    val fullName: String? = null,
    // Поля для работодателя
    val companyName: String? = null,
    val companyPosition: String? = null,
    // Верификация работодателя
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_VERIFIED,
    val passportPhotoUrl: String? = null,
    val verificationRequestedAt: Long? = null
) {
    val displayName: String
        get() = when (role) {
            UserRole.STUDENT -> fullName ?: "Студент"
            UserRole.EMPLOYER -> companyName ?: "Работодатель"
        }

    // Имя без фамилии
    val shortName: String
        get() = when (role) {
            UserRole.STUDENT -> fullName?.split(" ")?.getOrNull(1) ?: "Гость"
            UserRole.EMPLOYER -> companyName ?: "Компания"
        }

    // Фамилия и Имя для профиля
    val profileName: String
        get() = when (role) {
            UserRole.STUDENT -> {
                val parts = fullName?.split(" ") ?: emptyList()
                if (parts.size >= 2) "${parts[0]} ${parts[1]}" else fullName ?: "Студент"
            }
            UserRole.EMPLOYER -> companyName ?: "Работодатель"
        }

    // Проверка, верифицирован ли работодатель
    val isEmployerVerified: Boolean
        get() = role == UserRole.EMPLOYER && verificationStatus == VerificationStatus.VERIFIED

    // Требуется ли верификация (только для работодателей)
    val needsVerification: Boolean
        get() = role == UserRole.EMPLOYER && verificationStatus == VerificationStatus.NOT_VERIFIED

    // Ожидает ли проверки
    val isPendingVerification: Boolean
        get() = role == UserRole.EMPLOYER && verificationStatus == VerificationStatus.PENDING
}
