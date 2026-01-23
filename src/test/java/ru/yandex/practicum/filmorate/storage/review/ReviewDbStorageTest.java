package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {ReviewDbStorage.class, ReviewRowMapper.class})
class ReviewDbStorageTest {
    private final ReviewStorage storage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы в порядке, учитывающем внешние ключи (сначала детей, потом родителей)
        jdbcTemplate.update("DELETE FROM review_likes");
        jdbcTemplate.update("DELETE FROM reviews");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM rating_mpa");

        // Сброс счетчиков
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reviews ALTER COLUMN id RESTART WITH 1");

        // Подготовка справочников
        jdbcTemplate.update("INSERT INTO rating_mpa (id, name) VALUES (1, 'G'), (2, 'PG')");

        // Вставляем тестовых пользователей
        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (1, 'u1@mail.ru', 'u1', 'N1', '1990-01-01')");
        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (2, 'u2@mail.ru', 'u2', 'N2', '1992-05-15')");

        // Добавляем тестовые фильмы
        jdbcTemplate.update("INSERT INTO films (id, name, description, release_date, duration, rating_mpa_id) VALUES (1, 'F1', 'D1', '2020-01-01', 120, 1)");
        jdbcTemplate.update("INSERT INTO films (id, name, description, release_date, duration, rating_mpa_id) VALUES (2, 'F2', 'D2', '2021-06-15', 90, 2)");

        // Вставляем отзывы (useful по умолчанию 0 в БД, поэтому вставляем как есть)
        jdbcTemplate.update("INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)", "Great!", true, 1L, 1L, 5);
        jdbcTemplate.update("INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)", "Bad", false, 2L, 1L, 2);
        jdbcTemplate.update("INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)", "Amazing", true, 1L, 2L, 10);
    }

    @Test
    void createReview() {
        Review newReview = Review.builder()
                .content("New review content")
                .isPositive(true)
                .userId(1L)
                .filmId(2L)
                .build();

        Review created = storage.createReview(newReview);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isGreaterThan(0);
        assertThat(created.getContent()).isEqualTo("New review content");
        assertThat(created.getUseful()).isEqualTo(0);
    }

    @Test
    void getReview() {
        Review review = storage.getReview(1L);
        assertThat(review).isNotNull();
        assertThat(review.getContent()).isEqualTo("Great!");
        assertThat(review.getUseful()).isEqualTo(5L);
    }

    @Test
    void updateReview() {
        Review updated = Review.builder()
                .id(1L)
                .content("Updated content")
                .isPositive(false)
                .build();

        storage.updateReview(updated);
        Review result = storage.getReview(1L);

        assertThat(result.getContent()).isEqualTo("Updated content");
        assertThat(result.getIsPositive()).isFalse();
        assertThat(result.getUseful()).isEqualTo(5L);
    }

    @Test
    void getAllReviews() {
        List<Review> reviews = storage.getAllReviews();

        assertThat(reviews).hasSize(3);
        assertThat(reviews.get(0).getUseful()).isEqualTo(10L);
        assertThat(reviews.get(2).getUseful()).isEqualTo(2L);
    }

    @Test
    void getReviewsByFilm() {
        List<Review> reviews = storage.getReviewsByFilm(1L, 10);

        assertThat(reviews).hasSize(2);
        assertThat(reviews).allMatch(r -> r.getFilmId().equals(1L));
        assertThat(reviews.get(0).getUseful()).isEqualTo(5L);
    }

    @Test
    void removeReview() {
        storage.removeReview(1L);
        assertThat(storage.getReview(1L)).isNull();
    }

    @Test
    void addLikeAndRemoveLike() {
        storage.addLike(2L, 1L);
        assertThat(storage.getReview(2L).getUseful()).isEqualTo(3L); // Было 2, стало 3

        storage.removeLike(2L, 1L);
        assertThat(storage.getReview(2L).getUseful()).isEqualTo(2L); // Вернулось к 2
    }

    @Test
    void addDislike() {
        storage.addDislike(1L, 2L);
        assertThat(storage.getReview(1L).getUseful()).isEqualTo(4L); // Было 5, стало 4
    }
}