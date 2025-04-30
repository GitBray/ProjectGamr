<?php
// Assistance from ChatGPT 

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Connects to my MySQL database, needs to change the password to your password on MySQL!!
$conn = new mysqli("localhost", "root", "ZAQ!1qazXSW@2wsx", "gamr");

if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

$senderId = $_POST['sender_id'] ?? null;
$recieverId = $_POST['reciever_id'] ?? null;
$message = $_POST['message'] ?? null;

if (!$senderId || !$recieverId || !$message){
    echo json_encode(["success" => false, "error" => "Missing required fields"]);
    exit;
}

$stmt = $conn->prepare("INSERT INTO messages (sender_id, reciever_id, message) VALUES (?, ?, ?");
$stmt->bind_param("iis", $senderId, $recieverId, $message);

if ($stmt->execute()){
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "error" => $stmt->error]);
}

$conn->close();
?>