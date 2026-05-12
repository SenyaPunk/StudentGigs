package com.example.studentgigs.data.local

import android.content.ContentValues
import android.content.Context
import com.example.studentgigs.data.model.*

class TaskDao(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    fun insertTask(task: Task): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_EMPLOYER_ID, task.employerId)
            put(DatabaseHelper.COLUMN_TASK_TYPE, task.type.name)
            put(DatabaseHelper.COLUMN_TITLE, task.title)
            put(DatabaseHelper.COLUMN_DESCRIPTION, task.description)
            put(DatabaseHelper.COLUMN_REQUIREMENTS, task.requirements.joinToString("|"))
            put(DatabaseHelper.COLUMN_BENEFITS, task.benefits.joinToString("|"))
            put(DatabaseHelper.COLUMN_TAGS, task.tags.joinToString("|"))
            put(DatabaseHelper.COLUMN_PRICE, task.price)
            put(DatabaseHelper.COLUMN_PRICE_TYPE, task.priceType.name)
            put(DatabaseHelper.COLUMN_DURATION, task.duration)
            put(DatabaseHelper.COLUMN_LOCATION, task.location)
            put(DatabaseHelper.COLUMN_LOCATION_TYPE, task.locationType.name)
            put(DatabaseHelper.COLUMN_DEADLINE, task.deadline)
            put(DatabaseHelper.COLUMN_STATUS, task.status.name)
            put(DatabaseHelper.COLUMN_CREATED_AT, task.createdAt)
            put(DatabaseHelper.COLUMN_RESPONSES_COUNT, task.responsesCount)
            put(DatabaseHelper.COLUMN_ICON_EMOJI, task.iconEmoji)
            put(DatabaseHelper.COLUMN_EMPLOYMENT_TYPE, task.employmentType?.name)
            put(DatabaseHelper.COLUMN_SCHEDULE, task.schedule)
            put(DatabaseHelper.COLUMN_SERVICE_CATEGORY, task.serviceCategory)
        }
        return db.insert(DatabaseHelper.TABLE_TASKS, null, values)
    }

    private fun parseTask(cursor: android.database.Cursor): Task {
        val requirementsStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUIREMENTS))
        val benefitsStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BENEFITS))
        val tagsStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TAGS))
        val employmentTypeStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMPLOYMENT_TYPE))

        val deadlineIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEADLINE)
        val deadline = if (cursor.isNull(deadlineIndex)) null else cursor.getLong(deadlineIndex)

        return Task(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_ID)),
            employerId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMPLOYER_ID)),
            type = TaskType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_TYPE))),
            title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)),
            requirements = if (requirementsStr.isNullOrBlank()) emptyList() else requirementsStr.split("|"),
            benefits = if (benefitsStr.isNullOrBlank()) emptyList() else benefitsStr.split("|"),
            tags = if (tagsStr.isNullOrBlank()) emptyList() else tagsStr.split("|"),
            price = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE)),
            priceType = PriceType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE_TYPE))),
            duration = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION)) ?: "",
            location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION)) ?: "",
            locationType = LocationType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION_TYPE))),
            deadline = deadline,
            status = TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS))),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)),
            responsesCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESPONSES_COUNT)),
            iconEmoji = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON_EMOJI)) ?: "📋",
            employmentType = if (employmentTypeStr.isNullOrBlank()) null else EmploymentType.valueOf(employmentTypeStr),
            schedule = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE)),
            serviceCategory = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_CATEGORY)),
            employerName = "Заказчик"
        )
    }

    fun getTaskById(id: Long): Task? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_TASKS,
            null,
            "${DatabaseHelper.COLUMN_TASK_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) parseTask(it) else null
        }
    }

    fun getTasksByEmployerId(employerId: Long): List<Task> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_TASKS,
            null,
            "${DatabaseHelper.COLUMN_EMPLOYER_ID} = ?",
            arrayOf(employerId.toString()),
            null,
            null,
            "${DatabaseHelper.COLUMN_CREATED_AT} DESC"
        )

        val tasks = mutableListOf<Task>()
        cursor.use {
            while (it.moveToNext()) {
                tasks.add(parseTask(it))
            }
        }
        return tasks
    }

    fun getAllActiveTasks(): List<Task> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_TASKS,
            null,
            "${DatabaseHelper.COLUMN_STATUS} = ?",
            arrayOf(TaskStatus.ACTIVE.name),
            null,
            null,
            "${DatabaseHelper.COLUMN_CREATED_AT} DESC"
        )

        val tasks = mutableListOf<Task>()
        cursor.use {
            while (it.moveToNext()) {
                tasks.add(parseTask(it))
            }
        }
        return tasks
    }

    fun getTasksByType(type: TaskType): List<Task> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_TASKS,
            null,
            "${DatabaseHelper.COLUMN_TASK_TYPE} = ? AND ${DatabaseHelper.COLUMN_STATUS} = ?",
            arrayOf(type.name, TaskStatus.ACTIVE.name),
            null,
            null,
            "${DatabaseHelper.COLUMN_CREATED_AT} DESC"
        )

        val tasks = mutableListOf<Task>()
        cursor.use {
            while (it.moveToNext()) {
                tasks.add(parseTask(it))
            }
        }
        return tasks
    }

    fun updateTask(task: Task): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TITLE, task.title)
            put(DatabaseHelper.COLUMN_DESCRIPTION, task.description)
            put(DatabaseHelper.COLUMN_REQUIREMENTS, task.requirements.joinToString("|"))
            put(DatabaseHelper.COLUMN_BENEFITS, task.benefits.joinToString("|"))
            put(DatabaseHelper.COLUMN_TAGS, task.tags.joinToString("|"))
            put(DatabaseHelper.COLUMN_PRICE, task.price)
            put(DatabaseHelper.COLUMN_PRICE_TYPE, task.priceType.name)
            put(DatabaseHelper.COLUMN_DURATION, task.duration)
            put(DatabaseHelper.COLUMN_LOCATION, task.location)
            put(DatabaseHelper.COLUMN_LOCATION_TYPE, task.locationType.name)
            put(DatabaseHelper.COLUMN_DEADLINE, task.deadline)
            put(DatabaseHelper.COLUMN_STATUS, task.status.name)
            put(DatabaseHelper.COLUMN_ICON_EMOJI, task.iconEmoji)
            put(DatabaseHelper.COLUMN_EMPLOYMENT_TYPE, task.employmentType?.name)
            put(DatabaseHelper.COLUMN_SCHEDULE, task.schedule)
            put(DatabaseHelper.COLUMN_SERVICE_CATEGORY, task.serviceCategory)
        }
        return db.update(
            DatabaseHelper.TABLE_TASKS,
            values,
            "${DatabaseHelper.COLUMN_TASK_ID} = ?",
            arrayOf(task.id.toString())
        )
    }

    fun updateTaskStatus(taskId: Long, status: TaskStatus): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_STATUS, status.name)
        }
        return db.update(
            DatabaseHelper.TABLE_TASKS,
            values,
            "${DatabaseHelper.COLUMN_TASK_ID} = ?",
            arrayOf(taskId.toString())
        )
    }

    fun deleteTask(taskId: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_TASKS,
            "${DatabaseHelper.COLUMN_TASK_ID} = ?",
            arrayOf(taskId.toString())
        )
    }
}
