package com.example.studentgigs.data.model

data class Review(
    val id: Long = 0,
    val reviewerId: Long = 0,
    val revieweeId: Long = 0,
    val applicationId: Long = 0,
    val taskId: Long = 0,
    val rating: Int = 5,
    val comment: String = "",
    val reviewerRole: String = "",
    val reviewerName: String = "",
    val taskTitle: String = "",
    val createdAt: Long = 0L
)

data class PendingReview(
    val revieweeId: Long,
    val revieweeName: String,
    val applicationId: Long,
    val taskId: Long,
    val taskTitle: String = "",
    val reviewerRole: String
)
