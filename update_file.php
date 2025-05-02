<?php
// Assistance from ChatGPT

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json");

$target_dir = "uploads/";

// DB connection
$conn = new mysqli("localhost", "root", "ZAQ!1qazXSW@2wsx", "gamr");
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "DB connection failed"]));
}

// Only accept POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $userId = $_POST['user_id'] ?? null;
    if (!$userId) {
        echo json_encode(["status" => "error", "message" => "Missing user_id"]);
        exit;
    }

    // Update profile fields if present
    $bio = $_POST['bio'] ?? null;
    $discord = $_POST['discord'] ?? null;
    $instagram = $_POST['instagram'] ?? null;
    $playingStyle = $_POST['playing_style'] ?? null;

    if ($bio !== null && $discord !== null && $instagram !== null && $playingStyle !== null) {
        $stmt = $conn->prepare("UPDATE users SET bio=?, discord=?, instagram=?, playing_style=? WHERE user_id=?");
        $stmt->bind_param("ssssi", $bio, $discord, $instagram, $playingStyle, $userId);
        $stmt->execute();
        $stmt->close();
    }

    // If image is uploaded
    if (isset($_FILES['image'])) {
        if (!file_exists($target_dir)) {
            mkdir($target_dir, 0755, true);
        }

        $image = $_FILES['image'];
        $image_name = uniqid("profile_") . "_" . basename($image["name"]);
        $target_file = $target_dir . $image_name;

        // Move new file
        if (move_uploaded_file($image["tmp_name"], $target_file)) {
            // Build public URL
            $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? "https" : "http";
            $host = $_SERVER['HTTP_HOST'];
            $image_url = "$protocol://$host/$target_file";

            // Fetch old image to delete
            $getOld = $conn->prepare("SELECT image_url FROM users WHERE user_id = ?");
            $getOld->bind_param("i", $userId);
            $getOld->execute();
            $result = $getOld->get_result();
            $oldRow = $result->fetch_assoc();
            $getOld->close();

            if (!empty($oldRow['image_url'])) {
                $oldPath = parse_url($oldRow['image_url'], PHP_URL_PATH); // gets "/uploads/filename.jpg"
                $oldFullPath = $_SERVER['DOCUMENT_ROOT'] . $oldPath;
                if (file_exists($oldFullPath)) {
                    unlink($oldFullPath); // delete old image file
                }
            }

            // Save new image URL
            $stmt = $conn->prepare("UPDATE users SET image_url = ? WHERE user_id = ?");
            $stmt->bind_param("si", $image_url, $userId);
            $stmt->execute();
            $stmt->close();

            echo json_encode(["status" => "success", "message" => "Profile updated", "image_url" => $image_url]);
        } else {
            echo json_encode(["status" => "fail", "message" => "Image upload failed"]);
        }
    } else {
        echo json_encode(["status" => "success", "message" => "Profile updated (no image uploaded)"]);
    }
}

$conn->close();
?>
