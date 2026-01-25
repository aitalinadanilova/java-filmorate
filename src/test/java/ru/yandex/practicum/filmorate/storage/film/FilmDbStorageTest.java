package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, DirectorDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final DirectorDbStorage directorStorage;

    @Test
    void testCreateAndGetFilmWithDirector() {
        Mpa mpa = new Mpa(1L, "G");

        Director createdDirector = directorStorage.create(Director.builder().name("Christopher Nolan").build());

        Film film = Film.builder()
                .name("Inception")
                .description("Dream within a dream")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(mpa)
                .directors(List.of(createdDirector))
                .build();

        Film createdFilm = filmStorage.createFilm(film);
        Film savedFilm = filmStorage.getFilm(createdFilm.getId());

        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getDirectors()).hasSize(1);
        assertThat(savedFilm.getDirectors().get(0).getName()).isEqualTo("Christopher Nolan");
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
        Director director = directorStorage.create(Director.builder().name("Director X").build());
        Mpa mpa = new Mpa(1L, "G");

        filmStorage.createFilm(Film.builder()
                .name("Film 2020").description("D1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(100).mpa(mpa)
                .directors(List.of(director)).build());

        filmStorage.createFilm(Film.builder()
                .name("Film 2010").description("D2")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(100).mpa(mpa)
                .directors(List.of(director)).build());

        List<Film> sortedFilms = filmStorage.findSortFilmsByDirector(director.getId(), "year");

        assertThat(sortedFilms).hasSize(2);
        assertThat(sortedFilms.get(0).getReleaseDate().getYear()).isEqualTo(2010);
        assertThat(sortedFilms.get(1).getReleaseDate().getYear()).isEqualTo(2020);
    }

    @Test
    @Sql(scripts = {"/common-films-test.sql"})
    void testGetCommonFilms() {
        // В скрипте созданы 4 фильма и 3 пользователя
        // User1 лайкнул фильмы 1, 2, 3
        // User2 лайкнул фильмы 2, 3, 4
        // Общие фильмы: 2, 3
        // Сортировка по популярности: оба фильма имеют по 2 лайка

        List<Film> commonFilms = filmStorage.getCommonFilms(1L, 2L);

        // Проверяем, что найдено 2 общих фильма
        assertThat(commonFilms).hasSize(2);

        // Проверяем ID фильмов (должны быть 2 и 3)
        List<Long> filmIds = commonFilms.stream()
                .map(Film::getId)
                .toList();

        assertThat(filmIds).containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    @Sql(scripts = {"/common-films-test.sql"})
    void testGetCommonFilmsNoCommon() {
        // User1 лайкнул фильмы: 1, 2, 3
        // User3 лайкнул фильмы: 1, 4
        // Общие фильмы: только 1

        List<Film> commonFilms = filmStorage.getCommonFilms(1L, 3L);

        assertThat(commonFilms).hasSize(1);
        assertThat(commonFilms.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @Sql(scripts = {"/common-films-test.sql"})
    void testGetCommonFilmsEmptyResult() {
        // Тест на отсутствие общих фильмов с несуществующим пользователем
        List<Film> commonFilms = filmStorage.getCommonFilms(1L, 999L);
        assertThat(commonFilms).isEmpty();
    }

    @Test
    @Sql(scripts = {"/common-films-test.sql"})
    void testGetCommonFilmsNoCommonBetweenTwoUsers() {
        // Создаем нового пользователя без лайков
        // (это можно сделать через jdbcTemplate, но проще проверить с 999L)
        List<Film> commonFilms = filmStorage.getCommonFilms(1L, 999L);
        assertThat(commonFilms).isEmpty();
    }
}