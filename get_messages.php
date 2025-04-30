<?php
// Assistance from ChatGPT 

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Connects to my MySQL database, needs to change the password to your password on MySQL!!
$conn = new mysqli("localhost", "root", "ZAQ!1qazXSW@2wsx", "gamr");

$uid1 = $_GET['user1'] ?? null;
$uid2 = $_GET['user2'] ?? null;

if (!$uid1 || !$uid2){
    echo json_encode(["success" => false, "error" => "Missing user IDs"]);
    exit;
}

$uid1 = intval($uid1);
$uid2 = intval($uid2);

$sql = "SELECT * FROM messages WHERE (sender_id = $uid1 AND reciever_id = $uid2)
            OR (sender_id = $uid2 AND reciever_id = $uid1) 
            ORDER BY timestamp ASC";

$result = $conn->query($sql);
$messages = [];

while ($row = $result->fetch_assoc()){
    $messages[] = $row;
}

echo json_encode($messages);

$conn->close();
?>