<?php
/**
 * Получение данных пользователя по ID
 */

require_once 'config.php';

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

    $stmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
    $stmt->execute([$userId]);
    $user = $stmt->fetch();

    if (!$user) {
        sendResponse(false, 'Пользователь не найден');
    }

    sendResponse(true, 'Пользователь найден', [
        'id' => (int)$user['id'],
        'email' => $user['email'],
        'role' => $user['role'],
        'full_name' => $user['full_name'],
        'company_name' => $user['company_name'],
        'company_position' => $user['company_position'],
        'created_at' => (int)$user['created_at']
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при получении данных');
}
