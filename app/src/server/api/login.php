<?php
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Метод не поддерживается');
}

$data = getJsonInput();

$email    = trim(strtolower($data['email'] ?? ''));
$password = $data['password'] ?? '';

if (empty($email) || empty($password)) {
    sendResponse(false, 'Email и пароль обязательны');
}

try {
    $pdo = getDbConnection();

    $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();

    if (!$user) {
        sendResponse(false, 'Пользователь не найден');
    }

    if ($user['password_hash'] !== hash('sha256', $password)) {
        sendResponse(false, 'Неверный пароль');
    }

    sendResponse(true, 'Авторизация успешна', array(
        'id'                  => (int)$user['id'],
        'email'               => $user['email'],
        'role'                => $user['role'],
        'full_name'           => $user['full_name'],
        'company_name'        => $user['company_name'],
        'company_position'    => $user['company_position'],
        'verification_status' => $user['verification_status'] ?? 'NOT_VERIFIED',
        'created_at'          => (int)$user['created_at']
    ));

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при авторизации');
}