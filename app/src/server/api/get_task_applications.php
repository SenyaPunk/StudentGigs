<?php
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

$input = getJsonInput();

$taskId     = isset($input['task_id'])     ? (int)$input['task_id']     : 0;
$employerId = isset($input['employer_id']) ? (int)$input['employer_id'] : 0;

if ($taskId <= 0 || $employerId <= 0) {
    sendResponse(false, 'Неверные параметры');
}

try {
    $pdo = getDbConnection();

    // Проверяем, что задание принадлежит этому работодателю
    $checkStmt = $pdo->prepare(
        "SELECT id FROM tasks WHERE id = :task_id AND employer_id = :employer_id"
    );
    $checkStmt->execute([':task_id' => $taskId, ':employer_id' => $employerId]);

    if ($checkStmt->rowCount() === 0) {
        sendResponse(false, 'Задание не найдено или доступ запрещён');
    }

    // Получаем все отклики с данными студента
    // ИСПРАВЛЕНО: a.created_at вместо a.applied_at (такое поле в таблице applications)
    $stmt = $pdo->prepare("
        SELECT
            a.id            AS application_id,
            a.student_id,
            a.task_id,
            a.status        AS application_status,
            a.created_at    AS applied_at,
            u.id            AS user_id,
            u.email,
            u.full_name,
            u.role,
            u.verification_status,
            u.created_at    AS user_created_at
        FROM applications a
        JOIN users u ON u.id = a.student_id
        WHERE a.task_id = :task_id
        ORDER BY
            CASE a.status WHEN 'IN_PROGRESS' THEN 0 ELSE 1 END,
            a.created_at DESC
    ");
    $stmt->execute([':task_id' => $taskId]);

    $applications = [];
    while ($row = $stmt->fetch()) {
        $appliedAt = $row['applied_at'];
        if (is_numeric($appliedAt)) {
            $appliedAtMs = (int)$appliedAt;
            if ($appliedAtMs < 9_999_999_999) {
                $appliedAtMs = $appliedAtMs * 1000;
            }
        } else {
            $appliedAtMs = strtotime($appliedAt) * 1000;
        }

        $userCreatedAt = $row['user_created_at'];
        if (is_numeric($userCreatedAt)) {
            $userCreatedAtMs = (int)$userCreatedAt;
            if ($userCreatedAtMs < 9_999_999_999) {
                $userCreatedAtMs = $userCreatedAtMs * 1000;
            }
        } else {
            $userCreatedAtMs = strtotime($userCreatedAt) * 1000;
        }

        $applications[] = [
            'application_id'     => (int)$row['application_id'],
            'student_id'         => (int)$row['student_id'],
            'task_id'            => (int)$row['task_id'],
            'application_status' => $row['application_status'] ?? 'PENDING',
            'applied_at'         => $appliedAtMs,
            'student' => [
                'id'                  => (int)$row['user_id'],
                'email'               => $row['email'],
                'full_name'           => $row['full_name'] ?? '',
                'role'                => $row['role'],
                'verification_status' => $row['verification_status'] ?? 'NOT_VERIFIED',
                'created_at'          => $userCreatedAtMs,
            ],
        ];
    }

    sendResponse(true, 'OK', ['applications' => $applications]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка базы данных: ' . $e->getMessage());
} catch (Exception $e) {
    sendResponse(false, 'Внутренняя ошибка сервера');
}
