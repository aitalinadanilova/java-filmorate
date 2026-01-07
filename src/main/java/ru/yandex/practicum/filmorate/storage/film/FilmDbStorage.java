package ru.yandex.practicum.filmorate.storage.film;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@AllArgsConstructor
@Component
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;

    @Override
    public Film createFilm(Film film) {
        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, rating_mpa_id)values (?, ?, ?, ? ,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5,film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        if (film.getGenres() != null) {
            Set<Genre> genres = new LinkedHashSet<>(film.getGenres());
            for (Genre genre : genres) {
                jdbcTemplate.update("INSERT INTO films_genre (film_id, genre_id)values(?,?)", film.getId(), genre.getId());
            }
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlQuery =
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(
                sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        return film;
    }

    @Override
    public Film getFilm(Long filmId) {
        List<Film> films = jdbcTemplate.query("SELECT f.id, " +
                "f.name, " +
                "f.description, " +
                "f.release_date, " +
                "f.duration, " +
                "l.USER_ID AS like_id, " +
                "mr.id AS mpa_id, " +
                "mr.name AS mpa_name, " +
                "g.id AS genre_id , " +
                "g.name AS genre_name " +
                "FROM films AS f " +
                "LEFT JOIN LIKES AS l ON (f.ID = l.FILM_ID) " +
                "LEFT JOIN RATING_MPA AS mr ON (f.RATING_MPA_ID  = mr.ID) " +
                "LEFT JOIN FILMS_GENRE AS fg ON (f.ID  = fg.film_id) " +
                "LEFT JOIN GENRES AS g ON (fg.genre_id = g.ID)" +
                "WHERE F.ID = ?;", mapper, filmId);
        if (films.size() == 0) {
            return null;
        }
        return films.get(0);
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = jdbcTemplate.query("SELECT f.id, " +
                "f.name, " +
                "f.description, " +
                "f.release_date, " +
                "f.duration, " +
                "l.USER_ID AS like_id, " +
                "mr.id AS mpa_id, " +
                "mr.name AS mpa_name, " +
                "g.id AS genre_id , " +
                "g.name AS genre_name " +
                "FROM films AS f " +
                "LEFT JOIN LIKES AS l ON (f.ID = l.FILM_ID) " +
                "LEFT JOIN RATING_MPA AS mr ON (f.RATING_MPA_ID  = mr.ID) " +
                "LEFT JOIN FILMS_GENRE AS fg ON (f.ID  = fg.film_id) " +
                "LEFT JOIN GENRES AS g ON (fg.genre_id = g.ID);", mapper);
        Set<Film> uniqueFilms = new TreeSet<>(Comparator.comparing(Film::getId));
        uniqueFilms.addAll(films);
        return new ArrayList<>(uniqueFilms);
    }

    @Override
    public void addLike(Long id, Long userId) {
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id)values (?, ?);", id, userId);
    }

    @Override
    public void removeLike(Long id, Long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?;", id, userId);

    }

    @Override
    public List<User> getLikes(Long filmId) {
        try {
            return jdbcTemplate.query("SELECT * FROM users WHERE id IN (SELECT user_id FROM likes WHERE film_id = ?)",new DataClassRowMapper<>(User.class), filmId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Film> getPopularFilms(Long count) {
        return jdbcTemplate.query(
                "SELECT ID, NAME, cnt_like " +
                        "FROM PUBLIC.FILMS f " +
                        "LEFT JOIN (select FILM_ID, COUNT(user_id) cnt_like from likes group by FILM_ID) l ON (f.id = l.FILM_ID) " +
                        "ORDER BY l.cnt_like DESC " +
                        "LIMIT ?", new DataClassRowMapper<>(Film.class), count);
    }

    @Override
    public boolean checkLikeOnFilm(Long filmId, Long userId) {
        if ((jdbcTemplate.query("SELECT user_id FROM likes WHERE film_id = ? AND user_id = ?", new ColumnMapRowMapper(), filmId, userId)).contains(userId)) {
            throw new ValidationException("Пользователь с id = " + userId + " уже поставил лайк");
        }
        return true;
    }

}
