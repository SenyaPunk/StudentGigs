package com.example.studentgigs.data.model

// Тип задания
enum class TaskType {
    SERVICE,    // Услуга
    VACANCY,    // Вакансия
    TASK        // Задание
}

// Статус задания
enum class TaskStatus {
    ACTIVE,     // Активно
    CLOSED,     // Закрыто
    DRAFT       // Черновик
}

// Модель задания/вакансии/услуги
data class Task(
    val id: Long = 0,
    val employerId: Long,
    val employerName: String,
    val employerPosition: String? = null,
    val type: TaskType,
    val title: String,
    val description: String,
    val requirements: List<String> = emptyList(),
    val benefits: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val price: String,                      // Цена/Зарплата
    val priceType: PriceType = PriceType.FIXED,
    val duration: String,                   // Срок выполнения
    val location: String,                   // Место работы
    val locationType: LocationType = LocationType.REMOTE,
    val deadline: Long? = null,             // Дедлайн
    val status: TaskStatus = TaskStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val responsesCount: Int = 0,            // Количество откликов
    val iconEmoji: String = "📋",
    // Дополнительные поля для вакансии
    val employmentType: EmploymentType? = null,  // Тип занятости
    val schedule: String? = null,                 // График работы
    // Дополнительные поля для услуги
    val serviceCategory: String? = null,


)

// Тип оплаты
enum class PriceType {
    FIXED,          // Фиксированная
    HOURLY,         // Почасовая
    NEGOTIABLE,     // Договорная
    PER_PROJECT     // За проект
}

// Тип локации
enum class LocationType {
    REMOTE,         // Удалённо
    OFFICE,         // Офис
    HYBRID          // Гибрид
}

// Тип занятости (для вакансий)
enum class EmploymentType {
    FULL_TIME,      // Полная занятость
    PART_TIME,      // Частичная занятость
    INTERNSHIP,     // Стажировка
    PROJECT         // Проектная работа
}
