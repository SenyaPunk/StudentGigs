<?php
/**
 * reject_application.php — отклонить отклик студента (работодатель).
 * Нельзя отозвать принятого исполнителя (IN_PROGRESS).
 */
ini_set('display_errors', '0');
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

$input         = getJsonInput();
$applicationId = isset($input['application_id']) ? (int)$input['application_id'] : 0;
$employerId    = isset($input['employer_id'])    ? (int)$input['employer_id']    : 0;

if ($applicationId <= 0 || $employerId <= 0) {
    sendResponse(false, 'Неверные параметры');
}

try {
    $pdo = getDbConnection();

    $check = $pdo->prepare("
        SELECT a.id, a.status
        FROM applications a
        JOIN tasks t ON t.id = a.task_id
        WHERE a.id = :aid AND t.employer_id = :eid
    ");
    $check->execute([':aid' => $applicationId, ':eid' => $employerId]);
    $row = $check->fetch();

    if (!$row) {
        sendResponse(false, 'Отклик не найден или доступ запрещён');
    }
    if ($row['status'] === 'REJECTED') {
        sendResponse(true, 'Отклик уже отклонён');
    }
    if ($row['status'] === 'IN_PROGRESS') {
        sendResponse(false, 'Нельзя отозвать принятого исполнителя. Задание уже в работе.');
    }
    if ($row['status'] === 'COMPLETED') {
        sendResponse(false, 'Задание уже завершено');
    }

    $pdo->prepare("UPDATE applications SET status = 'REJECTED' WHERE id = :aid")
        ->execute([':aid' => $applicationId]);

    sendResponse(true, 'Отклик отклонён');

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка базы данных: ' . $e->getMessage());
} catch (Exception $e) {
    sendResponse(false, 'Ошибка сервера');
}
