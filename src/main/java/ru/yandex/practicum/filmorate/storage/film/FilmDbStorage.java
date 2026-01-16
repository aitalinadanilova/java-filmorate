package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;

    @Override
    public Film createFilm(Film film) {
        log.info("Запрос на создание фильма: {}", film.getName());
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        updateGenres(film);
        updateDirectorsForFilm(film);
        return getFilm(film.getId());
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Запрос на обновление фильма с id={}", film.getId());
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_mpa_id = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql, film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa().getId(), film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        updateGenres(film);
        updateDirectorsForFilm(film);
        return getFilm(film.getId());
    }

    @Override
    public Film getFilm(Long filmId) {
        String sql = "SELECT f.*, mr.name AS mpa_name FROM films f " +
                "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, mapper, filmId);
            if (film != null) loadDataForFilms(List.of(film));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, mr.name AS mpa_name FROM films f " +
                "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id";
        List<Film> films = jdbcTemplate.query(sql, mapper);
        loadDataForFilms(films);
        return films;
    }

    @Override
    public List<Film> getPopularFilms(Long count) {
        String sql = "SELECT f.*, mr.name AS mpa_name FROM films f " +
                "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id, mr.name ORDER BY COUNT(l.user_id) DESC LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, mapper, count);
        loadDataForFilms(films);
        return films;
    }

    @Override
    public List<Film> findSortFilmsByDirector(Long directorId, String sortBy) {
        findDirectorById(directorId);
        String sql;
        if ("likes".equalsIgnoreCase(sortBy)) {
            sql = "SELECT f.*, mr.name AS mpa_name FROM films f " +
                    "JOIN film_director fd ON f.id = fd.film_id " +
                    "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "WHERE fd.director_id = ? GROUP BY f.id, mr.name ORDER BY COUNT(l.user_id) DESC";
        } else {
            sql = "SELECT f.*, mr.name AS mpa_name FROM films f " +
                    "JOIN film_director fd ON f.id = fd.film_id " +
                    "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                    "WHERE fd.director_id = ? ORDER BY f.release_date ASC";
        }
        List<Film> films = jdbcTemplate.query(sql, mapper, directorId);
        loadDataForFilms(films);
        return films;
    }

    private void loadDataForFilms(List<Film> films) {
        if (films == null || films.isEmpty()) return;
        Map<Long, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, f -> f));
        String ids = filmMap.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));

        // Жанры
        jdbcTemplate.query("SELECT fg.film_id, g.id, g.name FROM genres g " +
                "JOIN films_genre fg ON g.id = fg.genre_id WHERE fg.film_id IN (" + ids + ")", rs -> {
            Film f = filmMap.get(rs.getLong("film_id"));
            if (f != null) f.getGenres().add(Genre.builder().id(rs.getLong("id")).name(rs.getString("name")).build());
        });

        // Режиссеры
        jdbcTemplate.query("SELECT fd.film_id, d.director_id, d.director_name FROM directors d " +
                "JOIN film_director fd ON d.director_id = fd.director_id WHERE fd.film_id IN (" + ids + ")", rs -> {
            Film f = filmMap.get(rs.getLong("film_id"));
            if (f != null) {
                if (f.getDirectors() == null) f.setDirectors(new ArrayList<>());
                f.getDirectors().add(Director.builder()
                        .id(rs.getLong("director_id"))
                        .name(rs.getString("director_name")).build());
            }
        });

        // Лайки
        jdbcTemplate.query("SELECT film_id, user_id FROM likes WHERE film_id IN (" + ids + ")", rs -> {
            Film f = filmMap.get(rs.getLong("film_id"));
            if (f != null) f.getLikes().add(rs.getLong("user_id"));
        });
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM films_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Genre> genres = new ArrayList<>(new LinkedHashSet<>(film.getGenres()));
            jdbcTemplate.batchUpdate("INSERT INTO films_genre (film_id, genre_id) VALUES (?, ?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, film.getId());
                            ps.setLong(2, genres.get(i).getId());
                        }

                        public int getBatchSize() {
                            return genres.size();
                        }
                    });
        }
    }

    private void updateDirectorsForFilm(Film film) {
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?", film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            List<Director> directors = new ArrayList<>(new LinkedHashSet<>(film.getDirectors()));
            jdbcTemplate.batchUpdate("INSERT INTO film_director (film_id, director_id) VALUES (?, ?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, film.getId());
                            ps.setLong(2, directors.get(i).getId());
                        }

                        public int getBatchSize() {
                            return directors.size();
                        }
                    });
        }
    }

    @Override
    public List<Director> findDirectors() {
        return jdbcTemplate.query("SELECT * FROM directors", (rs, n) ->
                Director.builder().id(rs.getLong("director_id")).name(rs.getString("director_name")).build());
    }

    @Override
    public Director findDirectorById(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM directors WHERE director_id = ?",
                    (rs, n) -> Director.builder().id(rs.getLong("director_id")).name(rs.getString("director_name")).build(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссёр не найден");
        }
    }

    @Override
    public Director createDirector(Director director) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO directors (director_name) VALUES (?)", new String[]{"director_id"});
            ps.setString(1, director.getName());
            return ps;
        }, kh);
        director.setId(kh.getKey().longValue());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        int rows = jdbcTemplate.update("UPDATE directors SET director_name = ? WHERE director_id = ?",
                director.getName(), director.getId());
        if (rows == 0) throw new NotFoundException("Режиссёр не найден");
        return director;
    }

    @Override
    public boolean deleteDirectorById(Long id) {
        return jdbcTemplate.update("DELETE FROM directors WHERE director_id = ?", id) > 0;
    }

    @Override
    public void addLike(Long id, Long userId) {
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", id, userId);
    }

    @Override
    public void removeLike(Long id, Long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", id, userId);
    }

    @Override
    public List<User> getLikes(Long filmId) {
        return jdbcTemplate.query("SELECT u.* FROM users u JOIN likes l ON u.id = l.user_id WHERE l.film_id = ?",
                (rs, rowNum) -> User.builder().id(rs.getLong("id")).email(rs.getString("email")).build(), filmId);
    }
}