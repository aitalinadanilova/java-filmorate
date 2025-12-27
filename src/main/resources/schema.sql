
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa (
    id INT PRIMARY KEY,
    name VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    mpa_id INT,
    CONSTRAINT fk_mpa FOREIGN KEY (mpa_id) REFERENCES mpa(id)
);

CREATE TABLE IF NOT EXISTS genres (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT,
    genre_id INT,
    PRIMARY KEY(film_id, genre_id),
    CONSTRAINT fk_film FOREIGN KEY (film_id) REFERENCES films(id),
    CONSTRAINT fk_genre FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE IF NOT EXISTS film_likes (
    film_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY(film_id, user_id),
    CONSTRAINT fk_like_film FOREIGN KEY (film_id) REFERENCES films(id),
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS friendships (
    user_id BIGINT,
    friend_id BIGINT,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY(user_id, friend_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_friend FOREIGN KEY (friend_id) REFERENCES users(id)
);