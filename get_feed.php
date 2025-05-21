<?php
// Assistance from ChatGPT

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Connects to MySQL database
$conn = new mysqli("localhost", "root", "", "gamr");

if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

// Get user_id from GET safely
$userId = $_GET['user_id'] ?? null;

if (!$userId) {
    echo json_encode(["success" => false, "error" => "Missing user_id"]);
    exit;
}

$userId = intval($userId); // sanitize for safety

$sql = "SELECT user_id, gamertag, username, name, age, preferred_playstyle, current_game,
               current_game_genre, bio, latitude, longitude, discord, instagram, playing_style, image_url
        FROM users
        WHERE user_id != $userId
        AND user_id NOT IN (
            SELECT swipee_id FROM swipes WHERE swiper_id = $userId
        )";

$result = $conn->query($sql);
$users = [];

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $users[] = $row;
    }
}

echo json_encode($users);
$conn->close();
?>
