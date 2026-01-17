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
            stmt.setLong(5, film.getMpa().getId());
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
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        jdbcTemplate.update(
                "DELETE FROM films_genre WHERE film_id = ?",
                film.getId()
        );

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(
                        "INSERT INTO films_genre (film_id, genre_id) VALUES (?, ?)",
                        film.getId(),
                        genre.getId()
                );
            }
        }

        return film;
    }


    @Override
    public Film getFilm(Long filmId) {
        List<Film> films = jdbcTemplate.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                        "mr.id AS mpa_id, mr.name AS mpa_name " +
                        "FROM films f " +
                        "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                        "WHERE f.id = ?",
                mapper,
                filmId
        );

        if (films.isEmpty()) {
            return null;
        }

        Film film = films.get(0);

        film.setGenres(getGenresByFilmId(filmId));
        film.setLikes(getLikesByFilmId(filmId));

        return film;
    }


    @Override
    public List<Film> getAllFilms() {
        List<Film> films = jdbcTemplate.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                        "mr.id AS mpa_id, mr.name AS mpa_name " +
                        "FROM films f " +
                        "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id",
                mapper
        );

        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setLikes(getLikesByFilmId(film.getId()));
        }

        return films;
    }

    private List<Genre> getGenresByFilmId(Long filmId) {
        return jdbcTemplate.query(
                "SELECT g.id, g.name FROM genres g " +
                        "JOIN films_genre fg ON g.id = fg.genre_id " +
                        "WHERE fg.film_id = ?",
                (rs, rowNum) -> Genre.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build(),
                filmId
        );
    }

    private List<Long> getLikesByFilmId(Long filmId) {
        return jdbcTemplate.queryForList(
                "SELECT user_id FROM likes WHERE film_id = ?",
                Long.class,
                filmId
        );
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
            return jdbcTemplate.query("SELECT * FROM users WHERE id IN (SELECT user_id FROM likes WHERE film_id = ?)", new DataClassRowMapper<>(User.class), filmId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Film> getPopularFilms(Long count, Long genreId, Integer year) {
        // Базовая часть запроса. Постоянна
        StringBuilder sql = new StringBuilder(
                "SELECT f.ID, f.NAME, COUNT(l.USER_ID) as cnt_like " +
                        "FROM FILMS f " +
                        "LEFT JOIN likes l ON l.film_id = f.id "
        );

        // Список параметров
        List<Object> params = new ArrayList<>();

        // Условия для фильтрации WHERE
        List<String> conditions = new ArrayList<>();

        // Если жанр передан - добавляется JOIN и условие фильтрации
        if (genreId != null) {
            sql.append("JOIN films_genre fg ON f.id = fg.film_id ");
            conditions.add("fg.genre_id = ?");
            params.add(genreId);
        }

        // Если год передан - добавляется JOIN и условие фильтрации
        if (year != null) {
            conditions.add("EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }

        // Если хотя бы один фильтр передан - добавляется WHERE
        if (!conditions.isEmpty()) {
            sql.append("WHERE ")
                    .append(String.join(" AND ", conditions))
                    .append(" ");
        }

        // Добавляется группировка и сортировка по количеству лайков + ограничение кол-ва
        sql.append(
                "GROUP BY f.ID, f.NAME " +
                        "ORDER BY cnt_like DESC " +
                        "LIMIT ? "
        );

        // LIMIT - последний параметр
        params.add(count);


        return jdbcTemplate.query(
                sql.toString(),
                new DataClassRowMapper<>(Film.class),
                params.toArray());
    }

    @Override
    public boolean checkLikeOnFilm(Long filmId, Long userId) {
        if ((jdbcTemplate.query("SELECT user_id FROM likes WHERE film_id = ? AND user_id = ?", new ColumnMapRowMapper(), filmId, userId)).contains(userId)) {
            throw new ValidationException("Пользователь с id = " + userId + " уже поставил лайк");
        }
        return true;
    }

}
