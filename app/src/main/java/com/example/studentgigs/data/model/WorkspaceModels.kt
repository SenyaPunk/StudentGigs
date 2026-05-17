package com.example.studentgigs.data.model

data class WorkspaceMessage(
    val id: Long = 0,
    val applicationId: Long,
    val senderId: Long,
    val senderName: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class WorkspaceFile(
    val id: Long = 0,
    val applicationId: Long,
    val uploaderId: Long,
    val uploaderName: String,
    val fileName: String,
    val originalName: String,
    val fileSize: Long = 0,
    val mimeType: String = "application/octet-stream",
    val createdAt: Long = System.currentTimeMillis(),
    val downloadUrl: String = ""
)

data class WorkspaceUiState(
    val isLoading: Boolean = false,
    val messages: List<WorkspaceMessage> = emptyList(),
    val files: List<WorkspaceFile> = emptyList(),
    val studentId: Long = 0,
    val employerId: Long = 0,
    val studentConfirmed: Boolean = false,
    val employerConfirmed: Boolean = false,
    val isCompleted: Boolean = false,
    val isSendingMessage: Boolean = false,
    val isUploadingFile: Boolean = false,
    val isConfirmingCompletion: Boolean = false,
    val error: String? = null,
    val uploadError: String? = null
)
