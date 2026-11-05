
<?php

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

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

require_once __DIR__ . "/../../config/database.php";

$data = json_decode(file_get_contents("php://input"), true);

$email = trim($data["email"] ?? "");
$password = $data["password"] ?? "";
$fullName = trim($data["fullName"] ?? "");

if ($email === "" || $password === "" || $fullName === "") {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Full name, email and password are required."
    ]);
    exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Please enter a valid email address."
    ]);
    exit;
}

if (strlen($password) < 8) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Password must contain at least 8 characters."
    ]);
    exit;
}

try {
    $check = $pdo->prepare(
        "SELECT id FROM users WHERE email = ?"
    );
    $check->execute([$email]);

    if ($check->fetch()) {
        http_response_code(409);
        echo json_encode([
            "success" => false,
            "message" => "An account with this email already exists."
        ]);
        exit;
    }

    $passwordHash = password_hash($password, PASSWORD_DEFAULT);

    $insert = $pdo->prepare(
        "INSERT INTO users (full_name, email, password_hash)
         VALUES (?, ?, ?)"
    );

    $insert->execute([
        $fullName,
        $email,
        $passwordHash
    ]);

    $userId = $pdo->lastInsertId();

    $token = bin2hex(random_bytes(32));
    $tokenHash = hash("sha256", $token);

    $expiresAt = date(
        "Y-m-d H:i:s",
        strtotime("+30 days")
    );

    $tokenInsert = $pdo->prepare(
        "INSERT INTO api_tokens (user_id, token_hash, expires_at)
         VALUES (?, ?, ?)"
    );

    $tokenInsert->execute([
        $userId,
        $tokenHash,
        $expiresAt
    ]);

    echo json_encode([
        "token" => $token,
        "user" => [
            "id" => (string)$userId,
            "fullName" => $fullName,
            "email" => $email,
            "weeklyStudyGoal" => 480,
            "language" => "English"
        ]
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Registration failed."
    ]);
}
