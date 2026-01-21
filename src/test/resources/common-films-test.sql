DELETE FROM likes;
DELETE FROM films_genre;
DELETE FROM film_director;
DELETE FROM films;
DELETE FROM users;
DELETE FROM directors;

ALTER TABLE films ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE directors ALTER COLUMN id RESTART WITH 1;

INSERT INTO users (email, login, name, birthday) VALUES
('user1@test.ru', 'user1', 'User One', '1990-01-01'),
('user2@test.ru', 'user2', 'User Two', '1992-02-02'),
('user3@test.ru', 'user3', 'User Three', '1994-03-03');

INSERT INTO films (name, description, release_date, duration, rating_mpa_id) VALUES
('Film 1', 'Description 1', '2020-01-01', 120, 1),
('Film 2', 'Description 2', '2021-01-01', 90, 1),
('Film 3', 'Description 3', '2022-01-01', 150, 1),
('Film 4', 'Description 4', '2023-01-01', 110, 1);

-- Добавление лайков
-- User1 лайкнул фильмы 1, 2, 3
INSERT INTO likes (film_id, user_id) VALUES (1, 1), (2, 1), (3, 1);
-- User2 лайкнул фильмы 2, 3, 4
INSERT INTO likes (film_id, user_id) VALUES (2, 2), (3, 2), (4, 2);
-- User3 лайкнул фильмы 1, 4
INSERT INTO likes (film_id, user_id) VALUES (1, 3), (4, 3);