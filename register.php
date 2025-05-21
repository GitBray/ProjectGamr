<?php
// Assistance from ChatGPT
ob_start();

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "gamr");

$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';

if (empty($username) || empty($password)) {
    echo json_encode(["status" => "error", "message" => "Missing fields"]);
    ob_end_flush();
    exit;
}

// Check if user already exists
$stmt = $conn->prepare("SELECT * FROM users WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Username already exists"]);
    ob_end_flush();
    exit;
}

// Create new user with default values
$defaultGamertag = $username;
$defaultPlaystyle = "Casual";
$defaultGenre = "FPS";
$defaultGame = "Call of Duty";
$defaultBio = "Hey there! I'm new here.";
$defaultLat = 32.5282;
$defaultLon = -92.6379;

$insert = $conn->prepare("
    INSERT INTO users (
        username, password, gamertag,
        preferred_playstyle, current_game_genre, current_game,
        bio, latitude, longitude
    )
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
");

$insert->bind_param(
    "sssssssdd",
    $username, $password, $defaultGamertag,
    $defaultPlaystyle, $defaultGenre, $defaultGame,
    $defaultBio, $defaultLat, $defaultLon
);

if ($insert->execute()) {
    echo json_encode(["status" => "success", "user_id" => $insert->insert_id]);
} else {
    echo json_encode(["status" => "error", "message" => "Registration failed"]);
}

$insert->close();
$conn->close();
ob_end_flush();
?>
