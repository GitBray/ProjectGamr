<?php
// Assistance from ChatGPT

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "gamr");

$userId = $_GET['user_id'] ?? null;

if (!$userId) {
    echo json_encode(["status" => "error", "message" => "Missing user_id"]);
    exit;
}

// Expanded query to include all relevant fields for profile display
$stmt = $conn->prepare("SELECT
    user_id,
    gamertag,
    username,
    name,
    age,
    current_game,
    current_game_genre,
    bio,
    discord,
    instagram,
    preferred_playstyle AS playing_style,
    image_url
    FROM users
    WHERE user_id = ?");

$stmt->bind_param("i", $userId);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    echo json_encode($row);
} else {
    echo json_encode(["status" => "error", "message" => "User not found"]);
}

$stmt->close();
$conn->close();
?>
