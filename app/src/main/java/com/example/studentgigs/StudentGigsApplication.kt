package com.example.studentgigs

import android.app.Application
import com.example.studentgigs.data.local.DatabaseHelper
import com.example.studentgigs.data.local.SessionManager

class StudentGigsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Инициализация базы данных при запуске приложения
        DatabaseHelper.getInstance(this)

        // Инициализация менеджера сессий
        SessionManager.getInstance(this)
    }
}
