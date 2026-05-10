<?php
/**
 * Регистрация нового пользователя
 */

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Метод не поддерживается');
}

$data = getJsonInput();

// Валидация обязательных полей
$email = trim(strtolower($data['email'] ?? ''));
$password = $data['password'] ?? '';
$role = strtoupper($data['role'] ?? 'STUDENT');

if (empty($email) || empty($password)) {
    sendResponse(false, 'Email и пароль обязательны');
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    sendResponse(false, 'Некорректный email');
}

if (strlen($password) < 6) {
    sendResponse(false, 'Пароль должен содержать минимум 6 символов');
}

if (!in_array($role, ['STUDENT', 'EMPLOYER'])) {
    sendResponse(false, 'Некорректная роль');
}

// Дополнительные поля
$fullName = $data['full_name'] ?? null;
$companyName = $data['company_name'] ?? null;
$companyPosition = $data['company_position'] ?? null;

try {
    $pdo = getDbConnection();

    // Проверка существования email
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->execute([$email]);

    if ($stmt->fetch()) {
        sendResponse(false, 'Пользователь с таким email уже существует');
    }

    // Хеширование пароля (SHA-256 для совместимости с Android)
    $passwordHash = hash('sha256', $password);
    $createdAt = round(microtime(true) * 1000); // timestamp в миллисекундах

    // Вставка пользователя
    $stmt = $pdo->prepare("
        INSERT INTO users (email, password_hash, role, full_name, company_name, company_position, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    ");

    $stmt->execute([
        $email,
        $passwordHash,
        $role,
        $fullName,
        $companyName,
        $companyPosition,
        $createdAt
    ]);

    $userId = $pdo->lastInsertId();

    sendResponse(true, 'Регистрация успешна', [
        'user_id' => (int)$userId,
        'email' => $email,
        'role' => $role
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при регистрации: ' . $e->getMessage());
}
