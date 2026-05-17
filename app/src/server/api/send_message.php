<?php
  /**
   * send_message.php — отправка сообщения в чат рабочей зоны.
   * Без mb_* функций (mbstring не установлен на сервере).
   */

  ob_start();

  register_shutdown_function(function () {
      $err = error_get_last();
      $out = ob_get_clean();
      if ($err && in_array($err['type'], [E_ERROR, E_PARSE, E_CORE_ERROR, E_COMPILE_ERROR])) {
          if (!headers_sent()) {
              header('Content-Type: application/json; charset=utf-8');
              header('Access-Control-Allow-Origin: *');
          }
          http_response_code(500);
          $msg = str_replace(["
","
"], '?', $err['message']);
          echo '{"success":false,"message":"PHP Fatal: ' . addslashes($msg) . '","line":' . (int)$err['line'] . '}';
      } else {
          echo $out;
      }
  });

  ini_set('display_errors', '0');
  ini_set('log_errors', '1');
  error_reporting(E_ALL);

  header('Content-Type: application/json; charset=utf-8');
  header('Access-Control-Allow-Origin: *');
  header('Access-Control-Allow-Methods: POST, OPTIONS');
  header('Access-Control-Allow-Headers: Content-Type, Authorization');

  if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
      ob_end_flush();
      exit;
  }

  function safeJson($data) {
      $encoded = json_encode($data, JSON_UNESCAPED_UNICODE);
      if ($encoded === false) {
          $encoded = json_encode($data, 0);
      }
      if ($encoded === false) {
          return '{"success":false,"message":"json_encode failed"}';
      }
      return $encoded;
  }

  // Чтение тела запроса
  $rawInput = (string)file_get_contents('php://input');
  $input    = json_decode($rawInput, true);
  if (!is_array($input)) $input = [];

  $applicationId = isset($input['application_id']) ? (int)$input['application_id'] : 0;
  $senderId      = isset($input['sender_id'])      ? (int)$input['sender_id']      : 0;
  $senderName    = isset($input['sender_name'])    ? trim((string)$input['sender_name']) : '';
  $message       = isset($input['message'])        ? trim((string)$input['message'])     : '';

  // Валидация (strlen вместо mb_strlen)
  if ($applicationId <= 0 || $senderId <= 0 || $message === '') {
      echo safeJson([
          'success' => false,
          'message' => 'Неверные параметры',
          'debug'   => [
              'application_id'  => $applicationId,
              'sender_id'       => $senderId,
              'message_length'  => strlen($message),
              'raw_body_length' => strlen($rawInput),
          ],
      ]);
      ob_end_flush();
      exit;
  }

  if (strlen($message) > 16000) {
      echo safeJson(['success' => false, 'message' => 'Сообщение слишком длинное']);
      ob_end_flush();
      exit;
  }

  try {
      $pdo = new PDO(
          'mysql:host=localhost;dbname=student_gig_db;charset=utf8mb4',
          'gig_user',
          'Sena090909.',
          [
              PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
              PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
              PDO::ATTR_EMULATE_PREPARES   => false,
          ]
      );

      // Проверка доступа
      $checkStmt = $pdo->prepare(
          "SELECT a.id FROM applications a
           JOIN tasks t ON t.id = a.task_id
           WHERE a.id = :app_id
             AND (a.student_id = :uid OR t.employer_id = :uid2)"
      );
      $checkStmt->execute([':app_id' => $applicationId, ':uid' => $senderId, ':uid2' => $senderId]);

      if (!$checkStmt->fetch()) {
          echo safeJson([
              'success' => false,
              'message' => 'Доступ запрещён',
              'debug'   => ['user_id' => $senderId, 'application_id' => $applicationId],
          ]);
          ob_end_flush();
          exit;
      }

      // Создаём таблицу messages если не существует
      $pdo->exec(
          "CREATE TABLE IF NOT EXISTS `messages` (
              `id`             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
              `application_id` BIGINT UNSIGNED NOT NULL,
              `sender_id`      BIGINT UNSIGNED NOT NULL,
              `sender_name`    VARCHAR(255) NOT NULL DEFAULT '',
              `message`        TEXT NOT NULL,
              `created_at`     BIGINT NOT NULL,
              KEY `idx_app_time` (`application_id`, `created_at`)
          ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
      );

      $createdAt  = (int)(microtime(true) * 1000);
      $insertStmt = $pdo->prepare(
          "INSERT INTO `messages` (application_id, sender_id, sender_name, message, created_at)
           VALUES (:app_id, :sid, :sname, :msg, :cat)"
      );
      $insertStmt->execute([
          ':app_id' => $applicationId,
          ':sid'    => $senderId,
          ':sname'  => $senderName,
          ':msg'    => $message,
          ':cat'    => $createdAt,
      ]);

      $newId = (int)$pdo->lastInsertId();

      echo safeJson([
          'success' => true,
          'message' => 'Отправлено',
          'data'    => [
              'message' => [
                  'id'             => $newId,
                  'application_id' => $applicationId,
                  'sender_id'      => $senderId,
                  'sender_name'    => $senderName,
                  'message'        => $message,
                  'created_at'     => $createdAt,
              ],
          ],
      ]);

  } catch (PDOException $e) {
      http_response_code(500);
      $errMsg = str_replace(["
", "
"], ' ', $e->getMessage());
      echo safeJson(['success' => false, 'message' => 'Ошибка БД: ' . $errMsg]);
  } catch (Throwable $e) {
      http_response_code(500);
      $errMsg = str_replace(["
", "
"], ' ', $e->getMessage());
      echo safeJson(['success' => false, 'message' => 'Ошибка: ' . $errMsg]);
  }

  ob_end_flush();
  exit;
  