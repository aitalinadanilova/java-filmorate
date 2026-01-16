package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    void testCreateAndGetFilmWithDirector() {
        Mpa mpa = new Mpa(1L, "G");

        Director director = Director.builder()
                .id(0L)
                .name("Christopher Nolan")
                .build();
        Director createdDirector = filmStorage.createDirector(director);

        Film film = Film.builder()
                .name("Inception")
                .description("Dream within a dream")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(mpa)
                .director(List.of(createdDirector))
                .build();

        Film createdFilm = filmStorage.createFilm(film);

        Film savedFilm = filmStorage.getFilm(createdFilm.getId());

        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getName()).isEqualTo("Inception");
        assertThat(savedFilm.getDirector()).hasSize(1);
        assertThat(savedFilm.getDirector().get(0).getName()).isEqualTo("Christopher Nolan");
    }

    @Test
    void testUpdateFilm() {
        Mpa mpa = new Mpa(1L, "G");
        Film film = filmStorage.createFilm(Film.builder()
                .name("Original Name")
                .description("Desc")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(mpa)
                .build());

        film.setName("Updated Name");
        filmStorage.updateFilm(film);

        Film updatedFilm = filmStorage.getFilm(film.getId());
        assertThat(updatedFilm.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testFindSortFilmsByDirector() {
        Director director = filmStorage.createDirector(Director.builder().id(0L).name("Director X").build());
        Mpa mpa = new Mpa(1L, "G");

        Film film1 = Film.builder()
                .name("Film 2020")
                .description("Description 1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(100)
                .mpa(mpa)
                .director(List.of(director))
                .build();

        Film film2 = Film.builder()
                .name("Film 2010")
                .description("Description 2")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(100)
                .mpa(mpa)
                .director(List.of(director))
                .build();

        filmStorage.createFilm(film1);
        filmStorage.createFilm(film2);

        List<Film> sortedFilms = filmStorage.findSortFilmsByDirector(director.getId(), "year");

        assertThat(sortedFilms).hasSize(2);
        assertThat(sortedFilms.get(0).getName()).isEqualTo("Film 2010"); // Старый фильм первый
        assertThat(sortedFilms.get(1).getName()).isEqualTo("Film 2020");
    }
}