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
name varchar(25),
age INT,
preferred_playstyle varchar(15),
current_game varchar(30),
bio TEXT,
latitude DECIMAL(9,6),
longitude DECIMAL(9,6)
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

INSERT INTO users (gamertag, name, age, preferred_playstyle, current_game, bio, latitude, longitude)
VALUES
('theRealBatman', 'Gage', 21, 'Casual', 'Arkham Knight', 'Justice for Gotham', 32.5282, -92.6379), -- Ruston, LA
('BananaJoe', 'Joe', 23, 'Competitive', 'Call of Duty: Black Ops 6', 'Need a group.', 32.5053, -92.7064), -- Grambling, LA
('KillerQueen21', 'Eva', 19, 'Competitive', 'Valorant', 'Help me climb the ranks.', 32.5368, -92.0454), -- Monroe, LA
('UnitMan', 'Luke', 27, 'Casual', 'Stardew Valley', 'Let me show you my town.', 32.6178, -93.7473), -- Shreveport, LA
('FireGamer69', 'John', 21, 'Casual', 'Palworld', 'Help me explore the world because I keep dying.', 32.6032, -93.7321), -- Bossier City, LA
('GamingValkyrie', 'Elizabeth', 20, 'Competitive', 'Valorant', 'Looking to help climb the ranks.', 32.4900, -92.4132), -- Farmerville, LA
('GokuMain420', 'Dan', 24, 'Competitive', 'Dragon Ball Fighters', 'Looking for strong opponents to fight.', 32.8120, -93.2830), -- Minden, LA
('SakuraPetal333', 'Mary', 22, 'Casual', 'Garry\'s Mod', 'Looking for someone to make maps with.', 32.7643, -91.8729), -- Bastrop, LA
('CapMerica', 'Ethan', 21, 'Competitive', 'Marvel Rivals', 'I need a group to play with.', 32.6918, -92.6376), -- Winnfield, LA
('PinkPower34512', 'Sarah', 18, 'Competitive', 'Super Smash Bros Ultimate', 'I need help with combos, can someone please teach me.', 32.1555, -93.0995), -- Natchitoches, LA
('GrizzlyGamer', 'Ros', '21', 'Competitive', 'Street Fighter 6', 'Fight me', 32.0593, -93.6991), -- Mansfield, LA
('Dactyl', 'Abby', '19', 'Competitive', 'Overwatch 2', 'Pocket me', 32.7820, -92.1481), -- West Monroe, LA
('C4ndyMAN19', 'Dakota', '22', 'Casual', 'Dead by Daylight', 'Unhook me', 32.2615, -92.7091); -- Jonesboro, LA
-- Confirm users exist
SELECT user_id, gamertag FROM users WHERE user_id IN (1, 5);

-- inserts a swipe from user 1 to user 5 (like)
INSERT INTO swipes (swiper_id, swipee_id, direction)
VALUES (1, 5, 'like')
ON DUPLICATE KEY UPDATE direction = 'like';

-- duplicate insert for manual testing
INSERT INTO swipes(swiper_id, swipee_id, direction)
VALUES(1,5,'like')
ON DUPLICATE KEY UPDATE direction = 'like';

-- Query for mutual likes or matches
SELECT u.* FROM users u
JOIN swipes s1 ON u.user_id = s1.swipee_id
JOIN swipes s2 ON s1.swipee_id = s2.swiper_id AND s1.swiper_id = s2.swipee_id
WHERE s1.direction = 'like' AND s2.direction = 'like';

-- recomendation query (shows users with the same playstyle and hasn't been swipped by user 1
SELECT * FROM users
WHERE user_id != 1
  AND preferred_playstyle = (SELECT preferred_playstyle FROM users WHERE user_id = 1);

ALTER TABLE users ADD COLUMN current_game_genre VARCHAR(30);

-- allows for UPDATE
SET SQL_SAFE_UPDATES = 0;

-- sets games to genres (Can add more)
UPDATE users SET current_game_genre = 'Fighting' WHERE current_game = 'Dragon Ball Fighters';
UPDATE users SET current_game_genre = 'Fighting' WHERE current_game = 'Super Smash Bros Ultimate';
UPDATE users SET current_game_genre = 'FPS' WHERE current_game = 'Valorant';
UPDATE users SET current_game_genre = 'FPS' WHERE current_game = 'Call of Duty: Black Ops 6';
UPDATE users SET current_game_genre = 'MOBA' WHERE current_game = 'Marvel Rivals';
UPDATE users SET current_game_genre = 'Racing' WHERE current_game = 'Mario Kart'; 

-- sets it back for safty
SET SQL_SAFE_UPDATES = 1;

-- adds text bio's, discord, insta, playstyle
ALTER TABLE users
ADD COLUMN discord VARCHAR(255),
ADD COLUMN instagram VARCHAR(255),
ADD COLUMN playing_style VARCHAR(50);
