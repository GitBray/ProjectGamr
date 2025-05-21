<?php
// update_file.php
// Updates a user's profile: text fields (bio, discord, instagram, preferred_playstyle) and/or profile image.

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json");

$target_dir = "uploads/";

// DB connection
$conn = new mysqli("localhost", "root", "", "gamr");
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "DB connection failed"]);
    exit;
}

// Only accept POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $userId = $_POST['user_id'] ?? null;
    if (!$userId) {
        echo json_encode(["status" => "error", "message" => "Missing user_id"]);
        exit;
    }

    $bio = $_POST['bio'] ?? null;
    $discord = $_POST['discord'] ?? null;
    $instagram = $_POST['instagram'] ?? null;
    $preferredPlaystyle = $_POST['playing_style'] ?? null;

    $updated = false;

    // Update profile fields if provided
    if ($bio !== null && $discord !== null && $instagram !== null && $preferredPlaystyle !== null) {
        $stmt = $conn->prepare("UPDATE users SET bio = ?, discord = ?, instagram = ?, preferred_playstyle = ? WHERE user_id = ?");
        $stmt->bind_param("ssssi", $bio, $discord, $instagram, $preferredPlaystyle, $userId);
        if ($stmt->execute()) {
            $updated = true;
        }
        $stmt->close();
    }

    // If an image is uploaded
    if (isset($_FILES['image'])) {
        if (!file_exists($target_dir)) {
            mkdir($target_dir, 0755, true);
        }

        $image = $_FILES['image'];
        $image_name = uniqid("profile_") . "_" . basename($image["name"]);
        $target_file = $target_dir . $image_name;

        if (move_uploaded_file($image["tmp_name"], $target_file)) {
            // Construct full public URL
            $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? "https" : "http";
            $host = $_SERVER['HTTP_HOST'];
            $image_url = "$protocol://$host/$target_file";

            // Delete old image if exists
            $getOld = $conn->prepare("SELECT image_url FROM users WHERE user_id = ?");
            $getOld->bind_param("i", $userId);
            $getOld->execute();
            $result = $getOld->get_result();
            $oldRow = $result->fetch_assoc();
            $getOld->close();

            if (!empty($oldRow['image_url'])) {
                $oldPath = parse_url($oldRow['image_url'], PHP_URL_PATH); // e.g., /uploads/profile_xyz.jpg
                $oldFullPath = $_SERVER['DOCUMENT_ROOT'] . $oldPath;
                if (file_exists($oldFullPath)) {
                    unlink($oldFullPath); // Delete old image
                }
            }

            // Update DB with new image URL
            $stmt = $conn->prepare("UPDATE users SET image_url = ? WHERE user_id = ?");
            $stmt->bind_param("si", $image_url, $userId);
            if ($stmt->execute()) {
                $updated = true;
            }
            $stmt->close();
        } else {
            echo json_encode(["status" => "fail", "message" => "Image upload failed"]);
            exit;
        }
    }

    // Return final result
    if ($updated) {
        echo json_encode(["status" => "success", "message" => "Profile updated"]);
    } else {
        echo json_encode(["status" => "no_change", "message" => "No fields updated"]);
    }
}

$conn->close();
?>
