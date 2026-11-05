
<?php

require_once __DIR__ . "/database.php";

function requireAuth(PDO $pdo): array
{
    $headers = function_exists("getallheaders")
        ? getallheaders()
        : [];

    $authorization = $headers["Authorization"]
        ?? $headers["authorization"]
        ?? $_SERVER["HTTP_AUTHORIZATION"]
        ?? $_SERVER["REDIRECT_HTTP_AUTHORIZATION"]
        ?? "";

    if (!preg_match(
        '/^Bearer\s+([a-f0-9]{64})$/i',
        $authorization,
        $matches
    )) {
        http_response_code(401);
        echo json_encode([
            "success" => false,
            "message" => "Please log in."
        ]);
        exit;
    }

    $tokenHash = hash("sha256", $matches[1]);

    $stmt = $pdo->prepare(
        "SELECT u.id, u.full_name, u.email
         FROM api_tokens t
         INNER JOIN users u ON u.id = t.user_id
         WHERE t.token_hash = ?
         AND t.expires_at > NOW()
         LIMIT 1"
    );

    $stmt->execute([$tokenHash]);
    $user = $stmt->fetch();

    if (!$user) {
        http_response_code(401);
        echo json_encode([
            "success" => false,
            "message" => "Invalid or expired token."
        ]);
        exit;
    }

    return $user;
}
