package com.example.studentgigs.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "studentgigs.db"
        const val DATABASE_VERSION = 2

        // Таблица пользователей
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD_HASH = "password_hash"
        const val COLUMN_ROLE = "role"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_COMPANY_NAME = "company_name"
        const val COLUMN_COMPANY_POSITION = "company_position"
        const val COLUMN_CREATED_AT = "created_at"
        // Новые поля для верификации работодателя
        const val COLUMN_VERIFICATION_STATUS = "verification_status"
        const val COLUMN_PASSPORT_PHOTO_URL = "passport_photo_url"
        const val COLUMN_VERIFICATION_REQUESTED_AT = "verification_requested_at"

        // Таблица заданий
        const val TABLE_TASKS = "tasks"
        const val COLUMN_TASK_ID = "id"
        const val COLUMN_EMPLOYER_ID = "employer_id"
        const val COLUMN_TASK_TYPE = "type"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_REQUIREMENTS = "requirements"
        const val COLUMN_BENEFITS = "benefits"
        const val COLUMN_TAGS = "tags"
        const val COLUMN_PRICE = "price"
        const val COLUMN_PRICE_TYPE = "price_type"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_LOCATION_TYPE = "location_type"
        const val COLUMN_DEADLINE = "deadline"
        const val COLUMN_STATUS = "status"
        const val COLUMN_RESPONSES_COUNT = "responses_count"
        const val COLUMN_ICON_EMOJI = "icon_emoji"
        const val COLUMN_EMPLOYMENT_TYPE = "employment_type"
        const val COLUMN_SCHEDULE = "schedule"
        const val COLUMN_SERVICE_CATEGORY = "service_category"

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD_HASH TEXT NOT NULL,
                $COLUMN_ROLE TEXT NOT NULL,
                $COLUMN_FULL_NAME TEXT,
                $COLUMN_COMPANY_NAME TEXT,
                $COLUMN_COMPANY_POSITION TEXT,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_VERIFICATION_STATUS TEXT DEFAULT 'NOT_VERIFIED',
                $COLUMN_PASSPORT_PHOTO_URL TEXT,
                $COLUMN_VERIFICATION_REQUESTED_AT INTEGER
            )
        """.trimIndent()

        val createTasksTable = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMPLOYER_ID INTEGER NOT NULL,
                $COLUMN_TASK_TYPE TEXT NOT NULL,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_REQUIREMENTS TEXT,
                $COLUMN_BENEFITS TEXT,
                $COLUMN_TAGS TEXT,
                $COLUMN_PRICE TEXT NOT NULL,
                $COLUMN_PRICE_TYPE TEXT DEFAULT 'FIXED',
                $COLUMN_DURATION TEXT,
                $COLUMN_LOCATION TEXT,
                $COLUMN_LOCATION_TYPE TEXT DEFAULT 'REMOTE',
                $COLUMN_DEADLINE INTEGER,
                $COLUMN_STATUS TEXT DEFAULT 'ACTIVE',
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_RESPONSES_COUNT INTEGER DEFAULT 0,
                $COLUMN_ICON_EMOJI TEXT DEFAULT '📋',
                $COLUMN_EMPLOYMENT_TYPE TEXT,
                $COLUMN_SCHEDULE TEXT,
                $COLUMN_SERVICE_CATEGORY TEXT,
                FOREIGN KEY ($COLUMN_EMPLOYER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createTasksTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Добавляем новые столбцы для верификации
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_VERIFICATION_STATUS TEXT DEFAULT 'NOT_VERIFIED'")
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_PASSPORT_PHOTO_URL TEXT")
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_VERIFICATION_REQUESTED_AT INTEGER")

            // Создаем таблицу заданий
            val createTasksTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_TASKS (
                    $COLUMN_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_EMPLOYER_ID INTEGER NOT NULL,
                    $COLUMN_TASK_TYPE TEXT NOT NULL,
                    $COLUMN_TITLE TEXT NOT NULL,
                    $COLUMN_DESCRIPTION TEXT NOT NULL,
                    $COLUMN_REQUIREMENTS TEXT,
                    $COLUMN_BENEFITS TEXT,
                    $COLUMN_TAGS TEXT,
                    $COLUMN_PRICE TEXT NOT NULL,
                    $COLUMN_PRICE_TYPE TEXT DEFAULT 'FIXED',
                    $COLUMN_DURATION TEXT,
                    $COLUMN_LOCATION TEXT,
                    $COLUMN_LOCATION_TYPE TEXT DEFAULT 'REMOTE',
                    $COLUMN_DEADLINE INTEGER,
                    $COLUMN_STATUS TEXT DEFAULT 'ACTIVE',
                    $COLUMN_CREATED_AT INTEGER NOT NULL,
                    $COLUMN_RESPONSES_COUNT INTEGER DEFAULT 0,
                    $COLUMN_ICON_EMOJI TEXT DEFAULT '📋',
                    $COLUMN_EMPLOYMENT_TYPE TEXT,
                    $COLUMN_SCHEDULE TEXT,
                    $COLUMN_SERVICE_CATEGORY TEXT,
                    FOREIGN KEY ($COLUMN_EMPLOYER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
                )
            """.trimIndent()
            db.execSQL(createTasksTable)
        }
    }
}
