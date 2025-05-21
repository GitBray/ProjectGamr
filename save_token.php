<?php
header("Content-Type: application/json");

// Add database connection directly
$conn = new mysqli("localhost", "root", "", "gamr");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "Connection failed"]);
    exit;
}

// Get POST data
$user_id = $_POST['user_id'] ?? null;
$device_token = $_POST['device_token'] ?? null;

if (!$user_id || !$device_token) {
    echo json_encode(["success" => false, "error" => "Missing required fields"]);
    exit;
}

// Update token
$stmt = $conn->prepare("UPDATE users SET device_token = ? WHERE user_id = ?");
$stmt->bind_param("si", $device_token, $user_id);

if ($stmt->execute()) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "error" => $stmt->error]);
}

$stmt->close();
$conn->close();
?>
