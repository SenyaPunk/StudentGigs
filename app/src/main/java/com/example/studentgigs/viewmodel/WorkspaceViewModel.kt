package com.example.studentgigs.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentgigs.data.model.ApplicationStatus
import com.example.studentgigs.data.model.WorkspaceUiState
import com.example.studentgigs.data.repository.WorkspaceData
import com.example.studentgigs.data.repository.WorkspaceRepository
import com.example.studentgigs.data.repository.WorkspaceResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkspaceViewModel : ViewModel() {

    private val repository = WorkspaceRepository.getInstance()

    private val _uiState = MutableStateFlow(WorkspaceUiState())
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var currentAppId: Long = 0
    private var currentUserId: Long = 0

    companion object {
        private const val TAG = "WorkspaceViewModel"
        private const val POLL_INTERVAL_MS = 5_000L
    }

    fun startWorkspace(applicationId: Long, userId: Long) {
        currentAppId = applicationId
        currentUserId = userId
        _uiState.value = WorkspaceUiState(isLoading = true)
        loadWorkspace(showLoading = true)
        startPolling()
    }

    fun stopWorkspace() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(POLL_INTERVAL_MS)
                if (!_uiState.value.isCompleted) {
                    loadWorkspace(showLoading = false)
                }
            }
        }
    }

    private fun loadWorkspace(showLoading: Boolean) {
        viewModelScope.launch {
            if (showLoading) _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.getWorkspace(currentAppId, currentUserId)) {
                is WorkspaceResult.Success -> {
                    val data = result.data as? WorkspaceData ?: return@launch
                    _uiState.value = _uiState.value.copy(
                        isLoading         = false,
                        messages          = data.messages,
                        files             = data.files,
                        studentId         = data.studentId,
                        employerId        = data.employerId,
                        studentConfirmed  = data.studentConfirmed,
                        employerConfirmed = data.employerConfirmed,
                        isCompleted       = data.applicationStatus == ApplicationStatus.COMPLETED
                    )
                    if (data.applicationStatus == ApplicationStatus.COMPLETED) stopWorkspace()
                }
                is WorkspaceResult.Error -> {
                    if (showLoading) {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                    }
                    Log.w(TAG, "loadWorkspace silent error: ${result.message}")
                }
            }
        }
    }

    fun sendMessage(applicationId: Long, senderId: Long, senderName: String, message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingMessage = true)
            when (val result = repository.sendMessage(applicationId, senderId, senderName, message)) {
                is WorkspaceResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSendingMessage = false)
                    loadWorkspace(showLoading = false)
                }
                is WorkspaceResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSendingMessage = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun uploadFile(context: Context, uri: Uri, applicationId: Long, uploaderId: Long, uploaderName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingFile = true, uploadError = null)
            when (val result = repository.uploadFile(context, uri, applicationId, uploaderId, uploaderName)) {
                is WorkspaceResult.Success -> {
                    _uiState.value = _uiState.value.copy(isUploadingFile = false)
                    loadWorkspace(showLoading = false)
                }
                is WorkspaceResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUploadingFile = false,
                        uploadError = result.message
                    )
                }
            }
        }
    }

    fun confirmCompletion(applicationId: Long, userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConfirmingCompletion = true)
            when (val result = repository.confirmCompletion(applicationId, userId)) {
                is WorkspaceResult.Success -> {
                    _uiState.value = _uiState.value.copy(isConfirmingCompletion = false)
                    loadWorkspace(showLoading = false)
                }
                is WorkspaceResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isConfirmingCompletion = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearUploadError() { _uiState.value = _uiState.value.copy(uploadError = null) }
}
