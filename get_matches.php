<?php
//Assistance from ChatGPT
header('Content-Type: application/json');
error_reporting(0);
ini_set('display_errors', 0); // error detection removed as a safety net for output reading
                                // will be removed after more rigorous testing

$conn = new mysqli("localhost","root","", "gamr");

if (!isset($_GET['user_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing user_id"]);
    exit;
}

$user_id = intval($_GET['user_id']);

// This query fetches all matched users for the given user_id
$sql = "
    SELECT u.user_id, u.gamertag, u.name, u.age, u.preferred_playstyle, 
           u.current_game, u.current_game_genre, u.bio, u.latitude, u.longitude
    FROM matches m
    JOIN users u ON (
        (m.user1_id = ? AND u.user_id = m.user2_id) OR
        (m.user2_id = ? AND u.user_id = m.user1_id)
    )
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $user_id, $user_id);
$stmt->execute();
$result = $stmt->get_result();

$matches = [];

while ($row = $result->fetch_assoc()) {
    $matches[] = $row;
}

echo json_encode($matches);
?>
