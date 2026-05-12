<?php
/**
 * Получение списка заданий
 */

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Метод не поддерживается');
}

$data = getJsonInput();

// Фильтры
$type = isset($data['type']) ? strtoupper($data['type']) : null;
$employerId = $data['employer_id'] ?? null;
$status = isset($data['status']) ? strtoupper($data['status']) : 'ACTIVE';
$limit = min((int)($data['limit'] ?? 50), 100);
$offset = (int)($data['offset'] ?? 0);

try {
    $pdo = getDbConnection();

    $whereConditions = [];
    $params = [];

    // Фильтр по статусу
    if ($status && in_array($status, ['ACTIVE', 'CLOSED', 'DRAFT'])) {
        $whereConditions[] = "t.status = ?";
        $params[] = $status;
    }

    // Фильтр по типу
    if ($type && in_array($type, ['SERVICE', 'VACANCY', 'TASK'])) {
        $whereConditions[] = "t.type = ?";
        $params[] = $type;
    }

    // Фильтр по работодателю
    if ($employerId) {
        $whereConditions[] = "t.employer_id = ?";
        $params[] = $employerId;
    }

    $whereClause = empty($whereConditions) ? "" : "WHERE " . implode(" AND ", $whereConditions);

    $sql = "
        SELECT 
            t.*,
            u.company_name as employer_name,
            u.company_position as employer_position
        FROM tasks t
        LEFT JOIN users u ON t.employer_id = u.id
        $whereClause
        ORDER BY t.created_at DESC
        LIMIT ? OFFSET ?
    ";

    $params[] = $limit;
    $params[] = $offset;

    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $tasks = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Получаем общее количество
    $countParams = array_slice($params, 0, -2);
    $countSql = "SELECT COUNT(*) FROM tasks t $whereClause";
    $countStmt = $pdo->prepare($countSql);
    $countStmt->execute($countParams);
    $totalCount = $countStmt->fetchColumn();

    // Форматируем данные
    $formattedTasks = array_map(function($task) {
        return [
            'id' => (int)$task['id'],
            'employer_id' => (int)$task['employer_id'],
            'employer_name' => $task['employer_name'],
            'type' => $task['type'],
            'title' => $task['title'],
            'description' => $task['description'],
            'requirements' => $task['requirements'],
            'benefits' => $task['benefits'],
            'tags' => $task['tags'],
            'price' => $task['price'],
            'price_type' => $task['price_type'],
            'duration' => $task['duration'],
            'location' => $task['location'],
            'location_type' => $task['location_type'],
            'deadline' => $task['deadline'] ? (int)$task['deadline'] : null,
            'status' => $task['status'],
            'created_at' => (int)$task['created_at'],
            'responses_count' => (int)$task['responses_count'],
            'icon_emoji' => $task['icon_emoji'],
            'employment_type' => $task['employment_type'],
            'schedule' => $task['schedule'],
            'service_category' => $task['service_category']
        ];
    }, $tasks);

    sendResponse(true, 'Задания получены', [
        'tasks' => $formattedTasks,
        'total_count' => (int)$totalCount,
        'limit' => $limit,
        'offset' => $offset
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при получении заданий: ' . $e->getMessage());
}
