<?php
/**
 * get_reviews.php — получить отзывы о пользователе + средний рейтинг.
 */
ini_set('display_errors', '0');
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

$input  = getJsonInput();
$userId = isset($input['user_id']) ? (int)$input['user_id'] : 0;

if ($userId <= 0) {
    sendResponse(false, 'Неверный user_id');
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

    // Получаем отзывы с именем рецензента и названием задания
    $stmt = $pdo->prepare("
        SELECT
            r.id,
            r.reviewer_id,
            r.reviewee_id,
            r.application_id,
            r.task_id,
            r.rating,
            r.comment,
            r.reviewer_role,
            r.created_at,
            COALESCE(u.full_name, u.company_name, u.email) AS reviewer_name,
            t.title AS task_title
        FROM reviews r
        LEFT JOIN users u ON u.id = r.reviewer_id
        LEFT JOIN tasks t ON t.id = r.task_id
        WHERE r.reviewee_id = :uid
        ORDER BY r.created_at DESC
    ");
    $stmt->execute([':uid' => $userId]);
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $totalCount = count($rows);
    $avgRating  = $totalCount > 0
        ? round(array_sum(array_column($rows, 'rating')) / $totalCount, 1)
        : 0.0;

    $reviews = array_map(function ($row) {
        $ts = $row['created_at'];
        $createdAtMs = is_numeric($ts) ? (int)$ts * 1000 : strtotime($ts) * 1000;
        return [
            'id'             => (int)$row['id'],
            'reviewer_id'    => (int)$row['reviewer_id'],
            'reviewee_id'    => (int)$row['reviewee_id'],
            'application_id' => (int)$row['application_id'],
            'task_id'        => (int)$row['task_id'],
            'rating'         => (int)$row['rating'],
            'comment'        => $row['comment'] ?? '',
            'reviewer_role'  => $row['reviewer_role'],
            'reviewer_name'  => $row['reviewer_name'] ?? 'Пользователь',
            'task_title'     => $row['task_title'] ?? '',
            'created_at'     => $createdAtMs,
        ];
    }, $rows);

    sendResponse(true, 'Отзывы получены', [
        'reviews'        => $reviews,
        'average_rating' => (float)$avgRating,
        'total_count'    => $totalCount,
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка базы данных: ' . $e->getMessage());
} catch (Exception $e) {
    sendResponse(false, 'Ошибка сервера');
}
