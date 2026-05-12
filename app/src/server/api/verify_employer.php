<?php
/**
 * Верификация работодателя - загрузка паспорта
 */

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Метод не поддерживается');
}

$data = getJsonInput();

$userId = $data['user_id'] ?? 0;
$passportPhotoUrl = $data['passport_photo_url'] ?? '';

if (empty($userId)) {
    sendResponse(false, 'ID пользователя обязателен');
}

if (empty($passportPhotoUrl)) {
    sendResponse(false, 'URL фото паспорта обязателен');
}

try {
    $pdo = getDbConnection();

    // Проверка существования пользователя и его роли
    $stmt = $pdo->prepare("SELECT id, role, verification_status FROM users WHERE id = ?");
    $stmt->execute([$userId]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        sendResponse(false, 'Пользователь не найден');
    }

    if ($user['role'] !== 'EMPLOYER') {
        sendResponse(false, 'Верификация доступна только для работодателей');
    }

    if ($user['verification_status'] === 'VERIFIED') {
        sendResponse(false, 'Профиль уже верифицирован');
    }

    // Обновляем статус на PENDING и сохраняем URL фото
    $verificationRequestedAt = round(microtime(true) * 1000);
    
    $stmt = $pdo->prepare("
        UPDATE users 
        SET verification_status = 'PENDING', 
            passport_photo_url = ?,
            verification_requested_at = ?
        WHERE id = ?
    ");
    $stmt->execute([$passportPhotoUrl, $verificationRequestedAt, $userId]);

    sendResponse(true, 'Заявка на верификацию отправлена', [
        'user_id' => (int)$userId,
        'verification_status' => 'PENDING',
        'verification_requested_at' => $verificationRequestedAt
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при верификации: ' . $e->getMessage());
}
