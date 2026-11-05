
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

if ($email === "" || $password === "") {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Email and password are required."
    ]);
    exit;
}

try {
    $stmt = $pdo->prepare(
        "SELECT id, full_name, email, password_hash,
                weekly_study_goal, language
         FROM users
         WHERE email = ?"
    );

    $stmt->execute([$email]);
    $user = $stmt->fetch();

    if (!$user || !password_verify($password, $user["password_hash"])) {
        http_response_code(401);
        echo json_encode([
            "success" => false,
            "message" => "Invalid email or password."
        ]);
        exit;
    }

    $token = bin2hex(random_bytes(32));
    $tokenHash = hash("sha256", $token);

    $expiresAt = date(
        "Y-m-d H:i:s",
        strtotime("+30 days")
    );

    $tokenStmt = $pdo->prepare(
        "INSERT INTO api_tokens (user_id, token_hash, expires_at)
         VALUES (?, ?, ?)"
    );

    $tokenStmt->execute([
        $user["id"],
        $tokenHash,
        $expiresAt
    ]);

    echo json_encode([
        "token" => $token,
        "user" => [
            "id" => (string)$user["id"],
            "fullName" => $user["full_name"],
            "email" => $user["email"],
            "weeklyStudyGoal" => (int)$user["weekly_study_goal"],
            "language" => $user["language"]
        ]
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Login failed."
    ]);
}
