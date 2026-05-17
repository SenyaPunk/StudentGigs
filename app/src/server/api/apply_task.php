<?php
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Метод не поддерживается');
}

$data = getJsonInput();

$studentId = $data['student_id'] ?? null;
$taskId    = $data['task_id']    ?? null;

if (!$studentId || !$taskId) {
    sendResponse(false, 'Не указан student_id или task_id');
}

try {
    $pdo = getDbConnection();

    // Проверяем, существует ли уже отклик
    $checkStmt = $pdo->prepare(
        "SELECT id, status FROM applications WHERE student_id = ? AND task_id = ?"
    );
    $checkStmt->execute([$studentId, $taskId]);
    $existing = $checkStmt->fetch(PDO::FETCH_ASSOC);

    if ($existing) {
        sendResponse(true, 'Отклик уже существует', [
            'applied'        => true,
            'application_id' => (int)$existing['id'],
            'status'         => $existing['status']
        ]);
    }

    // Проверяем, что задание существует и активно
    $taskStmt = $pdo->prepare("SELECT id, status FROM tasks WHERE id = ?");
    $taskStmt->execute([$taskId]);
    $task = $taskStmt->fetch(PDO::FETCH_ASSOC);

    if (!$task) {
        sendResponse(false, 'Задание не найдено');
    }
    if ($task['status'] !== 'ACTIVE') {
        sendResponse(false, 'Задание неактивно');
    }

    // Создаём отклик
    $pdo->beginTransaction();

    $insertStmt = $pdo->prepare(
        "INSERT INTO applications (student_id, task_id, status, created_at)
         VALUES (?, ?, 'PENDING', ?)"
    );
    $insertStmt->execute([$studentId, $taskId, (int)(microtime(true) * 1000)]);
    $applicationId = (int)$pdo->lastInsertId();

    // Увеличиваем счётчик откликов
    $pdo->prepare("UPDATE tasks SET responses_count = responses_count + 1 WHERE id = ?")
        ->execute([$taskId]);

    $pdo->commit();

    sendResponse(true, 'Вы успешно откликнулись', [
        'applied'        => true,
        'application_id' => $applicationId,
        'status'         => 'PENDING'
    ]);

} catch (PDOException $e) {
    if (isset($pdo) && $pdo->inTransaction()) $pdo->rollBack();
    sendResponse(false, 'Ошибка сервера: ' . $e->getMessage());
}
