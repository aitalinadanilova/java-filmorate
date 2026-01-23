package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.DataClassRowMapper;
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
        log.info("Добавление нового фильма: {}", film.getName());
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
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
        log.info("Обновление фильма с id={}", film.getId());
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                "rating_mpa_id = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql, film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(),
                film.getMpa().getId(), film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        updateGenres(film);
        updateDirectorsForFilm(film);
        return getFilm(film.getId());
    }

    @Override
    public Film getFilm(Long filmId) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.rating_mpa_id AS mpa_id, mr.name AS mpa_name " +
                "FROM films f " +
                "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                "WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, mapper, filmId);
            if (film != null) {
                loadDataForFilms(List.of(film));
            }
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.rating_mpa_id AS mpa_id, mr.name AS mpa_name " +
                "FROM films f " +
                "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id";
        List<Film> films = jdbcTemplate.query(sql, mapper);
        loadDataForFilms(films);
        return films;
    }

    @Override
    public List<Film> findSortFilmsByDirector(Long directorId, String sortBy) {
        findDirectorById(directorId);
        String sql;
        if ("likes".equalsIgnoreCase(sortBy)) {
            sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                    "f.rating_mpa_id AS mpa_id, mr.name AS mpa_name " +
                    "FROM films f " +
                    "JOIN film_director fd ON f.id = fd.film_id " +
                    "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.id, mr.name " +
                    "ORDER BY COUNT(l.user_id) DESC";
        } else {
            sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                    "f.rating_mpa_id AS mpa_id, mr.name AS mpa_name " +
                    "FROM films f " +
                    "JOIN film_director fd ON f.id = fd.film_id " +
                    "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date ASC";
        }
        List<Film> films = jdbcTemplate.query(sql, mapper, directorId);
        loadDataForFilms(films);
        return films;
    }

    private void loadDataForFilms(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }
        Map<Long, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, f -> f));
        String ids = filmMap.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));

        // Жанры
        jdbcTemplate.query("SELECT fg.film_id, g.id, g.name FROM genres g " +
                "JOIN films_genre fg ON g.id = fg.genre_id WHERE fg.film_id IN (" + ids + ")", rs -> {
            Film f = filmMap.get(rs.getLong("film_id"));
            if (f != null) {
                f.getGenres().add(Genre.builder().id(rs.getLong("id")).name(rs.getString("name")).build());
            }
        });

        // Режиссеры
        jdbcTemplate.query("SELECT fd.film_id, d.id, d.name FROM directors d " +
                "JOIN film_director fd ON d.id = fd.director_id WHERE fd.film_id IN (" + ids + ")", rs -> {
            Film f = filmMap.get(rs.getLong("film_id"));
            if (f != null) {
                if (f.getDirectors() == null) {
                    f.setDirectors(new ArrayList<>());
                }
                f.getDirectors().add(Director.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name")).build());
            }
        });

        // Лайки
        jdbcTemplate.query("SELECT film_id, user_id FROM likes WHERE film_id IN (" + ids + ")", rs -> {
            Film f = filmMap.get(rs.getLong("film_id"));
            if (f != null) {
                f.getLikes().add(rs.getLong("user_id"));
            }
        });
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM films_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Genre> genres = new ArrayList<>(new LinkedHashSet<>(film.getGenres()));
            jdbcTemplate.batchUpdate("INSERT INTO films_genre (film_id, genre_id) VALUES (?, ?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, film.getId());
                            ps.setLong(2, genres.get(i).getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return genres.size();
                        }
                    });
        }
    }

    private void updateDirectorsForFilm(Film film) {
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?", film.getId());

        if (film.getDirectors() == null) {
            return;
        }

        List<Director> directors = new ArrayList<>(new LinkedHashSet<>(film.getDirectors()));
        jdbcTemplate.batchUpdate("INSERT INTO film_director (film_id, director_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, directors.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return directors.size();
                    }
                });
    }

    @Override
    public List<Director> findDirectors() {
        return jdbcTemplate.query("SELECT * FROM directors", (rs, n) ->
                Director.builder().id(rs.getLong("id")).name(rs.getString("name")).build());
    }

    @Override
    public Director findDirectorById(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM directors WHERE id = ?",
                    (rs, n) -> Director.builder().id(rs.getLong("id")).name(rs.getString("name")).build(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссёр с id=" + id + " не найден");
        }
    }

    @Override
    public Director createDirector(Director director) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO directors (name) VALUES (?)",
                    new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, kh);
        director.setId(kh.getKey().longValue());
        return director;
    }

    @Override
    public List<Film> getPopularFilms(Long count, Long genreId, Integer year) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                        "f.rating_mpa_id AS mpa_id, mr.name AS mpa_name " +
                        "FROM films f " +
                        "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                        "LEFT JOIN likes l ON f.id = l.film_id "
        );

        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (genreId != null) {
            sql.append("JOIN films_genre fg ON f.id = fg.film_id ");
            conditions.add("fg.genre_id = ?");
            params.add(genreId);
        }

        if (year != null) {
            conditions.add("EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }

        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }

        sql.append("GROUP BY f.id, mr.name ORDER BY COUNT(l.user_id) DESC LIMIT ?");
        params.add(count);

        List<Film> films = jdbcTemplate.query(sql.toString(), mapper, params.toArray());

        loadDataForFilms(films);

        return films;
    }


    @Override
    public Director updateDirector(Director director) {
        int rows = jdbcTemplate.update("UPDATE directors SET name = ? WHERE id = ?",
                director.getName(), director.getId());
        if (rows == 0) {
            throw new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
        }
        return director;
    }

    @Override
    public boolean deleteDirectorById(Long id) {
        return jdbcTemplate.update("DELETE FROM directors WHERE id = ?", id) > 0;
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
        String sql = "SELECT u.* FROM users u JOIN likes l ON u.id = l.user_id WHERE l.film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .build(), filmId);
    }

    @Override
    public List<Film> getFilmsByIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inClause = String.join(",", Collections.nCopies(filmIds.size(), "?"));

        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "mr.id AS mpa_id, mr.name AS mpa_name " +
                "FROM films f " +
                "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                "WHERE f.id IN (" + inClause + ")";

        List<Film> films = jdbcTemplate.query(sql, mapper, filmIds.toArray());

        loadDataForFilms(films);

        return films;
    }

    @Override
    public List<Film> searchFilms(String query, boolean byTitle, boolean byDirector) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                        "mr.id AS mpa_id, mr.name AS mpa_name " +
                        "FROM films f " +
                        "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                        "LEFT JOIN film_director fd ON f.id = fd.film_id " +
                        "LEFT JOIN directors d ON fd.director_id = d.id " +
                        "LEFT JOIN likes l ON f.id = l.film_id " +
                        "WHERE "
        );

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String searchPattern = "%" + query.toLowerCase() + "%";

        if (byTitle) {
            conditions.add("LOWER(f.name) LIKE ?");
            params.add(searchPattern);
        }
        if (byDirector) {
            conditions.add("LOWER(d.name) LIKE ?");
            params.add(searchPattern);
        }

        if (conditions.isEmpty()) return Collections.emptyList();

        sql.append(String.join(" OR ", conditions));
        sql.append(" GROUP BY f.id, mr.name ORDER BY COUNT(l.user_id) DESC");

        List<Film> films = jdbcTemplate.query(sql.toString(), mapper, params.toArray());
        loadDataForFilms(films);
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        log.info("Получение общих фильмов для пользователей {} и {}", userId, friendId);

        // SQL запрос для получения общих фильмов, отсортированных по популярности (количеству лайков)
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.rating_mpa_id AS mpa_id, mr.name AS mpa_name, " +
                "COUNT(l.user_id) AS like_count " +
                "FROM films f " +
                "JOIN rating_mpa mr ON f.rating_mpa_id = mr.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "WHERE f.id IN (" +
                "    SELECT l1.film_id FROM likes l1 WHERE l1.user_id = ? " +
                "    INTERSECT " +
                "    SELECT l2.film_id FROM likes l2 WHERE l2.user_id = ? " +
                ") " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.rating_mpa_id, mr.name " +
                "ORDER BY like_count DESC";

        try {
            List<Film> films = jdbcTemplate.query(sql, mapper, userId, friendId);
            loadDataForFilms(films);
            return films;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteFilm(Long filmId) {
        log.info("Удаление фильма с id={}", filmId);

        // Проверяем существование фильма
        Film film = getFilm(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }

        // Удаляем связанные данные (из-за внешних ключей)
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ?", filmId);
        jdbcTemplate.update("DELETE FROM films_genre WHERE film_id = ?", filmId);
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?", filmId);

        // Удаляем сам фильм
        int rows = jdbcTemplate.update("DELETE FROM films WHERE id = ?", filmId);

        if (rows == 0) {
            throw new NotFoundException("Не удалось удалить фильм с id=" + filmId);
        }

        log.info("Фильм с id={} успешно удален", filmId);
    }
}
