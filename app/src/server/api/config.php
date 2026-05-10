<?php
/**
 * Конфигурация базы данных для beget.ru
 * Замените значения на реальные данные вашего хостинга
 */

// Настройки подключения к MySQL на beget.ru
define('DB_HOST', 'localhost');           // Обычно localhost на beget
define('DB_NAME', 'student_gig_db');  // Имя вашей базы данных
define('DB_USER', 'gig_user');  // Логин базы данных
define('DB_PASS', 'Sena090909.');  // Пароль базы данных
define('DB_CHARSET', 'utf8mb4');

// Подключение к базе данных
function getDbConnection() {
    try {
        $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
        $pdo = new PDO($dsn, DB_USER, DB_PASS, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false
        ]);
        return $pdo;
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'message' => 'Ошибка подключения к базе данных'
        ]);
        exit;
    }
}

// CORS заголовки
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Обработка preflight запросов
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

// Функция для отправки JSON ответа
function sendResponse($success, $message, $data = null) {
    echo json_encode([
        'success' => $success,
        'message' => $message,
        'data' => $data
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Функция для получения JSON данных из запроса
function getJsonInput() {
    $input = file_get_contents('php://input');
    return json_decode($input, true) ?: [];
}
