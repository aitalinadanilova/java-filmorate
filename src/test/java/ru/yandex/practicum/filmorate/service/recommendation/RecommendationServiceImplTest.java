package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RecommendationServiceImplTest {

    private final RecommendationService recommendationService;
    private final JdbcTemplate jdbcTemplate;
    private final ru.yandex.practicum.filmorate.storage.film.FilmStorage filmStorage;

    @Test
    @Sql(scripts = {"/clear-for-recommendations.sql"})
    void testGetRecommendations() {
        // Создаем пользователей (без указания ID, чтобы генерировались автоматически)
        jdbcTemplate.update(
                "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user1@test.ru", "user1", "User One", LocalDate.of(1990, 1, 1)
        );
        jdbcTemplate.update(
                "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user2@test.ru", "user2", "User Two", LocalDate.of(1995, 1, 1)
        );

        // Создаем фильмы с существующим рейтингом MPA (id=1 должен быть из data.sql)
        Mpa mpa = Mpa.builder().id(1L).name("G").build();

        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();
        film1 = filmStorage.createFilm(film1);

        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(90)
                .mpa(mpa)
                .build();
        film2 = filmStorage.createFilm(film2);

        Film film3 = Film.builder()
                .name("Film 3")
                .description("Description 3")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(150)
                .mpa(mpa)
                .build();
        film3 = filmStorage.createFilm(film3);

        // Получаем ID пользователей (они будут 1 и 2 после очистки базы)
        Long user1Id = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = 'user1@test.ru'", Long.class);
        Long user2Id = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = 'user2@test.ru'", Long.class);

        // Добавляем лайки
        filmStorage.addLike(film1.getId(), user1Id);
        filmStorage.addLike(film2.getId(), user1Id);

        filmStorage.addLike(film1.getId(), user2Id);
        filmStorage.addLike(film3.getId(), user2Id);

        // Получаем рекомендации для пользователя 1
        List<Film> recommendations = recommendationService.getRecommendations(user1Id);

        // Ожидаем, что будет рекомендован фильм 3
        assertEquals(1, recommendations.size(), "Должен быть рекомендован 1 фильм");
        assertEquals(film3.getId(), recommendations.get(0).getId(), "Должен быть рекомендован Film 3");
    }

    @Test
    @Sql(scripts = {"/clear-for-recommendations.sql"})
    void testGetRecommendationsNoSimilarUsers() {
        // Создаем пользователя без лайков
        jdbcTemplate.update(
                "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user@test.ru", "user", "User", LocalDate.of(1990, 1, 1)
        );

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = 'user@test.ru'", Long.class);

        // Получаем рекомендации
        List<Film> recommendations = recommendationService.getRecommendations(userId);

        // Ожидаем пустой список
        assertTrue(recommendations.isEmpty(), "Для пользователя без лайков должны быть пустые рекомендации");
    }

    @Test
    void testGetRecommendationsUserNotFound() {
        // Пытаемся получить рекомендации для несуществующего пользователя
        assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> recommendationService.getRecommendations(999L),
                "Для несуществующего пользователя должно выбрасываться исключение"
        );
    }
}