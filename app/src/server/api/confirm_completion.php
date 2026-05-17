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
        SELECT a.id, a.student_id, a.status, a.task_id,
               a.student_confirmed, a.employer_confirmed,
               t.employer_id
        FROM applications a
        JOIN tasks t ON t.id = a.task_id
        WHERE a.id = :app_id
    ");
    $appStmt->execute([':app_id' => $applicationId]);
    $app = $appStmt->fetch();

    if (!$app) {
        sendResponse(false, 'Заявка не найдена');
    }

    if ($app['status'] === 'COMPLETED') {
        sendResponse(true, 'Задание уже завершено', [
            'student_confirmed'  => true,
            'employer_confirmed' => true,
            'both_confirmed'     => true,
            'status'             => 'COMPLETED',
        ]);
    }

    if ($app['status'] !== 'IN_PROGRESS') {
        sendResponse(false, 'Задание не активно (статус: ' . $app['status'] . ')');
    }

    $isStudent  = ($userId == $app['student_id']);
    $isEmployer = ($userId == $app['employer_id']);

    if (!$isStudent && !$isEmployer) {
        sendResponse(false, 'Доступ запрещён: userId=' . $userId
            . ' studentId=' . $app['student_id']
            . ' employerId=' . $app['employer_id']);
    }

    if ($isStudent) {
        $pdo->prepare("UPDATE applications SET student_confirmed = 1 WHERE id = :id")
            ->execute([':id' => $applicationId]);
    } else {
        $pdo->prepare("UPDATE applications SET employer_confirmed = 1 WHERE id = :id")
            ->execute([':id' => $applicationId]);
    }

    $reload = $pdo->prepare("SELECT student_confirmed, employer_confirmed FROM applications WHERE id = :id");
    $reload->execute([':id' => $applicationId]);
    $upd = $reload->fetch();

    $bothConfirmed = (bool)$upd['student_confirmed'] && (bool)$upd['employer_confirmed'];
    $status        = 'IN_PROGRESS';

    if ($bothConfirmed) {
        $pdo->prepare("UPDATE applications SET status = 'COMPLETED' WHERE id = :id")
            ->execute([':id' => $applicationId]);
        // Задание COMPLETED — появляется в 'Завершённые' у работодателя
        $pdo->prepare("UPDATE tasks SET status = 'COMPLETED' WHERE id = :tid")
            ->execute([':tid' => $app['task_id']]);
        $status = 'COMPLETED';
    }

    $msg = $bothConfirmed
        ? 'Задание успешно завершено!'
        : ($isStudent
            ? 'Ваше подтверждение принято. Ожидаем подтверждения от работодателя.'
            : 'Ваше подтверждение принято. Ожидаем подтверждения от студента.');

    sendResponse(true, $msg, [
        'student_confirmed'  => (bool)$upd['student_confirmed'],
        'employer_confirmed' => (bool)$upd['employer_confirmed'],
        'both_confirmed'     => $bothConfirmed,
        'status'             => $status,
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка базы данных: ' . $e->getMessage());
} catch (Exception $e) {
    sendResponse(false, 'Ошибка сервера: ' . $e->getMessage());
}
