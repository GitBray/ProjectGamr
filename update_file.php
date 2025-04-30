<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Connects to my MySQL database, needs to change the password to your password on MySQL!!
$conn = new mysqli("localhost", "root", "ZAQ!1qazXSW@2wsx", "gamr"); //Connect to database

$userId = $_POST['user_id'];
$bio = $_POST['bio'];
$discord = $_POST['discord'];
$instagram = $_POST['instagram'];
$playingStyle = $_POST['playing_style'];

$sql = "UPDATE users SET bio=?, discord=?, instagram=?, playing_style=? WHERE user_id=?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ssssi", $bio, $discord, $instagram, $playingStyle, $userId);

if ($stmt->execute()) {
    echo json_encode(["status" => "success"]);
} else {
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$stmt->close();
$conn->close();
?>