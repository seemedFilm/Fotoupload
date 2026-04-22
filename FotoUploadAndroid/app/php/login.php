<?php

declare(strict_types=1);

header('Content-Type: application/json');

// 🔒 Nur POST erlauben
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["error" => "Method not allowed"]);
    exit;
}

// 🔌 DB laden
require_once __DIR__ . '/config/database.php';

try {
    $db = Database::get();
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(["error" => "Database connection failed"]);
    exit;
}

// 📥 JSON lesen
$rawInput = file_get_contents("php://input");
$data = json_decode($rawInput, true);

if (!is_array($data)) {
    http_response_code(400);
    echo json_encode(["error" => "Invalid JSON"]);
    exit;
}

$username = $data['username'] ?? null;
$password = $data['password'] ?? null;

if (!$username || !$password) {
    http_response_code(400);
    echo json_encode(["error" => "Missing credentials"]);
    exit;
}

try {

    // 👤 User suchen
    $stmt = $db->prepare("
        SELECT id, password_hash 
        FROM users 
        WHERE username = :username
        LIMIT 1
    ");
    $stmt->execute(["username" => $username]);
    $user = $stmt->fetch();

    if (!$user || !password_verify($password, $user['password_hash'])) {
        http_response_code(401);
        echo json_encode(["error" => "Invalid credentials"]);
        exit;
    }

    // 🎟️ Token generieren
    $token = bin2hex(random_bytes(32));
   
  // Ablaufzeit (10 Minuten)
    $expiresAt = (new DateTime('+10 minutes'))->format('Y-m-d H:i:s');

// Alte Tokens löschen
    $db->prepare("
        DELETE FROM enrollment_tokens
        WHERE expires_at < datetime('now')
    ")->execute();

    // Token speichern
    $stmt = $db->prepare("
        INSERT INTO enrollment_tokens (token, user_id, expires_at)
        VALUES (:token, :user_id, :expires_at)
    ");
    $stmt->execute([
        "token" => $token,
        "user_id" => $user['id'],
        "expires_at" => $expiresAt
    ]);

    echo json_encode([
        "token" => $token
    ]);

} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(["error" => "Server error"]);
}
