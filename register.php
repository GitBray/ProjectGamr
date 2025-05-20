<?php
// Assistance from ChatGPT
// Enable error reporting
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// Set response headers
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json");

// Connect to DB
$conn = new mysqli("localhost", "root", "", "gamr");

// Check connection
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "DB connection failed"]);
    exit;
}

// Get POST data
$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';

// Validate input
if (empty($username) || empty($password)) {
    echo json_encode(["status" => "error", "message" => "Missing fields"]);
    exit;
}

// Check if user exists
$stmt = $conn->prepare("SELECT user_id FROM users WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Username already exists"]);
    $stmt->close();
    $conn->close();
    exit;
}
$stmt->close();

// Insert new user with plain password
$insert = $conn->prepare("INSERT INTO users (username, password, gamertag) VALUES (?, ?, ?)");
$insert->bind_param("sss", $username, $password, $username);
if ($insert->execute()) {
    echo json_encode(["status" => "success", "user_id" => $insert->insert_id]);
} else {
    echo json_encode(["status" => "error", "message" => "Registration failed"]);
}
$insert->close();
$conn->close();
?>
