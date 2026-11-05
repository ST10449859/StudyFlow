
<?php

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: PUT, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(200);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] !== "PUT") {
    http_response_code(405);
    echo json_encode([
        "success" => false,
        "message" => "Only PUT requests are allowed."
    ]);
    exit;
}

require_once __DIR__ . "/../config/auth.php";

$user = requireAuth($pdo);
$userId = (int)$user["id"];

$data = json_decode(file_get_contents("php://input"), true) ?? [];

$fullName = trim($data["fullName"] ?? "");
$weeklyStudyGoal = (int)($data["weeklyStudyGoal"] ?? 0);
$language = trim($data["language"] ?? "English");

if ($fullName === "" || $weeklyStudyGoal <= 0 || $language === "") {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Please provide valid settings."
    ]);
    exit;
}

try {

    $stmt = $pdo->prepare(
        "UPDATE users
         SET full_name = ?,
             weekly_study_goal = ?,
             language = ?
         WHERE id = ?"
    );

    $stmt->execute([
        $fullName,
        $weeklyStudyGoal,
        $language,
        $userId
    ]);

    echo json_encode([
        "id" => (string)$userId,
        "fullName" => $fullName,
        "email" => $user["email"],
        "weeklyStudyGoal" => $weeklyStudyGoal,
        "language" => $language
    ]);

} catch (PDOException $e) {

    http_response_code(500);

    echo json_encode([
        "success" => false,
        "message" => "Could not save settings."
    ]);
}
