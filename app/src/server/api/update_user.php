<?php
/**
 * Обновление данных пользователя
 */

error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Метод не поддерживается');
}

$data = getJsonInput();

$userId = $data['user_id'] ?? 0;

if (empty($userId)) {
    sendResponse(false, 'ID пользователя обязателен');
}

try {
    $pdo = getDbConnection();

    // Проверка существования пользователя
    $stmt = $pdo->prepare("SELECT id FROM users WHERE id = ?");
    $stmt->execute([$userId]);

    if (!$stmt->fetch()) {
        sendResponse(false, 'Пользователь не найден');
    }

    // Формирование запроса обновления
    $updates = [];
    $params = [];

    if (isset($data['full_name'])) {
        $updates[] = 'full_name = ?';
        $params[] = $data['full_name'];
    }

    if (isset($data['company_name'])) {
        $updates[] = 'company_name = ?';
        $params[] = $data['company_name'];
    }

    if (isset($data['company_position'])) {
        $updates[] = 'company_position = ?';
        $params[] = $data['company_position'];
    }

    if (empty($updates)) {
        sendResponse(false, 'Нет данных для обновления');
    }

    $params[] = $userId;

    $sql = "UPDATE users SET " . implode(', ', $updates) . " WHERE id = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);

    sendResponse(true, 'Данные обновлены');

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при обновлении данных');
}
