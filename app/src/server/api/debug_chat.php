<?php
  /**
   * debug_chat.php — диагностика чата.
   * Открыть: http://46.173.28.109/api/debug_chat.php?app_id=1&user_id=1
   * УДАЛИТЬ с сервера после диагностики!
   */
  header('Content-Type: application/json; charset=utf-8');
  header('Access-Control-Allow-Origin: *');

  $appId  = isset($_GET['app_id'])  ? (int)$_GET['app_id']  : 0;
  $userId = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 0;

  $log = [];

  // 1. PHP version
  $log[] = 'PHP: ' . PHP_VERSION;

  // 2. DB connection
  try {
      $pdo = new PDO('mysql:host=localhost;dbname=student_gig_db;charset=utf8mb4',
          'gig_user', 'Sena090909.',
          [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC]
      );
      $log[] = 'БД: подключено';
  } catch (\Throwable $e) {
      echo json_encode(['success'=>false,'log'=>$log,'error'=>'БД: '.$e->getMessage()], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
      exit;
  }

  // 3. Check tables exist
  foreach (['users','tasks','applications','messages','task_files'] as $tbl) {
      $exists = !empty($pdo->query("SHOW TABLES LIKE '{$tbl}'")->fetchAll());
      $log[] = "Таблица {$tbl}: " . ($exists ? 'OK' : 'ОТСУТСТВУЕТ!');
  }

  // 4. Check columns in messages
  try {
      $cols = $pdo->query("SHOW COLUMNS FROM messages")->fetchAll();
      $log[] = 'messages columns: ' . implode(', ', array_column($cols, 'Field'));
  } catch (\Throwable $e) {
      $log[] = 'messages columns ERROR: ' . $e->getMessage();
  }

  // 5. Check columns in task_files  
  try {
      $cols = $pdo->query("SHOW COLUMNS FROM task_files")->fetchAll();
      $log[] = 'task_files columns: ' . implode(', ', array_column($cols, 'Field'));
  } catch (\Throwable $e) {
      $log[] = 'task_files columns ERROR: ' . $e->getMessage();
  }

  // 6. Check columns in users
  try {
      $cols = $pdo->query("SHOW COLUMNS FROM users")->fetchAll();
      $log[] = 'users columns: ' . implode(', ', array_column($cols, 'Field'));
  } catch (\Throwable $e) {
      $log[] = 'users columns ERROR: ' . $e->getMessage();
  }

  // 7. Count records
  foreach (['users','tasks','applications','messages'] as $tbl) {
      try {
          $cnt = $pdo->query("SELECT COUNT(*) FROM `{$tbl}`")->fetchColumn();
          $log[] = "Записей в {$tbl}: {$cnt}";
      } catch (\Throwable $e) { $log[] = "{$tbl} count ERROR: " . $e->getMessage(); }
  }

  // 8. If app_id provided, check access
  if ($appId > 0 && $userId > 0) {
      try {
          $stmt = $pdo->prepare("SELECT a.id, a.student_id, a.status, t.employer_id, t.title
              FROM applications a JOIN tasks t ON t.id = a.task_id WHERE a.id = :id");
          $stmt->execute([':id' => $appId]);
          $app = $stmt->fetch();
          if ($app) {
              $log[] = "Заявка {$appId}: статус={$app['status']}, студент={$app['student_id']}, работодатель={$app['employer_id']}";
              $isAllowed = ($userId == $app['student_id'] || $userId == $app['employer_id']);
              $log[] = "Доступ userId={$userId}: " . ($isAllowed ? 'РАЗРЕШЁН' : 'ЗАПРЕЩЁН!');
              
              // Count messages for this app
              $msgCnt = $pdo->prepare("SELECT COUNT(*) FROM messages WHERE application_id = :id");
              $msgCnt->execute([':id' => $appId]);
              $log[] = "Сообщений для заявки {$appId}: " . $msgCnt->fetchColumn();
          } else {
              $log[] = "Заявка {$appId}: НЕ НАЙДЕНА!";
          }
      } catch (\Throwable $e) {
          $log[] = 'app check ERROR: ' . $e->getMessage();
      }
      
      // 9. Try to insert a test message (then delete)
      try {
          $stmt = $pdo->prepare("INSERT INTO messages (application_id, sender_id, sender_name, message, created_at)
              VALUES (:app_id, :sid, 'TEST', 'тест сообщение', :cat)");
          $stmt->execute([':app_id' => $appId, ':sid' => $userId, ':cat' => (int)(microtime(true)*1000)]);
          $testId = $pdo->lastInsertId();
          $pdo->exec("DELETE FROM messages WHERE id = {$testId}");
          $log[] = "Тест INSERT+DELETE в messages: OK (id={$testId} удалён)";
      } catch (\Throwable $e) {
          $log[] = "Тест INSERT в messages FAILED: " . $e->getMessage();
      }
  }

  // 10. uploads dir check
  $uploadsDir = __DIR__ . '/uploads';
  $log[] = 'uploads dir exists: ' . (is_dir($uploadsDir) ? 'YES' : 'NO');
  $log[] = 'uploads writable: ' . (is_writable($uploadsDir) ? 'YES' : 'NO');

  echo json_encode(['success'=>true,'log'=>$log], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
  