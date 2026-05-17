<?php
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

$input = getJsonInput();

$studentId = isset($input['student_id']) ? (int)$input['student_id'] : 0;

if ($studentId <= 0) {
    sendResponse(false, 'Неверный student_id');
}

$pdo = getDbConnection();

$stmt = $pdo->prepare("
    SELECT id, email, full_name, role, verification_status, created_at,
           company_name, company_position
    FROM users
    WHERE id = :student_id AND role = 'STUDENT'
");
$stmt->execute([':student_id' => $studentId]);
$row = $stmt->fetch();

if (!$row) {
    sendResponse(false, 'Студент не найден');
}

// created_at может быть unix timestamp (сек) или уже в миллисекундах
$createdAt = $row['created_at'];
if (is_numeric($createdAt)) {
    $createdAtMs = (int)$createdAt;
    if ($createdAtMs < 9_999_999_999) {
        $createdAtMs = $createdAtMs * 1000;
    }
} else {
    $createdAtMs = strtotime($createdAt) * 1000;
}

// Количество завершённых проектов
$completedStmt = $pdo->prepare(
    "SELECT COUNT(*) FROM applications WHERE student_id = ? AND status = 'COMPLETED'"
);
$completedStmt->execute([$studentId]);
$completedCount = (int)$completedStmt->fetchColumn();

sendResponse(true, 'OK', [
    'id'                  => (int)$row['id'],
    'email'               => $row['email'],
    'full_name'           => $row['full_name'] ?? '',
    'role'                => $row['role'],
    'company_name'        => $row['company_name'] ?? '',
    'company_position'    => $row['company_position'] ?? '',
    'verification_status' => $row['verification_status'] ?? 'NOT_VERIFIED',
    'created_at'          => $createdAtMs,
    'completed_projects'  => $completedCount,
]);
