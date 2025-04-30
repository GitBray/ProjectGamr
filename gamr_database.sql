CREATE DATABASE IF NOT EXISTS Gamr;
USE Gamr;

-- Drop tables if needed (optional safety reset)
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
bio TEXT
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
-- inserts "users" into the database
INSERT INTO users (gamertag, name, age, preferred_playstyle, current_game, bio)
VALUES
('theRealBatman', 'Gage', 21, 'Casual', 'Arkham Knight', 'Justice for Gotham'),
('BananaJoe', 'Joe', 23, 'Competitive', 'Call of Duty: Black Ops 6', 'Need a group.'),
('KillerQueen21', 'Eva', 19, 'Competitive', 'Valorant', 'Help me climb the ranks.'),
('UnitMan', 'Luke', 27, 'Casual', 'Stardew Valley', 'Let me show you my town.'),
('FireGamer69', 'John', 21, 'Casual', 'Palworld', 'Help me explore the world because I keep dying.'),
('GamingValkyrie', 'Elizabeth', 20, 'Competitive', 'Valorant', 'Looking to help climb the ranks.'),
('GokuMain420', 'Dan', 24, 'Competitive', 'Dragon Ball Fighters', 'Looking for strong opponents to fight.'),
('SakuraPetal333', 'Mary', 22, 'Casual', 'Garry\'s Mod', 'Looking for someone to make maps with.'),
('CapMerica', 'Ethan', 21, 'Competitive', 'Marvel Rivals', 'I need a group to play with.'),
('PinkPower34512', 'Sarah', 18, 'Competitive', 'Super Smash Bros Ultimate', 'I need help with combos, can someone please teach me.');

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
