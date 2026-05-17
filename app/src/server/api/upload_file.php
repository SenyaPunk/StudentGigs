<?php
ini_set('display_errors', '0');
ini_set('log_errors', '1');
error_reporting(0);
ob_start();
require_once 'config.php';
ob_end_clean();

header('Content-Type: application/json; charset=utf-8');

$applicationId = isset($_POST['application_id']) ? (int)$_POST['application_id'] : 0;
$uploaderId    = isset($_POST['uploader_id'])    ? (int)$_POST['uploader_id']    : 0;
$uploaderName  = isset($_POST['uploader_name'])  ? trim($_POST['uploader_name']) : '';

if ($applicationId <= 0 || $uploaderId <= 0) {
    sendResponse(false, 'Неверные параметры: application_id=' . $applicationId . ' uploader_id=' . $uploaderId);
}

// Диагностика $_FILES
if (!isset($_FILES['file'])) {
    sendResponse(false, 'Файл не получен сервером. Проверьте upload_max_filesize и post_max_size в PHP конфиге. $_FILES пуст.');
}

$fileError = $_FILES['file']['error'];
if ($fileError !== UPLOAD_ERR_OK) {
    $errMessages = [
        UPLOAD_ERR_INI_SIZE   => 'Файл превышает upload_max_filesize в php.ini (' . ini_get('upload_max_filesize') . ')',
        UPLOAD_ERR_FORM_SIZE  => 'Файл превышает MAX_FILE_SIZE в форме',
        UPLOAD_ERR_PARTIAL    => 'Файл загружен частично',
        UPLOAD_ERR_NO_FILE    => 'Файл не выбран',
        UPLOAD_ERR_NO_TMP_DIR => 'Отсутствует временная папка',
        UPLOAD_ERR_CANT_WRITE => 'Не удалось записать файл на диск',
        UPLOAD_ERR_EXTENSION  => 'PHP расширение остановило загрузку',
    ];
    $errMsg = $errMessages[$fileError] ?? 'Неизвестная ошибка загрузки (код: ' . $fileError . ')';
    sendResponse(false, $errMsg);
}

if ($_FILES['file']['size'] > 20 * 1024 * 1024) {
    sendResponse(false, 'Файл слишком большой (максимум 20 МБ)');
}
if ($_FILES['file']['size'] === 0) {
    sendResponse(false, 'Файл пустой');
}

try {
    $pdo = getDbConnection();

    // Проверяем таблицу task_files
    $tableCheck = $pdo->query("SHOW TABLES LIKE 'task_files'")->fetchAll();
    if (empty($tableCheck)) {
        sendResponse(false, 'Таблица task_files не найдена. Откройте /api/setup_workspace.php в браузере для создания таблиц.');
    }

    $checkStmt = $pdo->prepare("
        SELECT a.id FROM applications a
        JOIN tasks t ON t.id = a.task_id
        WHERE a.id = :app_id
          AND (a.student_id = :uid OR t.employer_id = :uid2)
    ");
    $checkStmt->execute([':app_id' => $applicationId, ':uid' => $uploaderId, ':uid2' => $uploaderId]);
    if (!$checkStmt->fetch()) {
        sendResponse(false, 'Доступ запрещён');
    }

    $uploadsDir = __DIR__ . '/uploads';

    // Создаём папку с правами 0777 если не существует
    if (!is_dir($uploadsDir)) {
        if (!@mkdir($uploadsDir, 0777, true)) {
            sendResponse(false, 'Не удалось создать папку uploads. Создайте вручную: mkdir api/uploads && chmod 777 api/uploads');
        }
        @chmod($uploadsDir, 0777);
    } else {
        // Папка существует — убеждаемся что она доступна для записи
        if (!is_writable($uploadsDir)) {
            @chmod($uploadsDir, 0777);
            if (!is_writable($uploadsDir)) {
                sendResponse(false, 'Папка uploads недоступна для записи. Выполните на сервере: chmod 777 ' . $uploadsDir);
            }
        }
    }

    $originalName = basename($_FILES['file']['name']);
    // Безопасное расширение — только буквы и цифры
    $ext      = pathinfo($originalName, PATHINFO_EXTENSION);
    $safeExt  = preg_replace('/[^a-zA-Z0-9]/', '', $ext);
    $uniqueName = $applicationId . '_' . time() . '_' . bin2hex(random_bytes(4))
                  . ($safeExt ? '.' . $safeExt : '');
    $destPath = $uploadsDir . '/' . $uniqueName;

    if (!move_uploaded_file($_FILES['file']['tmp_name'], $destPath)) {
        $writable   = is_writable($uploadsDir) ? 'writable' : 'NOT writable';
        $perms      = substr(sprintf('%o', fileperms($uploadsDir)), -4);
        $tmpExists  = file_exists($_FILES['file']['tmp_name']) ? 'exists' : 'missing';
        sendResponse(false,
            'Не удалось сохранить файл. Папка uploads: ' . $writable
            . ', права: ' . $perms
            . ', tmp файл: ' . $tmpExists
            . ', путь: ' . $destPath
        );
    }

    // Устанавливаем права на загруженный файл
    @chmod($destPath, 0644);

    $mimeType  = !empty($_FILES['file']['type']) ? $_FILES['file']['type'] : 'application/octet-stream';
    $fileSize  = (int)$_FILES['file']['size'];
    $createdAt = (int)(microtime(true) * 1000);

    $insertStmt = $pdo->prepare("
        INSERT INTO task_files
            (application_id, uploader_id, uploader_name, file_name, original_name, file_size, mime_type, created_at)
        VALUES (:app_id, :uid, :uname, :fname, :oname, :fsize, :mime, :cat)
    ");
    $insertStmt->execute([
        ':app_id' => $applicationId, ':uid'   => $uploaderId,
        ':uname'  => $uploaderName,  ':fname' => $uniqueName,
        ':oname'  => $originalName,  ':fsize' => $fileSize,
        ':mime'   => $mimeType,      ':cat'   => $createdAt,
    ]);

    $proto  = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
    $host   = $_SERVER['HTTP_HOST'] ?? 'localhost';
    $apiDir = rtrim(dirname($_SERVER['SCRIPT_NAME']), '/');
    $dlUrl  = $proto . '://' . $host . $apiDir . '/uploads/' . $uniqueName;

    sendResponse(true, 'Файл загружен', [
        'file' => [
            'id'             => (int)$pdo->lastInsertId(),
            'application_id' => $applicationId,
            'uploader_id'    => $uploaderId,
            'uploader_name'  => $uploaderName,
            'file_name'      => $uniqueName,
            'original_name'  => $originalName,
            'file_size'      => $fileSize,
            'mime_type'      => $mimeType,
            'created_at'     => $createdAt,
            'download_url'   => $dlUrl,
        ]
    ]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка базы данных: ' . $e->getMessage());
} catch (Exception $e) {
    sendResponse(false, 'Ошибка: ' . $e->getMessage());
}
