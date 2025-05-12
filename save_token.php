<?php
header("Content-Type: application/json");
include 'get_users.php'; // need to connect this to the actual DB

$user_id = $_POST['user_id'];
$device_token = $_POST['device_token'];

$stmt = $conn->prepare("UPDATE users SET device_token=? WHERE user_id=?");
$stmt->bind_param("si", $device_token, $user_id);

if ($stmt->execute()) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "error" => $stmt->error]);
}
?>
