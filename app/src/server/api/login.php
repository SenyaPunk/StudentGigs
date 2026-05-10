<?php
/**
 * Авторизация пользователя
 */

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Метод не поддерживается');
}

$data = getJsonInput();

$email = trim(strtolower($data['email'] ?? ''));
$password = $data['password'] ?? '';

if (empty($email) || empty($password)) {
    sendResponse(false, 'Email и пароль обязательны');
}

try {
    $pdo = getDbConnection();

    // Поиск пользователя
    $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();

    if (!$user) {
        sendResponse(false, 'Пользователь не найден');
    }

    // Проверка пароля
    $passwordHash = hash('sha256', $password);

    if ($user['password_hash'] !== $passwordHash) {
        sendResponse(false, 'Неверный пароль');
    }

    // Успешная авторизация
    sendResponse(true, 'Авторизация успешна', [
        'id' => (int)$user['id'],
        'email' => $user['email'],
        'role' => $user['role'],
        'full_name' => $user['full_name'],
        'company_name' => $user['company_name'],
        'company_position' => $user['company_position'],
        'created_at' => (int)$user['created_at']
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при авторизации');
}
