<?php
// Assistance from ChatGPT

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Database connection
$conn = new mysqli("localhost", "root", "ZAQ!1qazXSW@2wsx", "gamr");

if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

// Grab POST/GET data
$swiperId = $_POST['swiper_id'] ?? $_GET['swiper_id'] ?? null;
$swipeeId = $_POST['swipee_id'] ?? $_GET['swipee_id'] ?? null;
$direction = $_POST['direction'] ?? $_GET['direction'] ?? null;

// Validate required fields
if (!$swiperId || !$swipeeId || !$direction) {
    echo json_encode([
        "success" => false,
        "error" => "Missing swiper_id, swipee_id, or direction"
    ]);
    exit;
}

// Insert or update the swipe
$sql = "INSERT INTO swipes (swiper_id, swipee_id, direction)
        VALUES ($swiperId, $swipeeId, '$direction')
        ON DUPLICATE KEY UPDATE direction = '$direction'";

$response = [];

if ($conn->query($sql) === TRUE) {
    $response["success"] = true;

    // If it's a like, send push notification
    if ($direction === "like") {
        $stmt = $conn->prepare("SELECT device_token FROM users WHERE user_id = ?");
        $stmt->bind_param("i", $swipeeId);
        $stmt->execute();
        $result = $stmt->get_result();
        $row = $result->fetch_assoc();

        if ($row && !empty($row['device_token'])) {
            $deviceToken = $row['device_token'];

            $data = [
                "message" => "Someone liked you!"
            ];

            $post = [
                "to" => $deviceToken,
                "data" => $data
            ];

            $json = json_encode($post);

            $ch = curl_init("https://api.pushy.me/push?api_key=d8964ce3bcd12ac237cd0a668ee8dbdd8f19b8bad398933ced24d548faac5498");
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
            curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            $pushyResponse = curl_exec($ch);
            curl_close($ch);

            $response["pushy_status"] = "sent";
        }
    }
} else {
    $response["success"] = false;
    $response["error"] = $conn->error;
}

$conn->close();
echo json_encode($response);
?>
