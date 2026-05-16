<?php
/**
 * Создание нового задания/вакансии/услуги
 */

error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Метод не поддерживается');
}

$data = getJsonInput();

// Обязательные поля
$employerId = $data['employer_id'] ?? 0;
$type = strtoupper($data['type'] ?? '');
$title = trim($data['title'] ?? '');
$description = trim($data['description'] ?? '');
$price = trim($data['price'] ?? '');

// Валидация обязательных полей
if (empty($employerId)) {
    sendResponse(false, 'ID работодателя обязателен');
}

if (!in_array($type, ['SERVICE', 'VACANCY', 'TASK'])) {
    sendResponse(false, 'Некорректный тип задания');
}

if (empty($title)) {
    sendResponse(false, 'Название обязательно');
}

if (strlen($title) < 5) {
    sendResponse(false, 'Название должно содержать минимум 5 символов');
}

if (empty($description)) {
    sendResponse(false, 'Описание обязательно');
}

if (strlen($description) < 20) {
    sendResponse(false, 'Описание должно содержать минимум 20 символов');
}

if (empty($price)) {
    sendResponse(false, 'Укажите оплату');
}

// Опциональные поля
$requirements = $data['requirements'] ?? '';  // Строка через |
$benefits = $data['benefits'] ?? '';          // Строка через |
$tags = $data['tags'] ?? '';                  // Строка через |
$priceType = strtoupper($data['price_type'] ?? 'FIXED');
$duration = trim($data['duration'] ?? '');
$location = trim($data['location'] ?? '');
$locationType = strtoupper($data['location_type'] ?? 'REMOTE');
$deadline = $data['deadline'] ?? null;
$iconEmoji = $data['icon_emoji'] ?? '📋';
$employmentType = isset($data['employment_type']) ? strtoupper($data['employment_type']) : null;
$schedule = $data['schedule'] ?? null;
$serviceCategory = $data['service_category'] ?? null;

// Валидация типов
if (!in_array($priceType, ['FIXED', 'HOURLY', 'NEGOTIABLE', 'PER_PROJECT'])) {
    $priceType = 'FIXED';
}

if (!in_array($locationType, ['REMOTE', 'OFFICE', 'HYBRID'])) {
    $locationType = 'REMOTE';
}

if ($employmentType && !in_array($employmentType, ['FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'PROJECT'])) {
    $employmentType = null;
}

try {
    $pdo = getDbConnection();

    // Проверяем, что пользователь - верифицированный работодатель
    $stmt = $pdo->prepare("
        SELECT id, role, verification_status 
        FROM users WHERE id = ?
    ");
    $stmt->execute([$employerId]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        sendResponse(false, 'Пользователь не найден');
    }

    if ($user['role'] !== 'EMPLOYER') {
        sendResponse(false, 'Только работодатели могут создавать задания');
    }

    if ($user['verification_status'] !== 'VERIFIED') {
        sendResponse(false, 'Для создания заданий необходимо верифицировать профиль');
    }

    // Создаем задание
    $createdAt = round(microtime(true) * 1000);

    $stmt = $pdo->prepare("
        INSERT INTO tasks (
            employer_id, type, title, description, requirements, benefits, tags,
            price, price_type, duration, location, location_type, deadline,
            status, created_at, icon_emoji, employment_type, schedule, service_category
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?, ?, ?, ?)
    ");

    $stmt->execute([
        $employerId,
        $type,
        $title,
        $description,
        $requirements,
        $benefits,
        $tags,
        $price,
        $priceType,
        $duration,
        $location,
        $locationType,
        $deadline,
        $createdAt,
        $iconEmoji,
        $employmentType,
        $schedule,
        $serviceCategory
    ]);

    $taskId = $pdo->lastInsertId();

    sendResponse(true, 'Задание успешно создано', [
        'task_id' => (int)$taskId,
        'type' => $type,
        'title' => $title,
        'status' => 'ACTIVE',
        'created_at' => $createdAt
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при создании задания: ' . $e->getMessage());
}
