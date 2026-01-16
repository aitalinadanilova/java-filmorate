package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {FilmDbStorage.class})
@ComponentScan(basePackages = {"ru.yandex.practicum.filmorate.storage.film"})
class FilmDbStorageTest {

    private final FilmStorage storage;

    private Film testFilm;
    private Director testDirector;

    @BeforeEach
    void setUp() {
        // Создаём тестового режиссёра
        testDirector = storage.createDirector(
                Director.builder()
                        .id(0L) // временный id, БД заменит его на сгенерированный
                        .name("Quentin Tarantino")
                        .build()
        );


        // Создаём тестовый фильм
        testFilm = storage.createFilm(Film.builder()
                .name("Pulp Fiction")
                .description("Crime movie")
                .releaseDate(LocalDate.of(1994, 10, 14))
                .duration(154)
                .mpa(new Mpa(1L, "G"))
                .build()
        );

        // Связываем фильм с режиссёром
        storage.assignDirectorToFilm(testFilm.getId(), testDirector.getId());
    }

    @Test
    void createFilmAndDirector() {
        Film film = storage.getFilm(testFilm.getId());
        assertThat(film).isNotNull();
        assertThat(film.getName()).isEqualTo("Pulp Fiction");
        assertThat(film.getGenres()).isEmpty();
        assertThat(film.getLikes()).isEmpty();

        Director director = storage.findDirectorById(testDirector.getId());
        assertThat(director).isNotNull();
        assertThat(director.getName()).isEqualTo("Quentin Tarantino");
    }

    @Test
    void updateFilm() {
        testFilm.setName("Pulp Fiction Updated");
        testFilm.setDuration(155);
        storage.updateFilm(testFilm);

        Film film = storage.getFilm(testFilm.getId());
        assertThat(film.getName()).isEqualTo("Pulp Fiction Updated");
        assertThat(film.getDuration()).isEqualTo(155);
    }

    @Test
    void getAllFilms() {
        List<Film> films = storage.getAllFilms();
        assertThat(films).hasSize(1);
        assertThat(films.get(0).getName()).isEqualTo("Pulp Fiction");
    }

    @Test
    void findSortFilmsByDirectorByYear() {
        List<Film> films = storage.findSortFilmsByDirector(testDirector.getId(), "year");
        assertThat(films).isNotEmpty();
        assertThat(films.get(0).getName()).isEqualTo("Pulp Fiction");
    }

    @Test
    void findSortFilmsByDirectorByLikes() {
        List<Film> films = storage.findSortFilmsByDirector(testDirector.getId(), "likes");
        assertThat(films).isNotEmpty();
        assertThat(films.get(0).getName()).isEqualTo("Pulp Fiction");
    }

    @Test
    void deleteDirector() {
        boolean deleted = storage.deleteDirectorById(testDirector.getId());
        assertThat(deleted).isTrue();
    }
}
