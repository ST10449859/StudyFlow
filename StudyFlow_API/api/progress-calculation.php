
<?php

// Calculate total study minutes
$studyStmt = $pdo->prepare(
    "SELECT COALESCE(SUM(duration_minutes), 0)
     FROM study_sessions
     WHERE user_id = ?"
);

$studyStmt->execute([$userId]);
$studyMinutes = (int)$studyStmt->fetchColumn();

// Calculate completed and total tasks
$taskStmt = $pdo->prepare(
    "SELECT
        COUNT(*) AS total_tasks,
        COALESCE(SUM(completed = 1), 0) AS completed_tasks
     FROM tasks
     WHERE user_id = ?"
);

$taskStmt->execute([$userId]);
$taskData = $taskStmt->fetch();

$totalTasks = (int)$taskData["total_tasks"];
$completedTasks = (int)$taskData["completed_tasks"];

// Calculate XP and level
$xp = $studyMinutes * 10;
$level = (int)floor($xp / 100) + 1;

// Calculate consecutive study-day streak
$datesStmt = $pdo->prepare(
    "SELECT DISTINCT session_date
     FROM study_sessions
     WHERE user_id = ?
     ORDER BY session_date DESC"
);

$datesStmt->execute([$userId]);
$dates = $datesStmt->fetchAll(PDO::FETCH_COLUMN);

$streak = 0;
$expectedDate = new DateTime("today");

foreach ($dates as $date) {

    $sessionDate = new DateTime($date);

    if ($sessionDate->format("Y-m-d") ===
        $expectedDate->format("Y-m-d")) {

        $streak++;
        $expectedDate->modify("-1 day");

    } else {
        break;
    }
}

// Return progress data
echo json_encode([
    "studyMinutes" => $studyMinutes,
    "completedTasks" => $completedTasks,
    "totalTasks" => $totalTasks,
    "xp" => $xp,
    "level" => $level,
    "streak" => $streak
]);
