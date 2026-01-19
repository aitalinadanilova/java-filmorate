package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RecommendationServiceImplTest {

    private final RecommendationService recommendationService;
    private final ru.yandex.practicum.filmorate.storage.user.UserStorage userStorage;
    private final ru.yandex.practicum.filmorate.storage.film.FilmStorage filmStorage;

    @Test
    @Sql(scripts = {"/clear-all.sql"})
    void testGetRecommendations() {
        // Создаем пользователей
        User user1 = User.builder()
                .email("user1@test.ru")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        user1 = userStorage.createUser(user1);

        User user2 = User.builder()
                .email("user2@test.ru")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();
        user2 = userStorage.createUser(user2);

        // Создаем фильмы
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(ru.yandex.practicum.filmorate.model.Mpa.builder().id(1L).build())
                .build();
        film1 = filmStorage.createFilm(film1);

        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(90)
                .mpa(ru.yandex.practicum.filmorate.model.Mpa.builder().id(1L).build())
                .build();
        film2 = filmStorage.createFilm(film2);

        Film film3 = Film.builder()
                .name("Film 3")
                .description("Description 3")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(150)
                .mpa(ru.yandex.practicum.filmorate.model.Mpa.builder().id(1L).build())
                .build();
        film3 = filmStorage.createFilm(film3);

        // Пользователь 1 лайкает фильмы 1 и 2
        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user1.getId());

        // Пользователь 2 лайкает фильмы 1 и 3
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user2.getId());

        // Получаем рекомендации для пользователя 1
        List<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        // Ожидаем, что будет рекомендован фильм 3 (лайкнутый пользователем 2, но не пользователем 1)
        assertEquals(1, recommendations.size());
        assertEquals(film3.getId(), recommendations.get(0).getId());
    }

    @Test
    @Sql(scripts = {"/clear-all.sql"})
    void testGetRecommendationsNoSimilarUsers() {
        // Создаем пользователя без лайков
        User user = User.builder()
                .email("user@test.ru")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        user = userStorage.createUser(user);

        // Получаем рекомендации
        List<Film> recommendations = recommendationService.getRecommendations(user.getId());

        // Ожидаем пустой список, так как нет пользователей с пересекающимися лайками
        assertTrue(recommendations.isEmpty());
    }
}