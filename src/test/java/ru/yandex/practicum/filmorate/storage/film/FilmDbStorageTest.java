package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        FilmDto dto = new FilmDto();
        dto.setName("Test Film");
        dto.setDescription("Description");
        dto.setReleaseDate(LocalDate.of(2023, 1, 1));
        dto.setDuration(120);
        dto.setMpa(MpaRating.G);
        dto.setGenres(Set.of(new Genre(1L, "Комедия")));

        Film film = FilmMapper.toModel(dto);
        filmDbStorage.createFilm(film);

        assertThat(film.getId()).isNotNull();

        Film fromDb = filmDbStorage.getFilmById(film.getId());
        assertThat(fromDb.getName()).isEqualTo("Test Film");
        assertThat(fromDb.getDuration()).isEqualTo(120);
        assertThat(fromDb.getMpa()).isEqualTo(MpaRating.G);
        assertThat(fromDb.getGenres().stream().map(Genre::getId).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder(1L);
    }

    @Test
    void testUpdateFilm() {
        FilmDto dto = new FilmDto();
        dto.setName("Initial Film");
        dto.setDescription("Initial Description");
        dto.setReleaseDate(LocalDate.of(2023, 1, 1));
        dto.setDuration(100);
        dto.setMpa(MpaRating.PG);
        dto.setGenres(Set.of(new Genre(1L, "Комедия")));

        Film film = FilmMapper.toModel(dto);
        filmDbStorage.createFilm(film);

        dto.setId(film.getId());
        dto.setName("Updated Film");
        dto.setDescription("Updated Description");
        dto.setDuration(150);
        dto.setMpa(MpaRating.R);
        dto.setGenres(Set.of(new Genre(2L, "Драма")));

        Film updatedFilm = FilmMapper.toModel(dto);
        filmDbStorage.updateFilm(updatedFilm);

        Film fromDb = filmDbStorage.getFilmById(film.getId());
        assertThat(fromDb.getName()).isEqualTo("Updated Film");
        assertThat(fromDb.getDescription()).isEqualTo("Updated Description");
        assertThat(fromDb.getDuration()).isEqualTo(150);
        assertThat(fromDb.getMpa()).isEqualTo(MpaRating.R);
        assertThat(fromDb.getGenres().stream().map(Genre::getId).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder(2L);
    }

    @Test
    void testGetAllFilms() {
        FilmDto dto1 = new FilmDto();
        dto1.setName("Film 1");
        dto1.setDescription("Desc 1");
        dto1.setReleaseDate(LocalDate.of(2023, 1, 1));
        dto1.setDuration(90);
        dto1.setMpa(MpaRating.G);
        dto1.setGenres(Set.of(new Genre(1L, "Комедия")));

        FilmDto dto2 = new FilmDto();
        dto2.setName("Film 2");
        dto2.setDescription("Desc 2");
        dto2.setReleaseDate(LocalDate.of(2023, 2, 1));
        dto2.setDuration(110);
        dto2.setMpa(MpaRating.PG);
        dto2.setGenres(Set.of(new Genre(2L, "Драма")));

        Film film1 = FilmMapper.toModel(dto1);
        Film film2 = FilmMapper.toModel(dto2);

        filmDbStorage.createFilm(film1);
        filmDbStorage.createFilm(film2);

        List<Film> films = List.copyOf(filmDbStorage.getAllFilms());
        assertThat(films).hasSizeGreaterThanOrEqualTo(2)
                .extracting(Film::getName)
                .contains("Film 1", "Film 2");
    }

    @Test
    void testDeleteFilm() {
        FilmDto dto = new FilmDto();
        dto.setName("Delete Film");
        dto.setDescription("To be deleted");
        dto.setReleaseDate(LocalDate.of(2023, 1, 1));
        dto.setDuration(100);
        dto.setMpa(MpaRating.PG_13);
        dto.setGenres(Set.of(new Genre(3L, "Мультфильм")));

        Film film = FilmMapper.toModel(dto);
        filmDbStorage.createFilm(film);
        Long id = film.getId();

        filmDbStorage.deleteFilm(film);

        assertThrows(RuntimeException.class, () -> filmDbStorage.getFilmById(id));
    }
}
