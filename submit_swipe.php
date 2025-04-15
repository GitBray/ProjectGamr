<?php
// Assistance from ChatGPT

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Connects to my MySQL database, needs to change the password to your password on MySQL!! 
$conn = new mysqli("localhost", "root", "ZAQ!1qazXSW@2wsx", "gamr");

if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

// Grabs and validate POST or GET data
$swiperId = $_POST['swiper_id'] ?? $_GET['swiper_id'] ?? null;
$swipeeId = $_POST['swipee_id'] ?? $_GET['swipee_id'] ?? null;
$direction = $_POST['direction'] ?? $_GET['direction'] ?? null;

// Validate required fields
if (!$swiperId || !$swipeeId || !$direction) {
    echo json_encode([
        "success" => false,
        "error" => "Missing swiper_id, swipee_id, or direction"
    ]);
    exit;
}

// Insert a new swipe or update the existing one (if a pair exists)
$sql = "INSERT INTO swipes (swiper_id, swipee_id, direction)
        VALUES ($swiperId, $swipeeId, '$direction')
        ON DUPLICATE KEY UPDATE direction = '$direction'";

// sends JSON response to android app
if ($conn->query($sql) === TRUE) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "error" => $conn->error]);
}

$conn->close();
?>
