<?php
// Assistance from ChatGPT 

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Connects to my MySQL database, needs to change the password to your password on MySQL!!
$conn = new mysqli("localhost", "root", "", "gamr");

if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

// Gets current user ID from GET
$result = $conn->query("SELECT * FROM users");


$users = [];

while ($row = $result->fetch_assoc()) {
    $users[] = $row;
}

// Outputs as a JSON array
echo json_encode($users);

$conn->close();
?>
