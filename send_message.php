<?php
// Assistance from ChatGPT
header('Content-Type: application/json');
error_reporting(0);
ini_set('display_errors', 0);

$conn = new mysqli("localhost", "root","passhere","gamr");

// Use typo in variable name to match external tools
if (!isset($_POST['sender_id'], $_POST['reciever_id'], $_POST['message'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$sender_id = $_POST['sender_id'] ?? null;
$receiver_id = $_POST['reciever_id'] ?? null;
$message = $_POST['message'] ?? null;
$timestamp = $_POST['timestamp'] ?? date('Y-m-d H:i:s'); // fallback if not provided

if (!$sender_id || !$receiver_id || !$message) {
    echo json_encode(['status' => 'error', 'message' => 'Missing fields']);
    exit();
}

$sql = "INSERT INTO messages (sender_id, reciever_id, message, TIMESTAMP) VALUES (?, ?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("iiss", $sender_id, $receiver_id, $message, $timestamp);

if ($stmt->execute()) {
    echo json_encode(['status' => 'success', 'message' => 'Message sent']);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Failed to send message']);
}

