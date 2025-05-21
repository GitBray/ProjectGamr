<?php
// Assistance from ChatGPT
header('Content-Type: application/json');
error_reporting(0);
ini_set('display_errors', 0); // removing error reporting fixed an issue I had with json reading the first output (Bray)
                              // keeping the code here but I'll test more soon and probably remove this.

$conn = new mysqli("localhost", "root", "", "gamr");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Connection failed"]);
    exit;
}

// Validate input
if (!isset($_POST['swiper_id'], $_POST['swipee_id'], $_POST['direction'])) {
    echo json_encode(["success" => false, "message" => "Missing required fields"]);
    exit;
}

$swiper_id = intval($_POST['swiper_id']);
$swipee_id = intval($_POST['swipee_id']);
$direction = $_POST['direction'];

$response = ["success" => true, "message" => "Swipe recorded"];

error_log("Swipe received: $swiper_id -> $swipee_id ($direction)");

// Insert or update swipe
$sql = "INSERT INTO swipes (swiper_id, swipee_id, direction) VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE direction = VALUES(direction)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("iis", $swiper_id, $swipee_id, $direction);

if (!$stmt->execute()) {
    error_log("Failed to insert swipe");
    echo json_encode(["success" => false, "message" => "Failed to record swipe"]);
    exit;
}

error_log("Swipe saved");

// Match checking is now handled in submit_swipe rather than another file!

// Only check for match if direction is 'like'
if ($direction === 'like') {
    error_log("Checking for reverse like...");

    $check_sql = "SELECT * FROM swipes 
                  WHERE swiper_id = ? AND swipee_id = ? AND direction = 'like'";
    $check_stmt = $conn->prepare($check_sql);
    $check_stmt->bind_param("ii", $swipee_id, $swiper_id);
    $check_stmt->execute();
    $result = $check_stmt->get_result();

    if ($result->num_rows > 0) {
        error_log("Mutual like detected");

        // Have a match key of both userids (lower value first)
        $min = min($swiper_id, $swipee_id);
        $max = max($swiper_id, $swipee_id);
        $match_key = "{$min}_{$max}";

        // Check if the match already exists
        $match_check = $conn->prepare("SELECT match_id FROM matches WHERE match_key = ?");
        $match_check->bind_param("s", $match_key);
        $match_check->execute();
        $match_check_result = $match_check->get_result();

        if ($match_check_result->num_rows === 0) {

            // Create the match
            $insert_match = $conn->prepare("INSERT INTO matches (user1_id, user2_id, match_key) VALUES (?, ?, ?)");
            $insert_match->bind_param("iis", $swiper_id, $swipee_id, $match_key);
            if ($insert_match->execute()) {
                error_log("Match created for $match_key");
                $response["message"] = "Match created";
            } else {
                error_log("Failed to create match for $match_key");
            }
        } else {
            error_log("Match already exists: $match_key");
        }
    }
}

echo json_encode($response);
?>
