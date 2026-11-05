
<?php

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
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

    // GET: Retrieve the logged-in user's tasks
    if ($method === "GET") {

        $stmt = $pdo->prepare(
            "SELECT id, title, description, subject,
                    DATE_FORMAT(due_date, '%Y-%m-%d') AS dueDate,
                    priority, completed
             FROM tasks
             WHERE user_id = ?
             ORDER BY due_date ASC"
        );

        $stmt->execute([$userId]);

        $tasks = $stmt->fetchAll();

        foreach ($tasks as &$task) {
            $task["completed"] = (bool)$task["completed"];
        }

        echo json_encode($tasks);
        exit;
    }

    // POST: Create a task
    if ($method === "POST") {

        $id = $data["id"] ?? bin2hex(random_bytes(16));
        $title = trim($data["title"] ?? "");
        $description = $data["description"] ?? null;
        $subject = trim($data["subject"] ?? "");
        $dueDate = $data["dueDate"] ?? "";
        $priority = $data["priority"] ?? "Medium";

        if ($title === "" || $subject === "" || $dueDate === "") {
            http_response_code(400);
            echo json_encode([
                "success" => false,
                "message" => "Title, subject and due date are required."
            ]);
            exit;
        }

        $stmt = $pdo->prepare(
            "INSERT INTO tasks
             (id, user_id, title, description, subject, due_date, priority)
             VALUES (?, ?, ?, ?, ?, ?, ?)"
        );

        $stmt->execute([
            $id,
            $userId,
            $title,
            $description,
            $subject,
            $dueDate,
            $priority
        ]);

        echo json_encode([
            "id" => $id,
            "title" => $title,
            "description" => $description,
            "subject" => $subject,
            "dueDate" => $dueDate,
            "priority" => $priority,
            "completed" => false
        ]);

        exit;
    }

    // PUT: Update an existing task
    if ($method === "PUT") {

        $id = $_GET["id"] ?? "";
        $title = trim($data["title"] ?? "");
        $description = $data["description"] ?? null;
        $subject = trim($data["subject"] ?? "");
        $dueDate = $data["dueDate"] ?? "";
        $priority = $data["priority"] ?? "Medium";
        $completed = (bool)($data["completed"] ?? false);

        if ($id === "" || $title === "" || $subject === "" || $dueDate === "") {
            http_response_code(400);
            echo json_encode([
                "success" => false,
                "message" => "Task ID, title, subject and due date are required."
            ]);
            exit;
        }

        $stmt = $pdo->prepare(
            "UPDATE tasks
             SET title = ?, description = ?, subject = ?,
                 due_date = ?, priority = ?, completed = ?
             WHERE id = ? AND user_id = ?"
        );

        $stmt->execute([
            $title,
            $description,
            $subject,
            $dueDate,
            $priority,
            $completed ? 1 : 0,
            $id,
            $userId
        ]);

        $check = $pdo->prepare(
            "SELECT id FROM tasks WHERE id = ? AND user_id = ?"
        );
        $check->execute([$id, $userId]);

        if (!$check->fetch()) {
            http_response_code(404);
            echo json_encode([
                "success" => false,
                "message" => "Task not found."
            ]);
            exit;
        }

        echo json_encode([
            "id" => $id,
            "title" => $title,
            "description" => $description,
            "subject" => $subject,
            "dueDate" => $dueDate,
            "priority" => $priority,
            "completed" => $completed
        ]);

        exit;
    }

    // DELETE: Delete a task
    if ($method === "DELETE") {

        $id = $_GET["id"] ?? "";

        if ($id === "") {
            http_response_code(400);
            echo json_encode([
                "success" => false,
                "message" => "Task ID is required."
            ]);
            exit;
        }

        $stmt = $pdo->prepare(
            "DELETE FROM tasks WHERE id = ? AND user_id = ?"
        );

        $stmt->execute([$id, $userId]);

        if ($stmt->rowCount() === 0) {
            http_response_code(404);
            echo json_encode([
                "success" => false,
                "message" => "Task not found."
            ]);
            exit;
        }

        echo json_encode([
            "success" => true,
            "message" => "Task deleted successfully."
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
        "message" => "Task operation failed."
    ]);
}
