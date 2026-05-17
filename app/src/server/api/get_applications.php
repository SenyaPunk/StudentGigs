<?php
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Метод не поддерживается');
}

$data = getJsonInput();
$studentId = $data['student_id'] ?? null;

if (!$studentId) {
    sendResponse(false, 'Не указан student_id');
}

try {
    $pdo = getDbConnection();

    $stmt = $pdo->prepare("
        SELECT
            a.id            AS application_id,
            a.student_id,
            a.task_id,
            a.status        AS application_status,
            a.created_at    AS applied_at,

            t.id            AS t_id,
            t.employer_id,
            t.type,
            t.title,
            t.description,
            t.requirements,
            t.benefits,
            t.tags,
            t.price,
            t.price_type,
            t.duration,
            t.location,
            t.location_type,
            t.deadline,
            t.status        AS task_status,
            t.created_at    AS task_created_at,
            t.responses_count,
            t.icon_emoji,
            t.employment_type,
            t.schedule,
            t.service_category,

            u.company_name     AS employer_name,
            u.company_position AS employer_position
        FROM applications a
        LEFT JOIN tasks t ON a.task_id = t.id
        LEFT JOIN users u ON t.employer_id = u.id
        WHERE a.student_id = ?
        ORDER BY a.created_at DESC
    ");

    $stmt->execute([$studentId]);
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $applications = array_map(function($row) {
        // applied_at хранится в мс (insert делает microtime(true)*1000)
        $appliedAt = (int)$row['applied_at'];

        // task_created_at — защитная конвертация (sec → ms для старых записей)
        $taskCreatedAt = 0;
        if (!empty($row['task_created_at'])) {
            $raw = $row['task_created_at'];
            if (is_numeric($raw)) {
                $taskCreatedAt = (int)$raw < 9_999_999_999 ? (int)$raw * 1000 : (int)$raw;
            } else {
                $taskCreatedAt = strtotime($raw) * 1000;
            }
        }

        // deadline — аналогично
        $deadline = null;
        if (!empty($row['deadline'])) {
            $dl = $row['deadline'];
            if (is_numeric($dl)) {
                $deadline = (int)$dl < 9_999_999_999 ? (int)$dl * 1000 : (int)$dl;
            } else {
                $deadline = strtotime($dl) * 1000;
            }
        }

        // Если задание было удалено — задача null
        $taskData = null;
        if (!empty($row['t_id'])) {
            $taskData = [
                'id'               => (int)$row['t_id'],
                'employer_id'      => (int)$row['employer_id'],
                'employer_name'    => $row['employer_name']     ?? '',
                'employer_position'=> $row['employer_position'] ?? '',
                'type'             => $row['type']              ?? 'TASK',
                'title'            => $row['title']             ?? '',
                'description'      => $row['description']       ?? '',
                'requirements'     => $row['requirements']      ?? '',
                'benefits'         => $row['benefits']          ?? '',
                'tags'             => $row['tags']              ?? '',
                'price'            => $row['price']             ?? '',
                'price_type'       => $row['price_type']        ?? 'FIXED',
                'duration'         => $row['duration']          ?? '',
                'location'         => $row['location']          ?? '',
                'location_type'    => $row['location_type']     ?? 'REMOTE',
                'deadline'         => $deadline,
                'status'           => $row['task_status']       ?? 'ACTIVE',
                'created_at'       => $taskCreatedAt,
                'responses_count'  => (int)($row['responses_count'] ?? 0),
                'icon_emoji'       => $row['icon_emoji']         ?? '📋',
                'employment_type'  => $row['employment_type']   ?? null,
                'schedule'         => $row['schedule']           ?? null,
                'service_category' => $row['service_category']  ?? null,
            ];
        }

        return [
            'application_id'     => (int)$row['application_id'],
            'student_id'         => (int)$row['student_id'],
            'task_id'            => (int)$row['task_id'],
            'application_status' => $row['application_status'] ?? 'PENDING',
            'applied_at'         => $appliedAt,
            'task'               => $taskData,
        ];
    }, $rows);

    $appliedTaskIds = array_values(array_unique(array_filter(
        array_column($applications, 'task_id')
    )));

    sendResponse(true, 'Отклики получены', [
        'applications'     => $applications,
        'applied_task_ids' => $appliedTaskIds,
        'total'            => count($applications),
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка сервера: ' . $e->getMessage());
}
