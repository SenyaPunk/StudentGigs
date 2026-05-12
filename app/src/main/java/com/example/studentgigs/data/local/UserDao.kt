package com.example.studentgigs.data.local

import android.content.ContentValues
import android.content.Context
import com.example.studentgigs.data.model.User
import com.example.studentgigs.data.model.UserRole
import com.example.studentgigs.data.model.VerificationStatus

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
            put(DatabaseHelper.COLUMN_VERIFICATION_STATUS, user.verificationStatus.name)
            put(DatabaseHelper.COLUMN_PASSPORT_PHOTO_URL, user.passportPhotoUrl)
            put(DatabaseHelper.COLUMN_VERIFICATION_REQUESTED_AT, user.verificationRequestedAt)
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
            put(DatabaseHelper.COLUMN_VERIFICATION_STATUS, user.verificationStatus.name)
            put(DatabaseHelper.COLUMN_PASSPORT_PHOTO_URL, user.passportPhotoUrl)
            put(DatabaseHelper.COLUMN_VERIFICATION_REQUESTED_AT, user.verificationRequestedAt)
        }
        return db.insertWithOnConflict(
            DatabaseHelper.TABLE_USERS,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    private fun parseUser(cursor: android.database.Cursor): User {
        val verificationStatusStr = try {
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VERIFICATION_STATUS))
        } catch (e: Exception) {
            "NOT_VERIFIED"
        }

        val passportPhotoUrl = try {
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSPORT_PHOTO_URL))
        } catch (e: Exception) {
            null
        }

        val verificationRequestedAt = try {
            val index = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VERIFICATION_REQUESTED_AT)
            if (cursor.isNull(index)) null else cursor.getLong(index)
        } catch (e: Exception) {
            null
        }

        return User(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
            passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD_HASH)),
            role = UserRole.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE))),
            fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FULL_NAME)),
            companyName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_NAME)),
            companyPosition = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_POSITION)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)),
            verificationStatus = try {
                VerificationStatus.valueOf(verificationStatusStr ?: "NOT_VERIFIED")
            } catch (e: Exception) {
                VerificationStatus.NOT_VERIFIED
            },
            passportPhotoUrl = passportPhotoUrl,
            verificationRequestedAt = verificationRequestedAt
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
            if (it.moveToFirst()) parseUser(it) else null
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
            if (it.moveToFirst()) parseUser(it) else null
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
            put(DatabaseHelper.COLUMN_VERIFICATION_STATUS, user.verificationStatus.name)
            put(DatabaseHelper.COLUMN_PASSPORT_PHOTO_URL, user.passportPhotoUrl)
            put(DatabaseHelper.COLUMN_VERIFICATION_REQUESTED_AT, user.verificationRequestedAt)
        }
        return db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(user.id.toString())
        )
    }

    // Обновление статуса верификации
    fun updateVerificationStatus(
        userId: Long,
        status: VerificationStatus,
        passportPhotoUrl: String? = null
    ): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_VERIFICATION_STATUS, status.name)
            if (passportPhotoUrl != null) {
                put(DatabaseHelper.COLUMN_PASSPORT_PHOTO_URL, passportPhotoUrl)
            }
            if (status == VerificationStatus.PENDING) {
                put(DatabaseHelper.COLUMN_VERIFICATION_REQUESTED_AT, System.currentTimeMillis())
            }
        }
        return db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(userId.toString())
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
