package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;

@Component
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void createFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                preparedStatement.setInt(5, film.getMpa().getId());
            } else {
                preparedStatement.setNull(5, Types.INTEGER);
            }
            return preparedStatement;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        saveFilmGenres(film);
    }

    @Override
    public void updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );
        updateFilmGenres(film);
    }

    @Override
    public void deleteFilm(Film film) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", film.getId());
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
    }

    @Override
    public Film getFilmById(long filmId) {
        String sql = "SELECT * FROM films WHERE id = ?";
        Film filmFromDb = jdbcTemplate.query(sql, (result, rowNum) -> {
            Film film = new Film();
            film.setId(result.getLong("id"));
            film.setName(result.getString("name"));
            film.setDescription(result.getString("description"));
            film.setReleaseDate(result.getDate("release_date").toLocalDate());
            film.setDuration(result.getInt("duration"));
            int mpaId = result.getInt("mpa_id");
            if (!result.wasNull()) {
                film.setMpa(MpaRating.fromId(mpaId));
            }
            return film;
        }, filmId).stream().findFirst().orElseThrow(() -> new RuntimeException("Film not found with id: " + filmId));

        filmFromDb.setGenres(getFilmGenres(filmId));
        return filmFromDb;
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, (result, rowNum) -> {
            Film film = new Film();
            film.setId(result.getLong("id"));
            film.setName(result.getString("name"));
            film.setDescription(result.getString("description"));
            film.setReleaseDate(result.getDate("release_date").toLocalDate());
            film.setDuration(result.getInt("duration"));
            int mpaId = result.getInt("mpa_id");
            if (!result.wasNull()) {
                film.setMpa(MpaRating.fromId(mpaId));
            }
            return film;
        });

        for (Film film : films) {
            film.setGenres(getFilmGenres(film.getId()));
        }
        return films;
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    private void updateFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveFilmGenres(film);
    }

    private Set<Genre> getFilmGenres(long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (result, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(result.getLong("id"));
            genre.setName(result.getString("name"));
            return genre;
        }, filmId);
        return new HashSet<>(genres);
    }
}
