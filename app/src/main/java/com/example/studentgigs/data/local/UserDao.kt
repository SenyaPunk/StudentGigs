package com.example.studentgigs.data.local

import android.content.ContentValues
import android.content.Context
import com.example.studentgigs.data.model.User
import com.example.studentgigs.data.model.UserRole

class UserDao(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    fun insertUser(user: User): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_EMAIL, user.email.lowercase().trim())
            put(DatabaseHelper.COLUMN_PASSWORD_HASH, user.passwordHash)
            put(DatabaseHelper.COLUMN_ROLE, user.role.name)
            put(DatabaseHelper.COLUMN_FULL_NAME, user.fullName)
            put(DatabaseHelper.COLUMN_COMPANY_NAME, user.companyName)
            put(DatabaseHelper.COLUMN_COMPANY_POSITION, user.companyPosition)
            put(DatabaseHelper.COLUMN_CREATED_AT, user.createdAt)
        }
        return db.insert(DatabaseHelper.TABLE_USERS, null, values)
    }

    // Вставка пользователя с конкретным ID
    fun insertUserWithId(user: User): Long {
        val db = dbHelper.writableDatabase

        val existingUser = getUserById(user.id)
        if (existingUser != null) {
            updateUser(user)
            return user.id
        }

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, user.id)
            put(DatabaseHelper.COLUMN_EMAIL, user.email.lowercase().trim())
            put(DatabaseHelper.COLUMN_PASSWORD_HASH, user.passwordHash)
            put(DatabaseHelper.COLUMN_ROLE, user.role.name)
            put(DatabaseHelper.COLUMN_FULL_NAME, user.fullName)
            put(DatabaseHelper.COLUMN_COMPANY_NAME, user.companyName)
            put(DatabaseHelper.COLUMN_COMPANY_POSITION, user.companyPosition)
            put(DatabaseHelper.COLUMN_CREATED_AT, user.createdAt)
        }
        return db.insertWithOnConflict(
            DatabaseHelper.TABLE_USERS,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getUserByEmail(email: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_EMAIL} = ?",
            arrayOf(email.lowercase().trim()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    email = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
                    passwordHash = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD_HASH)),
                    role = UserRole.valueOf(it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE))),
                    fullName = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FULL_NAME)),
                    companyName = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_NAME)),
                    companyPosition = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_POSITION)),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT))
                )
            } else null
        }
    }

    fun getUserById(id: Long): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    email = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
                    passwordHash = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD_HASH)),
                    role = UserRole.valueOf(it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE))),
                    fullName = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FULL_NAME)),
                    companyName = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_NAME)),
                    companyPosition = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_POSITION)),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT))
                )
            } else null
        }
    }

    fun isEmailExists(email: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_ID),
            "${DatabaseHelper.COLUMN_EMAIL} = ?",
            arrayOf(email.lowercase().trim()),
            null,
            null,
            null
        )
        return cursor.use { it.count > 0 }
    }

    fun updateUser(user: User): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_FULL_NAME, user.fullName)
            put(DatabaseHelper.COLUMN_COMPANY_NAME, user.companyName)
            put(DatabaseHelper.COLUMN_COMPANY_POSITION, user.companyPosition)
        }
        return db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(user.id.toString())
        )
    }

    fun deleteUser(userId: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_USERS,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(userId.toString())
        )
    }
}
