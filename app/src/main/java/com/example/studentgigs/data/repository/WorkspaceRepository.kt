package com.example.studentgigs.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.studentgigs.data.model.*
import com.example.studentgigs.data.remote.ApiClient
import com.example.studentgigs.data.remote.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class WorkspaceResult {
    data class Success(val data: Any? = null, val message: String = "") : WorkspaceResult()
    data class Error(val message: String) : WorkspaceResult()
}

data class WorkspaceData(
    val applicationId: Long,
    val studentId: Long,
    val employerId: Long,
    val taskTitle: String,
    val applicationStatus: ApplicationStatus,
    val studentConfirmed: Boolean,
    val employerConfirmed: Boolean,
    val messages: List<WorkspaceMessage>,
    val files: List<WorkspaceFile>
)

class WorkspaceRepository {

    private val apiClient = ApiClient()

    companion object {
        private const val TAG = "WorkspaceRepository"

        @Volatile
        private var INSTANCE: WorkspaceRepository? = null

        fun getInstance(): WorkspaceRepository =
            INSTANCE ?: synchronized(this) { INSTANCE ?: WorkspaceRepository().also { INSTANCE = it } }
    }

    suspend fun getWorkspace(applicationId: Long, userId: Long): WorkspaceResult =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("application_id", applicationId)
                    put("user_id", userId)
                }
                val response = apiClient.postJsonRaw(ApiConfig.BASE_URL + ApiConfig.GET_WORKSPACE, json.toString())
                val body = response.body.trim()
                if (body.isEmpty() || !body.startsWith("{"))
                    return@withContext WorkspaceResult.Error("Неверный формат ответа сервера: ${body.take(80)}")

                val root = JSONObject(body)
                if (!root.optBoolean("success", false))
                    return@withContext WorkspaceResult.Error(root.optString("message", "Ошибка"))

                val d = root.optJSONObject("data")
                    ?: return@withContext WorkspaceResult.Error("Нет данных в ответе")

                val msgs = mutableListOf<WorkspaceMessage>()
                val msgsArr = d.optJSONArray("messages")
                if (msgsArr != null) for (i in 0 until msgsArr.length()) {
                    val m = msgsArr.getJSONObject(i)
                    msgs.add(WorkspaceMessage(
                        id            = m.optLong("id"),
                        applicationId = m.optLong("application_id"),
                        senderId      = m.optLong("sender_id"),
                        senderName    = m.optString("sender_name", ""),
                        message       = m.optString("message", ""),
                        createdAt     = m.optLong("created_at")
                    ))
                }

                val files = mutableListOf<WorkspaceFile>()
                val filesArr = d.optJSONArray("files")
                if (filesArr != null) for (i in 0 until filesArr.length()) {
                    val f = filesArr.getJSONObject(i)
                    files.add(WorkspaceFile(
                        id            = f.optLong("id"),
                        applicationId = f.optLong("application_id"),
                        uploaderId    = f.optLong("uploader_id"),
                        uploaderName  = f.optString("uploader_name", ""),
                        fileName      = f.optString("file_name", ""),
                        originalName  = f.optString("original_name", ""),
                        fileSize      = f.optLong("file_size"),
                        mimeType      = f.optString("mime_type", "application/octet-stream"),
                        createdAt     = f.optLong("created_at"),
                        downloadUrl   = f.optString("download_url", "")
                    ))
                }

                val status = try {
                    ApplicationStatus.valueOf(d.optString("application_status", "IN_PROGRESS"))
                } catch (e: Exception) { ApplicationStatus.IN_PROGRESS }

                WorkspaceResult.Success(data = WorkspaceData(
                    applicationId     = d.optLong("application_id"),
                    studentId         = d.optLong("student_id"),
                    employerId        = d.optLong("employer_id"),
                    taskTitle         = d.optString("task_title", ""),
                    applicationStatus = status,
                    studentConfirmed  = d.optBoolean("student_confirmed", false),
                    employerConfirmed = d.optBoolean("employer_confirmed", false),
                    messages          = msgs,
                    files             = files
                ))
            } catch (e: Exception) {
                Log.e(TAG, "getWorkspace error", e)
                WorkspaceResult.Error(e.message ?: "Неизвестная ошибка")
            }
        }

    suspend fun sendMessage(
        applicationId: Long,
        senderId: Long,
        senderName: String,
        message: String
    ): WorkspaceResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("application_id", applicationId)
                put("sender_id", senderId)
                put("sender_name", senderName)
                put("message", message)
            }
            val response = apiClient.postJsonRaw(
                ApiConfig.BASE_URL + ApiConfig.SEND_MESSAGE,
                json.toString()
            )

            val body = response.body.trim()
            if (body.isEmpty()) {
                return@withContext WorkspaceResult.Error(
                    "Сервер вернул пустой ответ" +
                            "Откройте /api/setup_workspace.php для создания таблиц."
                )
            }
            if (!body.startsWith("{")) {
                return@withContext WorkspaceResult.Error(
                    "Неверный формат ответа сервера: ${body.take(120)}"
                )
            }

            val root = JSONObject(body)
            if (root.optBoolean("success", false))
                WorkspaceResult.Success()
            else
                WorkspaceResult.Error(root.optString("message", "Ошибка отправки"))

        } catch (e: Exception) {
            Log.e(TAG, "sendMessage error", e)
            WorkspaceResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        applicationId: Long,
        uploaderId: Long,
        uploaderName: String
    ): WorkspaceResult = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

            var originalName = "file"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) originalName = cursor.getString(idx) ?: "file"
                }
            }

            val fileBytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext WorkspaceResult.Error("Не удалось открыть файл")

            if (fileBytes.isEmpty()) {
                return@withContext WorkspaceResult.Error("Файл пустой")
            }
            if (fileBytes.size > 20 * 1024 * 1024) {
                return@withContext WorkspaceResult.Error("Файл слишком большой (макс. 20 МБ)")
            }

            val boundary = "WSBoundary${System.currentTimeMillis()}"

            // Строим тело запроса в памяти для корректного Content-Length
            // Используем UTF-8 для всех полей, включая кириллицу
            val bodyBuffer = ByteArrayOutputStream()

            fun writeUtf8(text: String) = bodyBuffer.write(text.toByteArray(Charsets.UTF_8))

            fun addField(name: String, value: String) {
                writeUtf8("--$boundary\r\n")
                writeUtf8("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
                writeUtf8("$value\r\n")
            }

            addField("application_id", applicationId.toString())
            addField("uploader_id", uploaderId.toString())
            addField("uploader_name", uploaderName)

            // Безопасное имя файла для заголовка (ASCII)
            val safeName = originalName
                .replace("\"", "_")
                .replace("\\", "_")
                // Оставляем только ASCII-печатаемые символы в имени файла для заголовка
                .map { if (it.code in 32..126) it else '_' }
                .joinToString("")

            writeUtf8("--$boundary\r\n")
            writeUtf8("Content-Disposition: form-data; name=\"file\"; filename=\"$safeName\"\r\n")
            writeUtf8("Content-Type: $mimeType\r\n\r\n")
            bodyBuffer.write(fileBytes)
            writeUtf8("\r\n--$boundary--\r\n")

            val bodyBytes = bodyBuffer.toByteArray()

            val connection = (URL(ApiConfig.BASE_URL + ApiConfig.UPLOAD_FILE)
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                setRequestProperty("Content-Length", bodyBytes.size.toString())
                setRequestProperty("Connection", "close")
                doOutput = true
                setFixedLengthStreamingMode(bodyBytes.size)
                connectTimeout = 30_000
                readTimeout    = 60_000
            }

            connection.outputStream.use { it.write(bodyBytes) }

            val code = connection.responseCode
            val responseBody = try {
                val stream = if (code in 200..299) connection.inputStream
                else (connection.errorStream ?: connection.inputStream)
                stream.bufferedReader(Charsets.UTF_8).readText()
            } catch (e: Exception) {
                ""
            }
            connection.disconnect()

            Log.d(TAG, "uploadFile code=$code body=${responseBody.take(200)}")

            if (responseBody.isBlank()) {
                return@withContext WorkspaceResult.Error(
                    "Сервер вернул пустой ответ (код $code). " +
                            "Проверьте upload_max_filesize и post_max_size в PHP конфиге."
                )
            }
            if (!responseBody.trimStart().startsWith("{")) {
                return@withContext WorkspaceResult.Error(
                    "Неверный ответ сервера: ${responseBody.take(120)}"
                )
            }

            val root = JSONObject(responseBody)
            if (root.optBoolean("success", false))
                WorkspaceResult.Success(message = "Файл загружен")
            else
                WorkspaceResult.Error(root.optString("message", "Ошибка загрузки"))

        } catch (e: Exception) {
            Log.e(TAG, "uploadFile error", e)
            WorkspaceResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    suspend fun confirmCompletion(applicationId: Long, userId: Long): WorkspaceResult =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("application_id", applicationId)
                    put("user_id", userId)
                }
                val response = apiClient.postJsonRaw(
                    ApiConfig.BASE_URL + ApiConfig.CONFIRM_COMPLETION,
                    json.toString()
                )

                val body = response.body.trim()
                if (body.isEmpty()) {
                    return@withContext WorkspaceResult.Error(
                        "Сервер вернул пустой ответ"
                    )
                }
                if (!body.startsWith("{")) {
                    return@withContext WorkspaceResult.Error(
                        "Неверный формат ответа: ${body.take(120)}"
                    )
                }

                val root = JSONObject(body)
                if (root.optBoolean("success", false))
                    WorkspaceResult.Success(message = root.optString("message", ""))
                else
                    WorkspaceResult.Error(root.optString("message", "Ошибка"))

            } catch (e: Exception) {
                Log.e(TAG, "confirmCompletion error", e)
                WorkspaceResult.Error(e.message ?: "Неизвестная ошибка")
            }
        }
}
