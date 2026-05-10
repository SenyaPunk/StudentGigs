<?php
/**
 * Скрипт для создания таблицы пользователей
 * Запустите этот скрипт один раз для инициализации БД
 */

require_once 'config.php';

try {
    $pdo = getDbConnection();

    // Создание таблицы пользователей
    $sql = "CREATE TABLE IF NOT EXISTS users (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        email VARCHAR(255) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        role ENUM('STUDENT', 'EMPLOYER') NOT NULL DEFAULT 'STUDENT',
        full_name VARCHAR(255) DEFAULT NULL,
        company_name VARCHAR(255) DEFAULT NULL,
        company_position VARCHAR(255) DEFAULT NULL,
        created_at BIGINT NOT NULL,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        INDEX idx_email (email),
        INDEX idx_role (role)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    $pdo->exec($sql);

    sendResponse(true, 'Таблица users успешно создана');

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при создании таблицы: ' . $e->getMessage());
}
