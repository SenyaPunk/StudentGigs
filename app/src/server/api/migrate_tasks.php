<?php
/**
 * Миграция: добавляет недостающие колонки в таблицу tasks
 * Запустите ОДИН РАЗ: http://46.173.28.109/api/migrate_tasks.php
 */

require_once 'config.php';

try {
    $pdo = getDbConnection();

    $migrations = [
        "ALTER TABLE tasks ADD COLUMN responses_count INT DEFAULT 0" => "responses_count",
        "ALTER TABLE tasks ADD COLUMN icon_emoji VARCHAR(10) DEFAULT '📋'" => "icon_emoji",
        "ALTER TABLE tasks ADD COLUMN employment_type ENUM('FULL_TIME','PART_TIME','INTERNSHIP','PROJECT') DEFAULT NULL" => "employment_type",
        "ALTER TABLE tasks ADD COLUMN schedule VARCHAR(255) DEFAULT NULL" => "schedule",
        "ALTER TABLE tasks ADD COLUMN service_category VARCHAR(100) DEFAULT NULL" => "service_category",
    ];

    $results = [];

    foreach ($migrations as $sql => $columnName) {
        // Проверяем, существует ли уже колонка
        $check = $pdo->prepare("
            SELECT COUNT(*) 
            FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
              AND TABLE_NAME = 'tasks' 
              AND COLUMN_NAME = ?
        ");
        $check->execute([$columnName]);
        $exists = (int)$check->fetchColumn();

        if ($exists === 0) {
            $pdo->exec($sql);
            $results[] = "✓ Добавлена колонка: $columnName";
        } else {
            $results[] = "— Уже есть: $columnName";
        }
    }

    sendResponse(true, 'Миграция завершена', ['steps' => $results]);

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка миграции: ' . $e->getMessage());
}