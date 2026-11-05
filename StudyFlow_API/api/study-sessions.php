
<?php

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(200);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    http_response_code(405);
    echo json_encode([
        "success" => false,
        "message" => "Only POST requests are allowed."
    ]);
    exit;
}

require_once __DIR__ . "/../config/auth.php";

$user = requireAuth($pdo);
$userId = (int)$user["id"];

$data = json_decode(file_get_contents("php://input"), true) ?? [];

$subject = trim($data["subject"] ?? "");
$duration = (int)($data["durationMinutes"] ?? 0);

if ($subject === "" || $duration <= 0) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Subject and a valid duration are required."
    ]);
    exit;
}

try {

    $stmt = $pdo->prepare(
        "INSERT INTO study_sessions
         (user_id, subject, duration_minutes, session_date)
         VALUES (?, ?, ?, CURDATE())"
    );

    $stmt->execute([
        $userId,
        $subject,
        $duration
    ]);

    // Calculate and return updated progress
    require __DIR__ . "/progress-calculation.php";

} catch (PDOException $e) {

    http_response_code(500);

    echo json_encode([
        "success" => false,
        "message" => "Could not save study session."
    ]);
}
