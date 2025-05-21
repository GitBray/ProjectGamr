CREATE DATABASE IF NOT EXISTS Gamr;
USE Gamr;

-- Drop tables if needed (optional safety reset)
DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS swipes;
DROP TABLE IF EXISTS region;
DROP TABLE IF EXISTS games_played;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    gamertag varchar(50) UNIQUE NOT NULL,
    username varchar(50) unique NOT NULL,
    password varchar(50) NOT NULL,
    name varchar(25),
    age INT,
    preferred_playstyle varchar(15),
    current_game varchar(30),
    bio TEXT,
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    -- Sarun add fields below
    current_game_genre VARCHAR(30),
    discord VARCHAR(255),
    instagram VARCHAR(255),
    playing_style VARCHAR(50),
    image_url VARCHAR(255) DEFAULT NULL
);

CREATE TABLE region(
    user_id INT,
    continent varchar(12),
    country varchar(12),
    city varchar(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE games_played (
    user_id INT,
    game_name varchar(30),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE swipes(
    swiper_id int,
    swipee_id INT,
    direction ENUM('like', 'dislike'),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (swiper_id, swipee_id),
    FOREIGN KEY (swiper_id) REFERENCES users(user_id),
    FOREIGN KEY (swipee_id) REFERENCES users(user_id)
);

CREATE TABLE matches (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    user1_id INT NOT NULL,
    user2_id INT NOT NULL,
    match_key VARCHAR(20) NOT NULL UNIQUE,  -- New field to prevent duplicates
    match_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(user_id),
    FOREIGN KEY (user2_id) REFERENCES users(user_id)
);

CREATE TABLE messages(
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    reciever_id INT NOT NULL,
    message TEXT NOT NULL,
    TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(user_id),
    FOREIGN KEY (reciever_id) REFERENCES users(user_id)
);

-- Insert users
INSERT INTO users (username, password, gamertag, name, age, preferred_playstyle, current_game, bio, latitude, longitude)
VALUES
('theRealBatman', 'password', 'theRealBatman', 'Gage', 21, '', 'Arkham Knight', '', 32.5282, -92.6379),
('BananaJoe', 'password', 'BananaJoe', 'Joe', 23, 'Competitive', 'Call of Duty: Black Ops 6', 'Need a group.', 32.5053, -92.7064),
('KillerQueen21', 'password', 'KillerQueen21', 'Eva', 19, 'Competitive', 'Valorant', 'Help me climb the ranks.', 32.5368, -92.0454),
('UnitMan', 'password', 'UnitMan', 'Luke', 27, 'Casual', 'Stardew Valley', 'Let me show you my town.', 32.6178, -93.7473),
('FireGamer69', 'password', 'FireGamer69', 'John', 21, 'Casual', 'Palworld', 'Help me explore the world because I keep dying.', 32.6032, -93.7321),
('GamingValkyrie', 'password', 'GamingValkyrie', 'Elizabeth', 20, 'Competitive', 'Valorant', 'Looking to help climb the ranks.', 32.4900, -92.4132),
('GokuMain420', 'password', 'GokuMain420', 'Dan', 24, 'Competitive', 'Dragon Ball Fighters', 'Looking for strong opponents to fight.', 32.8120, -93.2830),
('SakuraPetal333', 'password', 'SakuraPetal333', 'Mary', 22, 'Casual', 'Garry\'s Mod', 'Looking for someone to make maps with.', 32.7643, -91.8729),
('CapMerica', 'password', 'CapMerica', 'Ethan', 21, 'Competitive', 'Marvel Rivals', 'I need a group to play with.', 32.6918, -92.6376),
('PinkPower34512', 'password', 'PinkPower34512', 'Sarah', 18, 'Competitive', 'Super Smash Bros Ultimate', 'I need help with combos, can someone please teach me.', 32.1555, -93.0995),
('GrizzlyGamer', 'password', 'GrizzlyGamer', 'Ros', '21', 'Competitive', 'Street Fighter 6', 'Fight me', 32.0593, -93.6991),
('Dactyl', 'password', 'Dactyl', 'Abby', '19', 'Competitive', 'Overwatch 2', 'Pocket me', 32.7820, -92.1481),
('C4ndyMAN19', 'password', 'C4ndyMAN19', 'Dakota', '22', 'Casual', 'Dead by Daylight', 'Unhook me', 32.2615, -92.7091);

-- Confirm users exist
SELECT user_id, gamertag FROM users WHERE user_id IN (1, 5);

-- Test swipe (user 1 likes user 5)
INSERT INTO swipes (swiper_id, swipee_id, direction)
VALUES (1, 5, 'like')
ON DUPLICATE KEY UPDATE direction = 'like';

-- duplicate insert
INSERT INTO swipes(swiper_id, swipee_id, direction)
VALUES(1,5,'like')
ON DUPLICATE KEY UPDATE direction = 'like';

-- Query for mutual likes
SELECT u.* FROM users u
JOIN swipes s1 ON u.user_id = s1.swipee_id
JOIN swipes s2 ON s1.swipee_id = s2.swiper_id AND s1.swiper_id = s2.swipee_id
WHERE s1.direction = 'like' AND s2.direction = 'like';

-- Recommendation query
SELECT * FROM users
WHERE user_id != 1
  AND preferred_playstyle = (SELECT preferred_playstyle FROM users WHERE user_id = 1);

-- allows for UPDATE
SET SQL_SAFE_UPDATES = 0;

-- Game genre classification (non-destructive)
UPDATE users SET current_game_genre = 'Fighting' WHERE current_game IN ('Dragon Ball Fighters', 'Super Smash Bros Ultimate', 'Street Fighter 6');
UPDATE users SET current_game_genre = 'FPS' WHERE current_game IN ('Valorant', 'Call of Duty: Black Ops 6', 'Overwatch 2');
UPDATE users SET current_game_genre = 'MOBA' WHERE current_game = 'Marvel Rivals';
UPDATE users SET current_game_genre = 'Racing' WHERE current_game = 'Mario Kart';
UPDATE users SET current_game_genre = 'Sandbox' WHERE current_game = 'Garry\'s Mod';
UPDATE users SET current_game_genre = 'RPG' WHERE current_game = 'Palworld';
UPDATE users SET current_game_genre = 'Simulation' WHERE current_game = 'Stardew Valley';
UPDATE users SET current_game_genre = 'Horror' WHERE current_game = 'Dead by Daylight';

-- Restore safe mode
SET SQL_SAFE_UPDATES = 1;

-- Reset test data
DELETE FROM matches;
DELETE FROM swipes;
