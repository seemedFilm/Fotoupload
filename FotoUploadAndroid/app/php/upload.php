<?php
/* --- DEBUG LOGGING START --- */
$logFile = __DIR__ . '/var/www/html/upload_debug.log';
$debugData = [
    'timestamp' => date('Y-m-d H:i:s'),
    'method' => $_SERVER['REQUEST_METHOD'] ?? 'N/A',
    'uri' => $_SERVER['REQUEST_URI'] ?? 'N/A',
    'headers' => getallheaders(),
    'ssl_verify' => $_SERVER['SSL_CLIENT_VERIFY'] ?? 'NOT_SET',
    'http_ssl_verify' => $_SERVER['HTTP_SSL_CLIENT_VERIFY'] ?? 'NOT_SET',
    'files' => $_FILES,
    'post_data' => $_POST,
];
file_put_contents($logFile, print_r($debugData, true) . "\n" . str_repeat("-", 40) . "\n", FILE_APPEND);
/* --- DEBUG LOGGING END --- */

// ... rest of your code ...
error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json');

$mediaDir = __DIR__ . '/media/';
$dbPath   = '/opt/upload/db/upload.sqlite';

$allowedImages = ['jpg','jpeg','png','gif','webp'];
$allowedVideos = ['mp4','webm','ogg'];
$maxFileSize   = 100 * 1024 * 1024; // 100MB


/* ==============================
   mTLS Prüfung
============================== */

// In upload.php, update the mTLS check:
$clientVerify = $_SERVER['SSL_CLIENT_VERIFY'] ?? $_SERVER['HTTP_SSL_CLIENT_VERIFY'] ?? null;
$clientSerial = $_SERVER['SSL_CLIENT_SERIAL'] ?? $_SERVER['HTTP_SSL_CLIENT_SERIAL'] ?? null;

if ($clientVerify !== "SUCCESS") {
    http_response_code(403);
    echo json_encode(['error' => 'client_certificate_required', 'debug_received' => $clientVerify]);
    exit;
}

$clientSerial = $_SERVER['SSL_CLIENT_SERIAL'] ?? null;

if (!$clientSerial) {
    http_response_code(403);
    echo json_encode(['error' => 'missing_client_serial']);
    exit;
}


/* ==============================
   DB Verbindung
============================== */

try {
    $db = new SQLite3($dbPath);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => 'db_connection_failed']);
    exit;
}


/* ==============================
   Device prüfen
============================== */

$stmt = $db->prepare('SELECT revoked FROM devices WHERE certificate_serial = :serial');
$stmt->bindValue(':serial', $clientSerial, SQLITE3_TEXT);

$result = $stmt->execute();
$row = $result->fetchArray(SQLITE3_ASSOC);

if (!$row) {
    http_response_code(403);
    echo json_encode(['error' => 'unknown_device']);
    exit;
}

if ($row['revoked'] == 1) {
    http_response_code(403);
    echo json_encode(['error' => 'device_revoked']);
    exit;
}


/* ==============================
   Hilfsfunktionen
============================== */

function getMimeType($filePath) {

    if (function_exists('finfo_open')) {
        $finfo = finfo_open(FILEINFO_MIME_TYPE);
        $mime = finfo_file($finfo, $filePath);
        finfo_close($finfo);
        if ($mime) return $mime;
    }

    return mime_content_type($filePath) ?: 'application/octet-stream';
}

function sanitizeFilename($name) {
    $name = preg_replace('/[^a-zA-Z0-9._\-]/u', '_', $name);
    return trim($name, '.-_');
}


/* ==============================
   Upload prüfen
============================== */

if (!isset($_FILES['files'])) {
    http_response_code(400);
    echo json_encode(['error' => 'no_files']);
    exit;
}

if (!is_dir($mediaDir)) {
    mkdir($mediaDir, 0755, true);
}

$response = [];


/* ==============================
   Dateien verarbeiten
============================== */

foreach ($_FILES['files']['tmp_name'] as $index => $tmpName) {

    $error = $_FILES['files']['error'][$index];
    $size  = $_FILES['files']['size'][$index];
    $name  = $_FILES['files']['name'][$index];

    if ($error !== UPLOAD_ERR_OK) {
        $response[] = ['file' => $name, 'status' => 'error', 'reason' => 'upload_error'];
        continue;
    }

    if ($size > $maxFileSize || $size === 0) {
        $response[] = ['file' => $name, 'status' => 'error', 'reason' => 'file_too_large'];
        continue;
    }

    $ext  = strtolower(pathinfo($name, PATHINFO_EXTENSION));
    $mime = getMimeType($tmpName);

    if (!preg_match('/^(image|video)\//', $mime)) {
        $response[] = ['file' => $name, 'status' => 'error', 'reason' => 'invalid_mime'];
        continue;
    }

    if (!in_array($ext, array_merge($allowedImages, $allowedVideos))) {
        $response[] = ['file' => $name, 'status' => 'error', 'reason' => 'invalid_extension'];
        continue;
    }


    /* ==============================
       Hash berechnen
    ============================== */

    $hash = hash_file('sha256', $tmpName);


    /* ==============================
       Duplikat prüfen (DB)
    ============================== */

    $stmt = $db->prepare('SELECT id FROM uploads WHERE hash = :hash');
    $stmt->bindValue(':hash', $hash, SQLITE3_TEXT);

    $exists = $stmt->execute()->fetchArray(SQLITE3_ASSOC);

    if ($exists) {
        $response[] = [
            'file' => $name,
            'status' => 'duplicate',
            'reason' => 'already_uploaded'
        ];
        continue;
    }


    /* ==============================
       Dateiname bauen
    ============================== */

    $timestamp = date('Ymd_His');
    $cleanName = sanitizeFilename($name);
    $shortHash = substr($hash, 0, 12);

    $safeName = "{$timestamp}_{$shortHash}_{$cleanName}";


    /* ==============================
       Datei speichern
    ============================== */

    if (!move_uploaded_file($tmpName, $mediaDir . $safeName)) {

        $response[] = [
            'file' => $name,
            'status' => 'error',
            'reason' => 'move_failed'
        ];
        continue;
    }


    /* ==============================
       In DB speichern
    ============================== */

    $stmt = $db->prepare('
        INSERT INTO uploads (hash, filename, device_serial)
        VALUES (:hash, :filename, :device)
    ');

    $stmt->bindValue(':hash', $hash, SQLITE3_TEXT);
    $stmt->bindValue(':filename', $safeName, SQLITE3_TEXT);
    $stmt->bindValue(':device', $clientSerial, SQLITE3_TEXT);

    $stmt->execute();


    $response[] = [
        'file' => $name,
        'status' => 'ok',
        'saved_as' => $safeName,
        'url' => 'media/' . $safeName,
        'hash' => $shortHash
    ];
}


/* ==============================
   Response
============================== */

echo json_encode([
    'status' => 'completed',
    'files' => $response
]);
?>