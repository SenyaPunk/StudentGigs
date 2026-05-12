package com.example.studentgigs.data.remote


object ApiConfig {
    const val BASE_URL = "http://46.173.28.109/api/"

    // Auth Endpoints
    const val REGISTER = "register.php"
    const val LOGIN = "login.php"
    const val UPDATE_USER = "update_user.php"
    const val GET_USER = "get_user.php"

    // Verification Endpoints
    const val VERIFY_EMPLOYER = "verify_employer.php"
    const val CHECK_VERIFICATION = "check_verification.php"

    // Task Endpoints
    const val CREATE_TASK = "create_task.php"
    const val GET_TASKS = "get_tasks.php"
}
