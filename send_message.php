<?php
// Assistance from ChatGPT

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Connect to MySQL
$conn = new mysqli("localhost", "root", "ZAQ!1qazXSW@2wsx", "gamr");

if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

// Get POST data
$senderId = $_POST['sender_id'] ?? null;
$recieverId = $_POST['reciever_id'] ?? null;
$message = $_POST['message'] ?? null;

if (!$senderId || !$recieverId || !$message) {
    echo json_encode(["success" => false, "error" => "Missing required fields"]);
    exit;
}

// Insert message into DB
$stmt = $conn->prepare("INSERT INTO messages (sender_id, reciever_id, message) VALUES (?, ?, ?)");
$stmt->bind_param("iis", $senderId, $recieverId, $message);

$response = [];

if ($stmt->execute()) {
    $response["success"] = true;

    // Get receiver's device token
    $stmt = $conn->prepare("SELECT device_token FROM users WHERE user_id = ?");
    $stmt->bind_param("i", $recieverId);
    $stmt->execute();
    $result = $stmt->get_result();
    $row = $result->fetch_assoc();

    if ($row && !empty($row['device_token'])) {
        $deviceToken = $row['device_token'];

        // Prepare Pushy payload
        $data = [
            "message" => "New message received!"
        ];

        $post = [
            "to" => $deviceToken,
            "data" => $data
        ];

        $json = json_encode($post);

        // Send Pushy notification
        $ch = curl_init("https://api.pushy.me/push?api_key=d8964ce3bcd12ac237cd0a668ee8dbdd8f19b8bad398933ced24d548faac5498");
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
        curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $pushyResponse = curl_exec($ch);
        curl_close($ch);

        $response["pushy_status"] = "sent";
    }
} else {
    $response["success"] = false;
    $response["error"] = $stmt->error;
}

$conn->close();
echo json_encode($response);
?>
