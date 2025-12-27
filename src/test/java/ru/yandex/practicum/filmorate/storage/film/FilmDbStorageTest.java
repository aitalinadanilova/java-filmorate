package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

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
        dto.setMpaId(1);
        dto.setGenreIds(Set.of(1L));

        Film film = FilmMapper.toModel(dto);
        filmDbStorage.createFilm(film);

        assertThat(film.getId()).isNotNull();

        Film fromDb = filmDbStorage.getFilmById(film.getId());
        assertThat(fromDb.getName()).isEqualTo("Test Film");
        assertThat(fromDb.getDuration()).isEqualTo(120);
        assertThat(fromDb.getMpa().getId()).isEqualTo(1);
        assertThat(fromDb.getGenres().stream().map(g -> g.getId()).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder(1L);
    }

    @Test
    void testUpdateFilm() {
        FilmDto dto = new FilmDto();
        dto.setName("Initial Film");
        dto.setDescription("Initial Description");
        dto.setReleaseDate(LocalDate.of(2023, 1, 1));
        dto.setDuration(100);
        dto.setMpaId(2);
        dto.setGenreIds(Set.of(1L));

        Film film = FilmMapper.toModel(dto);
        filmDbStorage.createFilm(film);

        dto.setId(film.getId());
        dto.setName("Updated Film");
        dto.setDescription("Updated Description");
        dto.setDuration(150);
        dto.setMpaId(3);
        dto.setGenreIds(Set.of(2L));

        Film updatedFilm = FilmMapper.toModel(dto);
        filmDbStorage.updateFilm(updatedFilm);

        Film fromDb = filmDbStorage.getFilmById(film.getId());
        assertThat(fromDb.getName()).isEqualTo("Updated Film");
        assertThat(fromDb.getDescription()).isEqualTo("Updated Description");
        assertThat(fromDb.getDuration()).isEqualTo(150);
        assertThat(fromDb.getMpa().getId()).isEqualTo(3);
        assertThat(fromDb.getGenres().stream().map(g -> g.getId()).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder(2L);
    }

    @Test
    void testGetAllFilms() {
        FilmDto dto1 = new FilmDto();
        dto1.setName("Film 1");
        dto1.setDescription("Desc 1");
        dto1.setReleaseDate(LocalDate.of(2023, 1, 1));
        dto1.setDuration(90);
        dto1.setMpaId(1);

        FilmDto dto2 = new FilmDto();
        dto2.setName("Film 2");
        dto2.setDescription("Desc 2");
        dto2.setReleaseDate(LocalDate.of(2023, 2, 1));
        dto2.setDuration(110);
        dto2.setMpaId(2);

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
        dto.setMpaId(4);

        Film film = FilmMapper.toModel(dto);
        filmDbStorage.createFilm(film);
        Long id = film.getId();

        filmDbStorage.deleteFilm(film);

        assertThrows(RuntimeException.class, () -> filmDbStorage.getFilmById(id));
    }
}
