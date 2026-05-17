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

    // Application Endpoints (отклики студентов)
    const val APPLY_TASK = "apply_task.php"
    const val GET_APPLICATIONS = "get_applications.php"

    // Application Endpoints (работодатель)
    const val GET_TASK_APPLICATIONS = "get_task_applications.php"
    const val ACCEPT_APPLICATION = "accept_application.php"
    const val REJECT_APPLICATION = "reject_application.php"
    const val GET_STUDENT_PROFILE = "get_student_profile.php"

    // Workspace endpoints
    const val GET_WORKSPACE       = "get_workspace.php"
    const val SEND_MESSAGE        = "send_message.php"
    const val UPLOAD_FILE         = "upload_file.php"
    const val CONFIRM_COMPLETION  = "confirm_completion.php"
    const val SETUP_WORKSPACE     = "setup_workspace.php"
}
