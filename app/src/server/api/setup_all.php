<?php
/**
 * setup_all.php — полная пересоздание БД StudentGigs.
 * Открыть в браузере: http://46.173.28.109/api/setup_all.php
 *
 * ВНИМАНИЕ: удаляет и пересоздаёт все таблицы — все данные будут потеряны!
 * Используйте только при чистой установке или полном сбросе.
 */

header('Content-Type: application/json; charset=utf-8');

$log  = [];
$errs = [];

// ── Соединение ────────────────────────────────────────────────────────────────
try {
    $pdo = new PDO(
        'mysql:host=localhost;dbname=student_gig_db;charset=utf8mb4',
        'gig_user',
        'Sena090909.',
        [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        ]
    );
    $log[] = 'БД: подключение установлено';
} catch (\Throwable $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Не удалось подключиться к БД: ' . $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// ── DDL ───────────────────────────────────────────────────────────────────────
$statements = [

    // ─ Пользователи ──────────────────────────────────────────────────────────
    "DROP TABLE IF EXISTS `messages`",
    "DROP TABLE IF EXISTS `task_files`",
    "DROP TABLE IF EXISTS `applications`",
    "DROP TABLE IF EXISTS `tasks`",
    "DROP TABLE IF EXISTS `users`",

    "CREATE TABLE `users` (
        `id`                         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        `email`                      VARCHAR(255) NOT NULL UNIQUE,
        `password_hash`              VARCHAR(255) NOT NULL,
        `role`                       ENUM('STUDENT','EMPLOYER') NOT NULL DEFAULT 'STUDENT',
        `full_name`                  VARCHAR(255),
        `company_name`               VARCHAR(255),
        `company_position`           VARCHAR(255),
        `verification_status`        ENUM('NOT_VERIFIED','PENDING','VERIFIED','REJECTED') NOT NULL DEFAULT 'NOT_VERIFIED',
        `passport_photo_url`         VARCHAR(500),
        `verification_requested_at`  BIGINT,
        `created_at`                 BIGINT NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

    // ─ Задания ───────────────────────────────────────────────────────────────
    "CREATE TABLE `tasks` (
        `id`               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        `employer_id`      BIGINT UNSIGNED NOT NULL,
        `type`             VARCHAR(50) NOT NULL DEFAULT 'TASK',
        `title`            VARCHAR(500) NOT NULL,
        `description`      TEXT,
        `requirements`     TEXT,
        `benefits`         TEXT,
        `tags`             TEXT,
        `price`            VARCHAR(100),
        `price_type`       VARCHAR(50) DEFAULT 'FIXED',
        `duration`         VARCHAR(255),
        `location`         VARCHAR(255),
        `location_type`    VARCHAR(50) DEFAULT 'REMOTE',
        `deadline`         BIGINT,
        `status`           ENUM('ACTIVE','CLOSED','COMPLETED') NOT NULL DEFAULT 'ACTIVE',
        `responses_count`  INT DEFAULT 0,
        `icon_emoji`       VARCHAR(10) DEFAULT '📋',
        `employment_type`  VARCHAR(50),
        `schedule`         VARCHAR(255),
        `service_category` VARCHAR(255),
        `created_at`       BIGINT NOT NULL,
        FOREIGN KEY (`employer_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

    // ─ Отклики ───────────────────────────────────────────────────────────────
    "CREATE TABLE `applications` (
        `id`                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        `task_id`             BIGINT UNSIGNED NOT NULL,
        `student_id`          BIGINT UNSIGNED NOT NULL,
        `status`              ENUM('PENDING','IN_PROGRESS','COMPLETED','REJECTED') NOT NULL DEFAULT 'PENDING',
        `student_confirmed`   TINYINT(1) NOT NULL DEFAULT 0,
        `employer_confirmed`  TINYINT(1) NOT NULL DEFAULT 0,
        `created_at`          BIGINT NOT NULL, -- <── ИСПРАВЛЕНО ТУТ (вместо applied_at)
        UNIQUE KEY `uq_task_student` (`task_id`, `student_id`),
        FOREIGN KEY (`task_id`)    REFERENCES `tasks`(`id`) ON DELETE CASCADE,
        FOREIGN KEY (`student_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

    // ─ Сообщения чата ────────────────────────────────────────────────────────
    "CREATE TABLE `messages` (
        `id`             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        `application_id` BIGINT UNSIGNED NOT NULL,
        `sender_id`      BIGINT UNSIGNED NOT NULL,
        `sender_name`    VARCHAR(255) NOT NULL DEFAULT '',
        `message`        TEXT NOT NULL,
        `created_at`     BIGINT NOT NULL,
        KEY `idx_app_time` (`application_id`, `created_at`),
        FOREIGN KEY (`application_id`) REFERENCES `applications`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

    // ─ Файлы задания ─────────────────────────────────────────────────────────
    "CREATE TABLE `task_files` (
        `id`             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        `application_id` BIGINT UNSIGNED NOT NULL,
        `uploader_id`    BIGINT UNSIGNED NOT NULL,
        `uploader_name`  VARCHAR(255) NOT NULL DEFAULT '',
        `file_name`      VARCHAR(500) NOT NULL,
        `original_name`  VARCHAR(500) NOT NULL DEFAULT '',
        `file_size`      BIGINT NOT NULL DEFAULT 0,
        `mime_type`      VARCHAR(255) NOT NULL DEFAULT 'application/octet-stream',
        `created_at`     BIGINT NOT NULL,
        KEY `idx_app` (`application_id`),
        FOREIGN KEY (`application_id`) REFERENCES `applications`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
];

foreach ($statements as $sql) {
    try {
        $pdo->exec($sql);
        $short = substr(trim($sql), 0, 60);
        $log[] = 'OK: ' . $short . '...';
    } catch (\Throwable $e) {
        $errs[] = 'ОШИБКА (' . substr(trim($sql), 0, 40) . '): ' . $e->getMessage();
    }
}

// ── Папка uploads ─────────────────────────────────────────────────────────────
$uploadsDir = __DIR__ . '/uploads';
if (!is_dir($uploadsDir)) {
    if (mkdir($uploadsDir, 0777, true)) {
        $log[] = 'uploads/: создана (0777)';
    } else {
        $errs[] = 'uploads/: не удалось создать директорию';
    }
} else {
    chmod($uploadsDir, 0777);
    $log[] = 'uploads/: уже существует, права обновлены до 0777';
}

// ── Результат ─────────────────────────────────────────────────────────────────
$ok = empty($errs);
echo json_encode([
    'success' => $ok,
    'message' => $ok ? 'Все таблицы созданы успешно!' : 'Завершено с ошибками',
    'data'    => [
        'log'    => $log,
        'errors' => $errs,
    ]
], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
exit;
