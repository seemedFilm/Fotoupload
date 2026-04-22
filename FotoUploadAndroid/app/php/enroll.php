<?php

require_once __DIR__ . '/config/database.php';

header('Content-Type: application/json');

try {

    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        http_response_code(405);
        exit(json_encode(['error' => 'Method not allowed']));
    }

    $input = json_decode(file_get_contents('php://input'), true);

    if (!$input || empty($input['token']) || empty($input['csr'])) {
        http_response_code(400);
        exit(json_encode(['error' => 'Invalid payload']));
    }

    $token = $input['token'];
    $csrPem = $input['csr'];

    $db = Database::get();

    // Token prÃ¼fen
    $stmt = $db->prepare("
        SELECT * FROM enrollment_tokens
        WHERE token = :token
        AND expires_at > datetime('now')
    ");
    $stmt->execute(['token' => $token]);
    $tokenRow = $stmt->fetch();

    if (!$tokenRow) {
        http_response_code(403);
        exit(json_encode(['error' => 'Invalid or expired token']));
    }

    $userId = $tokenRow['user_id'];

    // CSR temporÃ¤r speichern
    $tmpCsr = '/opt/pki/tmp/' . bin2hex(random_bytes(8)) . '.csr';
    file_put_contents($tmpCsr, $csrPem);

    // CSR validieren
    $check = shell_exec("openssl req -in $tmpCsr -noout -text 2>&1");
    if (strpos($check, 'Certificate Request') === false) {
        unlink($tmpCsr);
        http_response_code(400);
        exit(json_encode(['error' => 'Invalid CSR']));
    }

    // Zertifikat-Dateiname
    $certPath = '/opt/pki/certs/' . bin2hex(random_bytes(8)) . '.crt';

    // Signieren
    $cmd = sprintf(
        "openssl ca -config /opt/pki/openssl.cnf -in %s -out %s -extensions usr_cert -batch 2>&1",
        escapeshellarg($tmpCsr),
        escapeshellarg($certPath)
    );

    $output = shell_exec($cmd);

    if (!file_exists($certPath)) {
        unlink($tmpCsr);
        http_response_code(500);
        exit(json_encode(['error' => 'Signing failed', 'details' => $output]));
    }

    // Serial extrahieren
    $serialOutput = shell_exec("openssl x509 -in $certPath -noout -serial");
    preg_match('/serial=([0-9A-F]+)/', $serialOutput, $matches);

    if (empty($matches[1])) {
        unlink($tmpCsr);
        unlink($certPath);
        http_response_code(500);
        exit(json_encode(['error' => 'Serial extraction failed']));
    }

    $serial = $matches[1];

    // Subject extrahieren
    $subjectOutput = shell_exec("openssl x509 -in $certPath -noout -subject");
    $subject = trim(str_replace('subject=', '', $subjectOutput));

    // Device speichern
    $stmt = $db->prepare("
        INSERT INTO devices (user_id, device_name, certificate_serial, certificate_subject)
        VALUES (:user_id, :device_name, :serial, :subject)
    ");

    $stmt->execute([
        'user_id' => $userId,
        'device_name' => $subject,
        'serial' => $serial,
        'subject' => $subject
    ]);

    // Token lÃ¶schen
    $stmt = $db->prepare("DELETE FROM enrollment_tokens WHERE token = :token");
    $stmt->execute(['token' => $token]);

    // Zertifikat zurÃ¼ckgeben
    //$certificatePem = file_get_contents($certPath);
    $certificateRaw = file_get_contents($certPath);

preg_match(
    '/-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----/s',
    $certificateRaw,
    $matches
);

if (empty($matches[0])) {
    http_response_code(500);
    exit(json_encode(['error' => 'Certificate extraction failed']));
}

$certificatePem = $matches[0];


    unlink($tmpCsr);

    echo json_encode([
        'certificate' => $certificatePem
    ]);

} catch (Throwable $e) {

    http_response_code(500);
    echo json_encode([
        'error' => 'Server error',
        'details' => $e->getMessage()
    ]);
}
