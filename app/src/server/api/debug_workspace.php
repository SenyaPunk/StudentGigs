<?php
/**
 * debug_workspace.php — диагностика сервера (GET из браузера).
 * Открыть: http://46.173.28.109/api/debug_workspace.php
 * УДАЛИТЬ с сервера после диагностики!
 */

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

$log = [];

$log[] = 'PHP version: ' . PHP_VERSION;
$log[] = 'REQUEST_METHOD: ' . $_SERVER['REQUEST_METHOD'];
$log[] = 'Content-Type header: ' . ($_SERVER['CONTENT_TYPE'] ?? '(не задан)');
$log[] = 'SCRIPT_FILENAME: ' . ($_SERVER['SCRIPT_FILENAME'] ?? '?');

// 1. Тест php://input
$rawInput = (string)file_get_contents('php://input');
$log[] = 'php://input байт: ' . strlen($rawInput);

// 2. Тест подключения к БД
try {
    $pdo = new PDO(
        'mysql:host=localhost;dbname=student_gig_db;charset=utf8mb4',
        'gig_user',
        'Sena090909.',
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC]
    );
    $log[] = 'БД: подключение успешно';

    // 3. Список таблиц
    $tables = $pdo->query("SHOW TABLES")->fetchAll(PDO::FETCH_COLUMN);
    $log[] = 'Таблицы: ' . (count($tables) ? implode(', ', $tables) : '(пусто — запустите setup_all.php)');

    foreach (['users', 'tasks', 'applications', 'messages', 'task_files'] as $tbl) {
        if (in_array($tbl, $tables)) {
            $cnt = $pdo->query("SELECT COUNT(*) FROM `$tbl`")->fetchColumn();
            $log[] = "$tbl: $cnt строк";
        } else {
            $log[] = "$tbl: ОТСУТСТВУЕТ";
        }
    }

    // 4. Проверка столбцов таблицы messages
    if (in_array('messages', $tables)) {
        $cols = $pdo->query("SHOW COLUMNS FROM messages")->fetchAll(PDO::FETCH_COLUMN);
        $log[] = 'messages.columns: ' . implode(', ', $cols);
    }

    // 5. Проверка столбцов applications (нужны student_confirmed, employer_confirmed)
    if (in_array('applications', $tables)) {
        $cols = $pdo->query("SHOW COLUMNS FROM applications")->fetchAll(PDO::FETCH_COLUMN);
        $log[] = 'applications.columns: ' . implode(', ', $cols);
        if (!in_array('student_confirmed', $cols)) {
            $log[] = 'ВНИМАНИЕ: applications.student_confirmed отсутствует — запустите setup_all.php';
        }
    }

    // 6. Тест вставки в messages (если есть хоть один отклик)
    if (in_array('messages', $tables) && in_array('applications', $tables)) {
        $testApp = $pdo->query("SELECT id FROM applications LIMIT 1")->fetchColumn();
        if ($testApp) {
            $log[] = 'Первый application_id для теста: ' . $testApp;
            $log[] = 'Отправьте POST на send_message.php с application_id=' . $testApp . ' для проверки чата';
        } else {
            $log[] = 'applications: нет строк (нужны тестовые аккаунты и отклик)';
        }
    }

} catch (\Throwable $e) {
    $log[] = 'ОШИБКА БД: ' . $e->getMessage();
}

// 7. Проверка папки uploads
$uploadsDir = __DIR__ . '/uploads';
if (is_dir($uploadsDir)) {
    $perms    = substr(sprintf('%o', fileperms($uploadsDir)), -4);
    $writable = is_writable($uploadsDir) ? 'доступна для записи' : 'НЕ доступна для записи';
    $log[] = "uploads/: $writable (права $perms)";
} else {
    $log[] = 'uploads/: ДИРЕКТОРИЯ НЕ СУЩЕСТВУЕТ';
}

// 8. Проверка include config.php
if (file_exists(__DIR__ . '/config.php')) {
    $log[] = 'config.php: существует';
} else {
    $log[] = 'config.php: ОТСУТСТВУЕТ';
}

echo json_encode([
    'success' => true,
    'message' => 'Диагностика завершена — удалите этот файл после проверки!',
    'data'    => ['log' => $log]
], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
exit;
