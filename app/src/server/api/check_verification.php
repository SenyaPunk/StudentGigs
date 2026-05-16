<?php
/**
 * Проверка статуса верификации работодателя
 * Автоматически подтверждает через 1 минуту после подачи заявки
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

    // Получаем данные пользователя
    $stmt = $pdo->prepare("
        SELECT id, role, verification_status, verification_requested_at 
        FROM users WHERE id = ?
    ");
    $stmt->execute([$userId]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        sendResponse(false, 'Пользователь не найден');
    }

    if ($user['role'] !== 'EMPLOYER') {
        sendResponse(false, 'Верификация доступна только для работодателей');
    }

    $currentStatus = $user['verification_status'];
    $requestedAt = $user['verification_requested_at'];

    // Если статус PENDING, проверяем прошла ли минута
    if ($currentStatus === 'PENDING' && $requestedAt) {
        $currentTime = round(microtime(true) * 1000);
        $elapsedTime = $currentTime - $requestedAt;
        $oneMinuteInMs = 60 * 1000; // 1 минута в миллисекундах

        if ($elapsedTime >= $oneMinuteInMs) {
            // Автоматически подтверждаем
            $stmt = $pdo->prepare("
                UPDATE users 
                SET verification_status = 'VERIFIED' 
                WHERE id = ?
            ");
            $stmt->execute([$userId]);
            $currentStatus = 'VERIFIED';
        }
    }

    // Рассчитываем оставшееся время (если еще в ожидании)
    $remainingTime = 0;
    if ($currentStatus === 'PENDING' && $requestedAt) {
        $currentTime = round(microtime(true) * 1000);
        $elapsedTime = $currentTime - $requestedAt;
        $oneMinuteInMs = 60 * 1000;
        $remainingTime = max(0, $oneMinuteInMs - $elapsedTime);
    }

    sendResponse(true, 'Статус верификации получен', [
        'user_id' => (int)$userId,
        'verification_status' => $currentStatus,
        'remaining_time_ms' => (int)$remainingTime
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при проверке верификации: ' . $e->getMessage());
}
