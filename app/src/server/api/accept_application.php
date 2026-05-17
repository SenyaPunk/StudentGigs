<?php
/**
 * accept_application.php — принять отклик студента.
 * Логика: один исполнитель, нельзя перевыбрать, автоотклонение остальных, задание закрывается.
 */
ini_set('display_errors', '0');
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

$input         = getJsonInput();
$applicationId = isset($input['application_id']) ? (int)$input['application_id'] : 0;
$employerId    = isset($input['employer_id'])    ? (int)$input['employer_id']    : 0;
$taskId        = isset($input['task_id'])        ? (int)$input['task_id']        : 0;

if ($applicationId <= 0 || $employerId <= 0 || $taskId <= 0) {
    sendResponse(false, 'Неверные параметры');
}

try {
    $pdo = getDbConnection();

    // 1. Задание принадлежит этому работодателю?
    $checkTask = $pdo->prepare("SELECT id, status FROM tasks WHERE id = :tid AND employer_id = :eid");
    $checkTask->execute([':tid' => $taskId, ':eid' => $employerId]);
    $task = $checkTask->fetch();
    if (!$task) {
        sendResponse(false, 'Задание не найдено или доступ запрещён');
    }

    // 2. Уже есть принятый исполнитель по этому заданию?
    $checkIP = $pdo->prepare(
        "SELECT id FROM applications WHERE task_id = :tid AND status = 'IN_PROGRESS'"
    );
    $checkIP->execute([':tid' => $taskId]);
    $existing = $checkIP->fetch();
    if ($existing) {
        if ((int)$existing['id'] === $applicationId) {
            sendResponse(true, 'Студент уже принят на задание');
        }
        sendResponse(false, 'Исполнитель уже выбран. Перевыбрать нельзя.');
    }

    // 3. Отклик существует для этого задания?
    $checkApp = $pdo->prepare(
        "SELECT id, status FROM applications WHERE id = :aid AND task_id = :tid"
    );
    $checkApp->execute([':aid' => $applicationId, ':tid' => $taskId]);
    $app = $checkApp->fetch();
    if (!$app) {
        sendResponse(false, 'Отклик не найден');
    }
    if ($app['status'] === 'REJECTED') {
        sendResponse(false, 'Нельзя принять отклонённый отклик');
    }

    // Всё проверено — начинаем транзакцию
    $pdo->beginTransaction();

    // 4. Принять выбранного студента
    $pdo->prepare(
        "UPDATE applications SET status = 'IN_PROGRESS', student_confirmed = 0, employer_confirmed = 0 WHERE id = :aid"
    )->execute([':aid' => $applicationId]);

    // 5. Отклонить всех остальных PENDING по этому заданию
    $pdo->prepare(
        "UPDATE applications SET status = 'REJECTED'
         WHERE task_id = :tid AND id != :aid AND status = 'PENDING'"
    )->execute([':tid' => $taskId, ':aid' => $applicationId]);

    // 6. Закрыть задание (исчезнет из фида студентов)
    $pdo->prepare("UPDATE tasks SET status = 'CLOSED' WHERE id = :tid")
        ->execute([':tid' => $taskId]);

    $pdo->commit();

    sendResponse(true, 'Студент принят. Задание закрыто для новых откликов.');

} catch (PDOException $e) {
    if (isset($pdo) && $pdo->inTransaction()) $pdo->rollBack();
    sendResponse(false, 'Ошибка базы данных: ' . $e->getMessage());
} catch (Exception $e) {
    if (isset($pdo) && $pdo->inTransaction()) $pdo->rollBack();
    sendResponse(false, 'Ошибка сервера: ' . $e->getMessage());
}
