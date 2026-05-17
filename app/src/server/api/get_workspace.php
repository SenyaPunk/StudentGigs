<?php
ini_set('display_errors', '0');
ini_set('log_errors', '1');
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

header('Content-Type: application/json; charset=utf-8');

$input         = getJsonInput();
$applicationId = isset($input['application_id']) ? (int)$input['application_id'] : 0;
$userId        = isset($input['user_id'])        ? (int)$input['user_id']        : 0;

if ($applicationId <= 0 || $userId <= 0) {
    sendResponse(false, 'Неверные параметры');
}

try {
    $pdo = getDbConnection();

    $appStmt = $pdo->prepare("
        SELECT a.id, a.student_id, a.task_id, a.status,
               a.student_confirmed, a.employer_confirmed,
               t.employer_id, t.title AS task_title
        FROM applications a
        JOIN tasks t ON t.id = a.task_id
        WHERE a.id = :app_id
    ");
    $appStmt->execute([':app_id' => $applicationId]);
    $app = $appStmt->fetch();

    if (!$app) {
        sendResponse(false, 'Заявка не найдена (id=' . $applicationId . ')');
    }

    if ($userId != $app['student_id'] && $userId != $app['employer_id']) {
        sendResponse(false, 'Доступ запрещён');
    }

    // Проверяем наличие таблицы messages
    $tableCheck = $pdo->query("SHOW TABLES LIKE 'messages'")->fetchAll();
    $messages   = [];
    if (!empty($tableCheck)) {
        $msgStmt = $pdo->prepare("
            SELECT id, application_id, sender_id, sender_name, message, created_at
            FROM messages
            WHERE application_id = :app_id
            ORDER BY created_at ASC
            LIMIT 300
        ");
        $msgStmt->execute([':app_id' => $applicationId]);
        while ($row = $msgStmt->fetch()) {
            $messages[] = [
                'id'             => (int)$row['id'],
                'application_id' => (int)$row['application_id'],
                'sender_id'      => (int)$row['sender_id'],
                'sender_name'    => $row['sender_name'],
                'message'        => $row['message'],
                'created_at'     => (int)$row['created_at'],
            ];
        }
    }

    $proto   = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
    $host    = $_SERVER['HTTP_HOST'] ?? 'localhost';
    $apiDir  = rtrim(dirname($_SERVER['SCRIPT_NAME']), '/');
    $baseUrl = $proto . '://' . $host . $apiDir;

    // Проверяем наличие таблицы task_files
    $tableCheck2 = $pdo->query("SHOW TABLES LIKE 'task_files'")->fetchAll();
    $files       = [];
    if (!empty($tableCheck2)) {
        $fileStmt = $pdo->prepare("
            SELECT id, application_id, uploader_id, uploader_name,
                   file_name, original_name, file_size, mime_type, created_at
            FROM task_files
            WHERE application_id = :app_id
            ORDER BY created_at DESC
        ");
        $fileStmt->execute([':app_id' => $applicationId]);
        while ($row = $fileStmt->fetch()) {
            $files[] = [
                'id'             => (int)$row['id'],
                'application_id' => (int)$row['application_id'],
                'uploader_id'    => (int)$row['uploader_id'],
                'uploader_name'  => $row['uploader_name'],
                'file_name'      => $row['file_name'],
                'original_name'  => $row['original_name'],
                'file_size'      => (int)$row['file_size'],
                'mime_type'      => $row['mime_type'],
                'created_at'     => (int)$row['created_at'],
                'download_url'   => $baseUrl . '/uploads/' . $row['file_name'],
            ];
        }
    }

    sendResponse(true, 'OK', [
        'application_id'     => (int)$app['id'],
        'student_id'         => (int)$app['student_id'],
        'employer_id'        => (int)$app['employer_id'],
        'task_title'         => $app['task_title'],
        'application_status' => $app['status'],
        'student_confirmed'  => (bool)$app['student_confirmed'],
        'employer_confirmed' => (bool)$app['employer_confirmed'],
        'messages'           => $messages,
        'files'              => $files,
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка базы данных: ' . $e->getMessage());
} catch (Exception $e) {
    sendResponse(false, 'Ошибка сервера: ' . $e->getMessage());
}
