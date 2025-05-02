<?php
//Assistance from ChatGPT

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "ZAQ!1qazXSW@2wsx", "gamr");

if ($_SERVER['REQUEST_METHOD'] !== 'POST'){
    echo json_encode(["status" => "error", "message" => "Only POST allowed"]);
    exit;
}

if (!isset($_POST['swiper_id'], $_POST['swipee_id'])){
    echo json_encode(["status" => "error", "message" => "Missing parameters"]);
    exit;
}

$swiperId = (int)$_POST['swiper_id'];
$swipeeId = (int)$_POST['swipee_id'];

// Checks for mutual "like"
$checkMatch = $conn->prepare("
    SELECT * FROM swipes
    WHERE swiper_id = ? AND swipee_id = ? AND direction = 'like'
    ");

$checkMatch->bind_param("ii", $swipeeId, $swiperId);
$checkMatch->execute();
$result = $checkMatch->get_result();

if ($result->num_rows > 0){
    //mutual like confirmed
    $user1 = min($swiperId, $swipeeId);
    $user2 = max($swiperId, $swipeeId);
    $matchKey = $user1 . "_" . $user2;

    $insertMatch = $conn->prepare("
    INSERT IGNORE INTO matches (user1_id, user2_id, match_key) VALUES (?,?,?)
    ");

$insertMatch->bind_param("iis", $user1, $user2, $matchKey);
$insertMatch->execute();

echo json_encode(["status" => "match"]);
}else {
    echo json_encode(["status" => "no_match"]);
}

$checkMatch->close();
$conn->close();
?>