<?php
  /**
   * migrate.php — безопасное добавление всех недостающих колонок.
   * НЕ удаляет данные. Можно запускать многократно.
   * Открыть: http://46.173.28.109/api/migrate.php
   */
  header('Content-Type: application/json; charset=utf-8');
  header('Access-Control-Allow-Origin: *');

  $log  = [];
  $errs = [];

  try {
      $pdo = new PDO(
          'mysql:host=localhost;dbname=student_gig_db;charset=utf8mb4',
          'gig_user', 'Sena090909.',
          [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
      );
      $log[] = 'БД: подключено';
  } catch (\Throwable $e) {
      echo json_encode(['success'=>false,'message'=>'Ошибка подключения: '.$e->getMessage()], JSON_UNESCAPED_UNICODE);
      exit;
  }

  function addColumnIfMissing(PDO $pdo, string $table, string $column, string $definition, array &$log, array &$errs): void {
      try {
          $check = $pdo->prepare("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
              WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = :t AND COLUMN_NAME = :c");
          $check->execute([':t' => $table, ':c' => $column]);
          if ((int)$check->fetchColumn() === 0) {
              $pdo->exec("ALTER TABLE `{$table}` ADD COLUMN `{$column}` {$definition}");
              $log[] = "ДОБАВЛЕНА: {$table}.{$column}";
          } else {
              $log[] = "OK (уже есть): {$table}.{$column}";
          }
      } catch (\Throwable $e) {
          $errs[] = "ОШИБКА {$table}.{$column}: " . $e->getMessage();
      }
  }

  function addTableIfMissing(PDO $pdo, string $table, string $createSQL, array &$log, array &$errs): void {
      try {
          $check = $pdo->query("SHOW TABLES LIKE '{$table}'")->fetchAll();
          if (empty($check)) {
              $pdo->exec($createSQL);
              $log[] = "СОЗДАНА ТАБЛИЦА: {$table}";
          } else {
              $log[] = "OK (уже есть): таблица {$table}";
          }
      } catch (\Throwable $e) {
          $errs[] = "ОШИБКА создания {$table}: " . $e->getMessage();
      }
  }

  // ── Таблица users ──────────────────────────────────────────────────────────
  addColumnIfMissing($pdo, 'users', 'passport_photo_url',        'VARCHAR(500) NULL', $log, $errs);
  addColumnIfMissing($pdo, 'users', 'verification_requested_at', 'BIGINT NULL', $log, $errs);
  addColumnIfMissing($pdo, 'users', 'company_name',              'VARCHAR(255) NULL', $log, $errs);
  addColumnIfMissing($pdo, 'users', 'company_position',          'VARCHAR(255) NULL', $log, $errs);
  addColumnIfMissing($pdo, 'users', 'full_name',                 'VARCHAR(255) NULL', $log, $errs);

  // ── Таблица tasks ──────────────────────────────────────────────────────────
  addColumnIfMissing($pdo, 'tasks', 'icon_emoji',       'VARCHAR(10) DEFAULT \'📋\'', $log, $errs);
  addColumnIfMissing($pdo, 'tasks', 'employment_type',  'VARCHAR(50) NULL', $log, $errs);
  addColumnIfMissing($pdo, 'tasks', 'schedule',         'VARCHAR(255) NULL', $log, $errs);
  addColumnIfMissing($pdo, 'tasks', 'service_category', 'VARCHAR(255) NULL', $log, $errs);
  addColumnIfMissing($pdo, 'tasks', 'benefits',         'TEXT NULL', $log, $errs);
  addColumnIfMissing($pdo, 'tasks', 'tags',             'TEXT NULL', $log, $errs);

  // ── Таблица applications ───────────────────────────────────────────────────
  addColumnIfMissing($pdo, 'applications', 'student_confirmed',  'TINYINT(1) NOT NULL DEFAULT 0', $log, $errs);
  addColumnIfMissing($pdo, 'applications', 'employer_confirmed', 'TINYINT(1) NOT NULL DEFAULT 0', $log, $errs);

  // ── Таблица task_files ─────────────────────────────────────────────────────
  addColumnIfMissing($pdo, 'task_files', 'uploader_name',  "VARCHAR(255) NOT NULL DEFAULT ''", $log, $errs);
  addColumnIfMissing($pdo, 'task_files', 'original_name',  "VARCHAR(500) NOT NULL DEFAULT ''", $log, $errs);

  // ── Создаём таблицы если их нет ───────────────────────────────────────────
  addTableIfMissing($pdo, 'messages',
      "CREATE TABLE `messages` (
          `id`             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
          `application_id` BIGINT UNSIGNED NOT NULL,
          `sender_id`      BIGINT UNSIGNED NOT NULL,
          `sender_name`    VARCHAR(255) NOT NULL DEFAULT '',
          `message`        TEXT NOT NULL,
          `created_at`     BIGINT NOT NULL,
          KEY `idx_app_time` (`application_id`, `created_at`),
          FOREIGN KEY (`application_id`) REFERENCES `applications`(`id`) ON DELETE CASCADE
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
      $log, $errs
  );

  addTableIfMissing($pdo, 'task_files',
      "CREATE TABLE `task_files` (
          `id`             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
          `application_id` BIGINT UNSIGNED NOT NULL,
          `uploader_id`    BIGINT UNSIGNED NOT NULL,
          `uploader_name`  VARCHAR(255) NOT NULL DEFAULT '',
          `file_name`      VARCHAR(500) NOT NULL,
          `original_name`  VARCHAR(500) NOT NULL DEFAULT '',
          `file_size`      BIGINT NOT NULL DEFAULT 0,
          `mime_type`      VARCHAR(255) NOT NULL DEFAULT 'application/octet-stream',
          `created_at`     BIGINT NOT NULL,
          KEY `idx_app` (`application_id`),
          FOREIGN KEY (`application_id`) REFERENCES `applications`(`id`) ON DELETE CASCADE
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
      $log, $errs
  );

  // ── Папка uploads ──────────────────────────────────────────────────────────
  $uploadsDir = __DIR__ . '/uploads';
  if (!is_dir($uploadsDir)) {
      mkdir($uploadsDir, 0777, true);
      $log[] = 'uploads/: создана';
  } else {
      chmod($uploadsDir, 0777);
      $log[] = 'uploads/: уже существует, права обновлены';
  }

  $ok = empty($errs);
  echo json_encode([
      'success' => $ok,
      'message' => $ok ? 'Миграция выполнена успешно!' : 'Завершено с ошибками',
      'data'    => ['log' => $log, 'errors' => $errs]
  ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
  