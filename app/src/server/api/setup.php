<?php
/**
 * Скрипт для создания таблиц базы данных
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
        verification_status ENUM('NOT_VERIFIED', 'PENDING', 'VERIFIED') DEFAULT 'NOT_VERIFIED',
        passport_photo_url VARCHAR(500) DEFAULT NULL,
        verification_requested_at BIGINT DEFAULT NULL,
        INDEX idx_email (email),
        INDEX idx_role (role),
        INDEX idx_verification_status (verification_status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    $pdo->exec($sql);

    // Добавление колонок если таблица уже существует
    try {
        $pdo->exec("ALTER TABLE users ADD COLUMN verification_status ENUM('NOT_VERIFIED', 'PENDING', 'VERIFIED') DEFAULT 'NOT_VERIFIED'");
    } catch (PDOException $e) {}
    
    try {
        $pdo->exec("ALTER TABLE users ADD COLUMN passport_photo_url VARCHAR(500) DEFAULT NULL");
    } catch (PDOException $e) {}
    
    try {
        $pdo->exec("ALTER TABLE users ADD COLUMN verification_requested_at BIGINT DEFAULT NULL");
    } catch (PDOException $e) {}

    // Создание таблицы заданий
    $tasksSql = "CREATE TABLE IF NOT EXISTS tasks (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        employer_id BIGINT NOT NULL,
        type ENUM('SERVICE', 'VACANCY', 'TASK') NOT NULL,
        title VARCHAR(255) NOT NULL,
        description TEXT NOT NULL,
        requirements TEXT DEFAULT NULL,
        benefits TEXT DEFAULT NULL,
        tags VARCHAR(500) DEFAULT NULL,
        price VARCHAR(100) NOT NULL,
        price_type ENUM('FIXED', 'HOURLY', 'NEGOTIABLE', 'PER_PROJECT') DEFAULT 'FIXED',
        duration VARCHAR(100) DEFAULT NULL,
        location VARCHAR(255) DEFAULT NULL,
        location_type ENUM('REMOTE', 'OFFICE', 'HYBRID') DEFAULT 'REMOTE',
        deadline BIGINT DEFAULT NULL,
        status ENUM('ACTIVE', 'CLOSED', 'DRAFT') DEFAULT 'ACTIVE',
        created_at BIGINT NOT NULL,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        responses_count INT DEFAULT 0,
        icon_emoji VARCHAR(10) DEFAULT '📋',
        employment_type ENUM('FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'PROJECT') DEFAULT NULL,
        schedule VARCHAR(255) DEFAULT NULL,
        service_category VARCHAR(100) DEFAULT NULL,
        INDEX idx_employer (employer_id),
        INDEX idx_type (type),
        INDEX idx_status (status),
        INDEX idx_created_at (created_at),
        FOREIGN KEY (employer_id) REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    $pdo->exec($tasksSql);

    sendResponse(true, 'Таблицы users и tasks успешно созданы');

} catch (PDOException $e) {
    sendResponse(false, 'Ошибка при создании таблиц: ' . $e->getMessage());
}
