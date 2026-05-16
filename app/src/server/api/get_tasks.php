<?php
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Метод не поддерживается');
}

$data = getJsonInput();

$type       = isset($data['type'])        ? strtoupper($data['type']) : null;
$employerId = $data['employer_id']        ?? null;
$status     = isset($data['status'])      ? strtoupper($data['status']) : 'ACTIVE';
$limit      = min((int)($data['limit']   ?? 50), 100);
$offset     = (int)($data['offset']      ?? 0);

try {
    $pdo = getDbConnection();

    $whereConditions = [];
    $params = [];

    if ($status && in_array($status, ['ACTIVE', 'CLOSED', 'DRAFT'])) {
        $whereConditions[] = "t.status = ?";
        $params[] = $status;
    }

    if ($type && in_array($type, ['SERVICE', 'VACANCY', 'TASK'])) {
        $whereConditions[] = "t.type = ?";
        $params[] = $type;
    }

    if ($employerId) {
        $whereConditions[] = "t.employer_id = ?";
        $params[] = (int)$employerId;
    }

    $whereClause = empty($whereConditions) ? "" : "WHERE " . implode(" AND ", $whereConditions);

    $sql = "
        SELECT
            t.*,
            u.company_name     AS employer_name,
            u.company_position AS employer_position
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

    $countParams = array_slice($params, 0, -2);
    $countSql    = "SELECT COUNT(*) FROM tasks t $whereClause";
    $countStmt   = $pdo->prepare($countSql);
    $countStmt->execute($countParams);
    $totalCount  = (int)$countStmt->fetchColumn();

    $formattedTasks = array_map(function ($task) {
        $createdAt = $task['created_at'];
        if (is_numeric($createdAt)) {
            $createdAtMs = (int)$createdAt < 9_999_999_999 ? (int)$createdAt * 1000 : (int)$createdAt;
        } else {
            $createdAtMs = strtotime($createdAt) * 1000;
        }

        $deadline = null;
        if (!empty($task['deadline'])) {
            $dl = $task['deadline'];
            if (is_numeric($dl)) {
                $deadline = (int)$dl < 9_999_999_999 ? (int)$dl * 1000 : (int)$dl;
            } else {
                $deadline = strtotime($dl) * 1000;
            }
        }

        return [
            'id'               => (int)$task['id'],
            'employer_id'      => (int)$task['employer_id'],
            'employer_name'    => $task['employer_name']    ?? '',
            'employer_position'=> $task['employer_position'] ?? null,
            'type'             => $task['type'],
            'title'            => $task['title'],
            'description'      => $task['description'],
            'requirements'     => $task['requirements']     ?? '',
            'benefits'         => $task['benefits']         ?? '',
            'tags'             => $task['tags']             ?? '',
            'price'            => $task['price'],
            'price_type'       => $task['price_type']       ?? 'FIXED',
            'duration'         => $task['duration']         ?? '',
            'location'         => $task['location']         ?? '',
            'location_type'    => $task['location_type']    ?? 'REMOTE',
            'deadline'         => $deadline,
            'status'           => $task['status'],
            'created_at'       => $createdAtMs,
            'responses_count'  => (int)($task['responses_count'] ?? 0),
            'icon_emoji'       => $task['icon_emoji']       ?? '📋',
            'employment_type'  => $task['employment_type']  ?? null,
            'schedule'         => $task['schedule']         ?? null,
            'service_category' => $task['service_category'] ?? null,
        ];
    }, $tasks);

    sendResponse(true, 'Задания получены', [
        'tasks'       => $formattedTasks,
        'total_count' => $totalCount,
        'limit'       => $limit,
        'offset'      => $offset,
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при получении заданий: ' . $e->getMessage());
} catch (Exception $e) {
    sendResponse(false, 'Внутренняя ошибка сервера');
}
