<?php
/**
 * Создание таблицы applications
 * Запустите ОДИН РАЗ через браузер или curl:
 *   curl -X POST http://46.173.28.109/api/setup_applications.php
 */

require_once 'config.php';

try {
    $pdo = getDbConnection();

    $sql = "CREATE TABLE IF NOT EXISTS applications (
        id          BIGINT PRIMARY KEY AUTO_INCREMENT,
        student_id  BIGINT NOT NULL,
        task_id     BIGINT NOT NULL,
        status      ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
        created_at  BIGINT NOT NULL,
        updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

        UNIQUE KEY unique_application (student_id, task_id),
        INDEX idx_student   (student_id),
        INDEX idx_task      (task_id),
        INDEX idx_status    (status),
        FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
        FOREIGN KEY (task_id)    REFERENCES tasks(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    $pdo->exec($sql);

    sendResponse(true, 'Таблица applications успешно создана');

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка: ' . $e->getMessage());
}
