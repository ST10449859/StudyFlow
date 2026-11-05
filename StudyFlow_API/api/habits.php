
<?php

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(200);
    exit;
}

require_once __DIR__ . "/../config/auth.php";

$user = requireAuth($pdo);
$userId = (int)$user["id"];

$method = $_SERVER["REQUEST_METHOD"];
$data = json_decode(file_get_contents("php://input"), true) ?? [];

try {

    // GET: Retrieve habits
    if ($method === "GET") {

        $stmt = $pdo->prepare(
            "SELECT id, title, frequency,
                    current_streak AS currentStreak,
                    longest_streak AS longestStreak
             FROM habits
             WHERE user_id = ?
             ORDER BY id DESC"
        );

        $stmt->execute([$userId]);

        $habits = $stmt->fetchAll();

        foreach ($habits as &$habit) {
            $habit["id"] = (string)$habit["id"];
            $habit["currentStreak"] = (int)$habit["currentStreak"];
            $habit["longestStreak"] = (int)$habit["longestStreak"];
        }

        echo json_encode($habits);
        exit;
    }

    // POST: Create a habit
    if ($method === "POST") {

        $title = trim($data["title"] ?? "");
        $frequency = trim($data["frequency"] ?? "Daily");

        if ($title === "") {
            http_response_code(400);

            echo json_encode([
                "success" => false,
                "message" => "Habit title is required."
            ]);

            exit;
        }

        $stmt = $pdo->prepare(
            "INSERT INTO habits
             (user_id, title, frequency)
             VALUES (?, ?, ?)"
        );

        $stmt->execute([
            $userId,
            $title,
            $frequency
        ]);

        $habitId = $pdo->lastInsertId();

        echo json_encode([
            "id" => (string)$habitId,
            "title" => $title,
            "frequency" => $frequency,
            "currentStreak" => 0,
            "longestStreak" => 0
        ]);

        exit;
    }

    http_response_code(405);

    echo json_encode([
        "success" => false,
        "message" => "Method not allowed."
    ]);

} catch (PDOException $e) {

    http_response_code(500);

    echo json_encode([
        "success" => false,
        "message" => "Habit operation failed."
    ]);
}
