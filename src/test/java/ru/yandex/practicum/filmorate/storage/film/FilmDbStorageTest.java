package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Test
    void testCreateAndGetFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(120);
        film.setMpa(MpaRating.G);
        film.setGenres(Set.of(new Genre(1L, "Comedy")));

        filmDbStorage.createFilm(film);

        assertThat(film.getId()).isNotNull();

        Film fromDb = filmDbStorage.getFilmById(film.getId());
        assertThat(fromDb)
                .hasFieldOrPropertyWithValue("name", "Test Film")
                .hasFieldOrPropertyWithValue("duration", 120)
                .hasFieldOrPropertyWithValue("mpa", MpaRating.G);

        assertThat(fromDb.getGenres())
                .hasSize(1)
                .contains(new Genre(1L, "Comedy"));
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Initial Film");
        film.setDescription("Initial Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(100);
        film.setMpa(MpaRating.PG);
        film.setGenres(Set.of(new Genre(1L, "Comedy")));

        filmDbStorage.createFilm(film);

        // Обновляем
        film.setName("Updated Film");
        film.setDescription("Updated Description");
        film.setDuration(150);
        film.setMpa(MpaRating.PG_13);
        film.setGenres(Set.of(new Genre(2L, "Drama")));

        filmDbStorage.updateFilm(film);

        Film updated = filmDbStorage.getFilmById(film.getId());
        assertThat(updated)
                .hasFieldOrPropertyWithValue("name", "Updated Film")
                .hasFieldOrPropertyWithValue("description", "Updated Description")
                .hasFieldOrPropertyWithValue("duration", 150)
                .hasFieldOrPropertyWithValue("mpa", MpaRating.PG_13);

        assertThat(updated.getGenres())
                .hasSize(1)
                .contains(new Genre(2L, "Drama"));
    }

    @Test
    void testGetAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Desc 1");
        film1.setReleaseDate(LocalDate.of(2023, 1, 1));
        film1.setDuration(90);
        film1.setMpa(MpaRating.G);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Desc 2");
        film2.setReleaseDate(LocalDate.of(2023, 2, 1));
        film2.setDuration(110);
        film2.setMpa(MpaRating.PG);

        filmDbStorage.createFilm(film1);
        filmDbStorage.createFilm(film2);

        List<Film> films = List.copyOf(filmDbStorage.getAllFilms());
        assertThat(films).hasSizeGreaterThanOrEqualTo(2)
                .extracting("name")
                .contains("Film 1", "Film 2");
    }

    @Test
    void testDeleteFilm() {
        Film film = new Film();
        film.setName("Delete Film");
        film.setDescription("To be deleted");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(100);
        film.setMpa(MpaRating.R);

        filmDbStorage.createFilm(film);
        Long id = film.getId();

        filmDbStorage.deleteFilm(film);

        assertThrows(RuntimeException.class, () -> filmDbStorage.getFilmById(id));
    }
}

