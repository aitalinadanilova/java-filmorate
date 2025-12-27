package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;

    @Test
    void testCreateAndGetFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(MpaRating.G);

        filmDbStorage.createFilm(film);

        Film retrieved = filmDbStorage.getFilmById(film.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getName()).isEqualTo("Test Film");
    }

    @Test
    void testUpdateFilm() {
        Film film = filmDbStorage.getFilmById(1);
        film.setName("Updated Film");
        filmDbStorage.updateFilm(film);

        Film updated = filmDbStorage.getFilmById(1);
        assertThat(updated.getName()).isEqualTo("Updated Film");
    }

    @Test
    void testDeleteFilm() {
        Film film = new Film();
        film.setName("Delete Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(100);
        film.setMpa(MpaRating.PG);

        filmDbStorage.createFilm(film);
        long id = film.getId();

        filmDbStorage.deleteFilm(film);
        assertThatThrownBy(() -> filmDbStorage.getFilmById(id))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testGenres() {
        Film film = filmDbStorage.getFilmById(1);
        Set<Genre> genres = film.getGenres();
        assertThat(genres).isNotNull();
    }

    @Test
    void testGetAllFilms() {
        List<Film> films = (List<Film>) filmDbStorage.getAllFilms();
        assertThat(films).isNotEmpty();
    }
}
