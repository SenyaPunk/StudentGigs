<?php
/**
 * submit_review.php — отправить отзыв после завершения задания.
 * Один пользователь может оставить только один отзыв на одно задание.
 */
ini_set('display_errors', '0');
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

$input        = getJsonInput();
$reviewerId   = isset($input['reviewer_id'])   ? (int)$input['reviewer_id']   : 0;
$revieweeId   = isset($input['reviewee_id'])   ? (int)$input['reviewee_id']   : 0;
$applicationId= isset($input['application_id'])? (int)$input['application_id']: 0;
$taskId       = isset($input['task_id'])       ? (int)$input['task_id']       : 0;
$rating       = isset($input['rating'])        ? (int)$input['rating']        : 0;
$comment      = isset($input['comment'])       ? trim($input['comment'])      : '';
$reviewerRole = isset($input['reviewer_role']) ? strtoupper($input['reviewer_role']) : '';

if ($reviewerId <= 0 || $revieweeId <= 0 || $applicationId <= 0 || $taskId <= 0) {
    sendResponse(false, 'Неверные параметры');
}
if ($rating < 1 || $rating > 5) {
    sendResponse(false, 'Оценка должна быть от 1 до 5');
}
if (!in_array($reviewerRole, ['STUDENT', 'EMPLOYER'])) {
    sendResponse(false, 'Неверная роль рецензента');
}
if ($reviewerId === $revieweeId) {
    sendResponse(false, 'Нельзя оставить отзыв самому себе');
}

try {
    $pdo = getDbConnection();

    // Создаём таблицу если не существует
    $pdo->exec("
        CREATE TABLE IF NOT EXISTS reviews (
            id            INT AUTO_INCREMENT PRIMARY KEY,
            reviewer_id   INT NOT NULL,
            reviewee_id   INT NOT NULL,
            application_id INT NOT NULL,
            task_id       INT NOT NULL,
            rating        TINYINT NOT NULL,
            comment       TEXT,
            reviewer_role ENUM('STUDENT','EMPLOYER') NOT NULL,
            created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            UNIQUE KEY unique_review (reviewer_id, application_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    ");

    // Проверяем, что заявка завершена и reviewer является участником
    $checkApp = $pdo->prepare("
        SELECT a.status, a.student_id, t.employer_id
        FROM applications a
        JOIN tasks t ON t.id = a.task_id
        WHERE a.id = :aid
    ");
    $checkApp->execute([':aid' => $applicationId]);
    $app = $checkApp->fetch();

    if (!$app) {
        sendResponse(false, 'Заявка не найдена');
    }
    if ($app['status'] !== 'COMPLETED') {
        sendResponse(false, 'Отзыв можно оставить только после завершения задания');
    }
    $isParticipant = ($reviewerId == $app['student_id'] || $reviewerId == $app['employer_id']);
    if (!$isParticipant) {
        sendResponse(false, 'Доступ запрещён: вы не участник этого задания');
    }

    // Вставляем отзыв (ON DUPLICATE KEY — обновляем, если уже есть)
    $stmt = $pdo->prepare("
        INSERT INTO reviews (reviewer_id, reviewee_id, application_id, task_id, rating, comment, reviewer_role)
        VALUES (:rid, :reid, :aid, :tid, :rating, :comment, :role)
        ON DUPLICATE KEY UPDATE
            rating       = VALUES(rating),
            comment      = VALUES(comment),
            created_at   = CURRENT_TIMESTAMP
    ");
    $stmt->execute([
        ':rid'     => $reviewerId,
        ':reid'    => $revieweeId,
        ':aid'     => $applicationId,
        ':tid'     => $taskId,
        ':rating'  => $rating,
        ':comment' => $comment !== '' ? $comment : null,
        ':role'    => $reviewerRole,
    ]);

    sendResponse(true, 'Отзыв успешно сохранён', ['review_id' => $pdo->lastInsertId()]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка базы данных: ' . $e->getMessage());
} catch (Exception $e) {
    sendResponse(false, 'Ошибка сервера');
}
