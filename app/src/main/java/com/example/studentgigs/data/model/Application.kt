package com.example.studentgigs.data.model

enum class ApplicationStatus {
    PENDING,      // Студент откликнулся (ожидает решения работодателя)
    IN_PROGRESS,  // В работе (работодатель принял)
    COMPLETED,    // Завершено
    REJECTED      // Отклонено / отозвано работодателем
}

data class Application(
    val applicationId: Long = 0,
    val studentId: Long,
    val taskId: Long,
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val appliedAt: Long = System.currentTimeMillis(),
    // Для студента — информация о задании
    val task: Task? = null,
    // Для работодателя — информация о студенте
    val student: User? = null
)
