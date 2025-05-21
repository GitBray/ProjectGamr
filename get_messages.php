<?php
header('Content-Type: application/json');
error_reporting(0);
ini_set('display_errors', 0);

$conn = new mysqli("localhost", "root", "", "gamr");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Connection failed"]);
    exit;
}

// Accept user1 and user2 from GET query params
$user1 = isset($_GET['user1']) ? intval($_GET['user1']) : null;
$user2 = isset($_GET['user2']) ? intval($_GET['user2']) : null;

if ($user1 === null || $user2 === null) {
    echo json_encode(["status" => "error", "message" => "Missing user IDs"]);
    exit;
}

// Fetch all messages between user1 and user2
$sql = "SELECT message_id, sender_id, reciever_id, message, TIMESTAMP as timestamp FROM messages 
        WHERE (sender_id = ? AND reciever_id = ?) 
           OR (sender_id = ? AND reciever_id = ?)
        ORDER BY timestamp ASC";
$stmt = $conn->prepare($sql);
$stmt->bind_param("iiii", $user1, $user2, $user2, $user1);
$stmt->execute();

$result = $stmt->get_result();
$messages = [];

while ($row = $result->fetch_assoc()) {
    $messages[] = $row;
}

// Output clean JSON
echo json_encode($messages);
?>
