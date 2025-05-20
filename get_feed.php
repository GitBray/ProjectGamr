<?php
// Assistance from ChatGPT 

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Connects to my MySQL database, needs to change the password to your password on MySQL!!
$conn = new mysqli("localhost", "root", "", "gamr");

if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

// Safely grab user_id from GET
$userId = $_GET['user_id'] ?? null;

if (!$userId) {
    echo json_encode(["success" => false, "error" => "Missing user_id"]);
    exit;
}

// convers user to int to prevent injection
$userId = intval($userId); 

$sql = "SELECT * FROM users
        WHERE user_id != $userId
        AND user_id NOT IN (
            SELECT swipee_id FROM swipes WHERE swiper_id = $userId
        )";

//runs query and fetch results into an array
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
