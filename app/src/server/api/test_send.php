<?php
  /**
   * test_send.php — тест отправки сообщения.
   * Открыть: http://46.173.28.109/api/test_send.php
   * УДАЛИТЬ после проверки!
   */
  ob_start();

  register_shutdown_function(function () {
      $err = error_get_last();
      $out = ob_get_clean();
      if ($err && in_array($err['type'], [E_ERROR, E_PARSE, E_CORE_ERROR, E_COMPILE_ERROR])) {
          header('Content-Type: application/json; charset=utf-8');
          echo json_encode(['step'=>'fatal','error'=> $err['message'],'line'=>$err['line']]);
      } else {
          echo $out;
      }
  });

  header('Content-Type: application/json; charset=utf-8');
  header('Access-Control-Allow-Origin: *');

  $log = [];

  // 1. Find a real IN_PROGRESS application
  try {
      $pdo = new PDO('mysql:host=localhost;dbname=student_gig_db;charset=utf8mb4',
          'gig_user', 'Sena090909.',
          [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC]);
      $log[] = 'БД: ОК';
  } catch (Throwable $e) {
      echo json_encode(['success'=>false,'log'=>$log,'error'=>'БД: '.$e->getMessage()], JSON_UNESCAPED_UNICODE);
      ob_end_flush(); exit;
  }

  $app = $pdo->query("SELECT a.id, a.student_id, t.employer_id FROM applications a JOIN tasks t ON t.id=a.task_id WHERE a.status='IN_PROGRESS' LIMIT 1")->fetch();
  if (!$app) {
      echo json_encode(['success'=>false,'log'=>$log,'error'=>'Нет заявок IN_PROGRESS'], JSON_UNESCAPED_UNICODE);
      ob_end_flush(); exit;
  }
  $appId = $app['id'];
  $userId = $app['student_id'];
  $log[] = "Заявка id={$appId}, student_id={$app['student_id']}, employer_id={$app['employer_id']}";

  // 2. Check messages table
  try {
      $cols = $pdo->query("SHOW COLUMNS FROM messages")->fetchAll();
      $log[] = 'messages columns: ' . implode(', ', array_column($cols, 'Field'));
  } catch (Throwable $e) {
      $log[] = 'messages table missing: ' . $e->getMessage();
      // Create it
      $pdo->exec("CREATE TABLE IF NOT EXISTS `messages` (
          `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
          `application_id` BIGINT UNSIGNED NOT NULL,
          `sender_id` BIGINT UNSIGNED NOT NULL,
          `sender_name` VARCHAR(255) NOT NULL DEFAULT '',
          `message` TEXT NOT NULL,
          `created_at` BIGINT NOT NULL,
          KEY `idx_app_time` (`application_id`, `created_at`)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
      $log[] = 'messages table: СОЗДАНА';
  }

  // 3. Try INSERT then DELETE (rollback test)
  try {
      $ts = (int)(microtime(true)*1000);
      $pdo->beginTransaction();
      $stmt = $pdo->prepare("INSERT INTO messages (application_id,sender_id,sender_name,message,created_at) VALUES (:a,:s,:n,:m,:t)");
      $stmt->execute([':a'=>$appId,':s'=>$userId,':n'=>'Тест','m'=>'тест сообщение',':t'=>$ts]);
      $newId = $pdo->lastInsertId();
      $pdo->rollBack();
      $log[] = "INSERT тест: ОК (id={$newId}, откат сделан — реальных данных не создано)";
  } catch (Throwable $e) {
      if ($pdo->inTransaction()) $pdo->rollBack();
      $log[] = "INSERT ОШИБКА: " . $e->getMessage();
      echo json_encode(['success'=>false,'log'=>$log], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
      ob_end_flush(); exit;
  }

  // 4. Call send_message.php internally
  $postData = json_encode(['application_id'=>(int)$appId,'sender_id'=>(int)$userId,'sender_name'=>'ТестПользователь','message'=>'Тестовое сообщение из test_send.php']);
  $ctx = stream_context_create(['http'=>[
      'method'=>'POST',
      'header'=>"Content-Type: application/json\r\nContent-Length: ".strlen($postData)."\r\n",
      'content'=>$postData,
      'ignore_errors'=>true,
      'timeout'=>10,
  ]]);
  $proto = (!empty($_SERVER['HTTPS'])&&$_SERVER['HTTPS']!=='off')?'https':'http';
  $host  = $_SERVER['HTTP_HOST'] ?? 'localhost';
  $apiDir = rtrim(dirname($_SERVER['SCRIPT_NAME']),'/');
  $sendUrl = $proto.'://'.$host.$apiDir.'/send_message.php';
  $log[] = "Вызов: POST $sendUrl";

  $result = @file_get_contents($sendUrl, false, $ctx);
  $log[] = "HTTP status: " . (isset($http_response_header[0]) ? $http_response_header[0] : 'unknown');
  $log[] = "Ответ: " . ($result === false ? 'FALSE (cURL failed)' : (strlen($result)==0 ? 'ПУСТОЙ!' : substr($result,0,200)));

  $parsed = $result ? json_decode($result, true) : null;
  $success = $parsed && $parsed['success'] === true;

  echo json_encode([
      'success'   => $success,
      'message'   => $success ? 'Чат работает!' : 'Чат НЕ работает — смотрите log',
      'log'       => $log,
      'send_response' => $parsed,
  ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);

  ob_end_flush();
  