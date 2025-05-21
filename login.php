<?php
//Assistance from ChatGPT
ob_start(); // Start output buffering

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "gamr");

if ($conn->connect_error) {
    http_response_code(500);
    ob_clean();
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$username = $_POST['username'] ?? $_GET['username'] ?? '';
$password = $_POST['password'] ?? $_GET['password'] ?? '';

if (empty($username) || empty($password)) {
    ob_clean();
    echo json_encode(["status" => "error", "message" => "Missing fields"]);
    exit;
}

$stmt = $conn->prepare("SELECT user_id, password FROM users WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();

ob_clean(); // Clean all output BEFORE sending JSON

if ($row = $result->fetch_assoc()) {
    if ($password == $row['password']) {
        echo json_encode(["status" => "success", "user_id" => $row['user_id']]);
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid password"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "User not found"]);
}

ob_end_flush(); // Send output cleanly
?>
