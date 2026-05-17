<?php
ini_set('display_errors', '1');
error_reporting(E_ALL);
ob_start();
require_once 'config.php';
ob_end_clean();

header('Content-Type: application/json; charset=utf-8');

$log = [];

try {
    $pdo = getDbConnection();
    $log[] = 'DB: подключились успешно';

    // 1. Безопасное добавление колонок student_confirmed / employer_confirmed
    $stmt = $pdo->prepare("
        SELECT COLUMN_NAME
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME = 'applications' AND TABLE_SCHEMA = DATABASE()
    ");
    $stmt->execute();
    $existingColumns = $stmt->fetchAll(PDO::FETCH_COLUMN);

    if (!in_array('student_confirmed', $existingColumns)) {
        $pdo->exec("ALTER TABLE applications ADD COLUMN student_confirmed TINYINT(1) NOT NULL DEFAULT 0");
        $log[] = 'Колонка student_confirmed добавлена';
    } else {
        $log[] = 'Колонка student_confirmed уже существует';
    }

    if (!in_array('employer_confirmed', $existingColumns)) {
        $pdo->exec("ALTER TABLE applications ADD COLUMN employer_confirmed TINYINT(1) NOT NULL DEFAULT 0");
        $log[] = 'Колонка employer_confirmed добавлена';
    } else {
        $log[] = 'Колонка employer_confirmed уже существует';
    }

    // 2. Таблица сообщений
    $pdo->exec("CREATE TABLE IF NOT EXISTS messages (
        id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        application_id BIGINT UNSIGNED NOT NULL,
        sender_id      BIGINT UNSIGNED NOT NULL,
        sender_name    VARCHAR(255) NOT NULL DEFAULT '',
        message        TEXT NOT NULL,
        created_at     BIGINT NOT NULL,
        INDEX idx_messages_app  (application_id),
        INDEX idx_messages_time (application_id, created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
    $log[] = 'Таблица messages — OK';

    // 3. Таблица файлов задач
    $pdo->exec("CREATE TABLE IF NOT EXISTS task_files (
        id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        application_id BIGINT UNSIGNED NOT NULL,
        uploader_id    BIGINT UNSIGNED NOT NULL,
        uploader_name  VARCHAR(255) NOT NULL DEFAULT '',
        file_name      VARCHAR(512) NOT NULL,
        original_name  VARCHAR(512) NOT NULL,
        file_size      BIGINT NOT NULL DEFAULT 0,
        mime_type      VARCHAR(255) NOT NULL DEFAULT 'application/octet-stream',
        created_at     BIGINT NOT NULL,
        INDEX idx_files_app (application_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
    $log[] = 'Таблица task_files — OK';

    // 4. Папка uploads с правами 0777
    $uploadsDir = __DIR__ . '/uploads';
    if (!is_dir($uploadsDir)) {
        if (@mkdir($uploadsDir, 0777, true)) {
            $log[] = 'Папка uploads создана';
        } else {
            $log[] = 'ОШИБКА: не удалось создать папку uploads. Создайте вручную: mkdir api/uploads && chmod 777 api/uploads';
        }
    } else {
        $log[] = 'Папка uploads уже существует';
    }
    // Устанавливаем права в любом случае
    @chmod($uploadsDir, 0777);
    $writable = is_writable($uploadsDir) ? 'доступна для записи ✓' : 'НЕ доступна для записи ✗ — выполните: chmod 777 api/uploads/';
    $log[] = 'Папка uploads: ' . $writable . ' (права: ' . substr(sprintf('%o', fileperms($uploadsDir)), -4) . ')';

    sendResponse(true, 'Рабочая зона настроена успешно', ['log' => $log]);

} catch (Exception $e) {
    sendResponse(false, 'Ошибка: ' . $e->getMessage(), ['log' => $log]);
}
