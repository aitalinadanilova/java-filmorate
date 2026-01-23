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
@ContextConfiguration(classes = {ReviewStorage.class})
@ComponentScan(basePackages = {"ru.yandex.practicum.filmorate.storage.review"})
class ReviewDbStorageTest {
    private final ReviewStorage storage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы
        jdbcTemplate.update("DELETE FROM reviews");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films_genre");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM rating_mpa");

        jdbcTemplate.update("ALTER TABLE reviews ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE rating_mpa ALTER COLUMN id RESTART WITH 1");

        // Добавляем тестовые данные для MPA рейтингов
        jdbcTemplate.update("INSERT INTO rating_mpa (id, name) VALUES (1, 'G')");
        jdbcTemplate.update("INSERT INTO rating_mpa (id, name) VALUES (2, 'PG')");

        // Вставляем тестовых пользователей
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                1L, "user1@example.com", "user1", "User One", "1990-01-01"
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                2L, "user2@example.com", "user2", "User Two", "1992-05-15"
        );

        // Добавляем тестовые фильмы
        jdbcTemplate.update(
                "INSERT INTO films (id, name, description, release_date, duration, rating_mpa_id) VALUES (?, ?, ?, ?, ?, ?)",
                1L, "Test Film 1", "Description for film 1", "2020-01-01", 120, 1L
        );
        jdbcTemplate.update(
                "INSERT INTO films (id, name, description, release_date, duration, rating_mpa_id) VALUES (?, ?, ?, ?, ?, ?)",
                2L, "Test Film 2", "Description for film 2", "2021-06-15", 90, 2L
        );

        // Добавляем тестовые отзывы (без указания ID, пусть auto-increment сработает)
        jdbcTemplate.update(
                "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)",
                "This is a great movie!", true, 1L, 1L, 5L
        );
        jdbcTemplate.update(
                "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)",
                "Not bad, but could be better", false, 2L, 1L, 2L
        );
        jdbcTemplate.update(
                "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)",
                "Amazing cinematography", true, 1L, 2L, 10L
        );
    }

    @Test
    void createReview() {
        Review newReview = new Review(
                null,
                "Great film!",
                true,
                1L,
                1L,
                0L
        );

        Review created = storage.createReview(newReview);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created).hasFieldOrPropertyWithValue("content", "Great film!");
        assertThat(created).hasFieldOrPropertyWithValue("isPositive", true);
        assertThat(created).hasFieldOrPropertyWithValue("userId", 1L);
        assertThat(created).hasFieldOrPropertyWithValue("filmId", 1L);
        assertThat(created).hasFieldOrPropertyWithValue("useful", 0L);
    }

    @Test
    void getReview() {
        Review review = storage.getReview(1L);

        assertThat(review).isNotNull();
        assertThat(review).hasFieldOrPropertyWithValue("content", "This is a great movie!");
        assertThat(review).hasFieldOrPropertyWithValue("isPositive", true);
        assertThat(review).hasFieldOrPropertyWithValue("userId", 1L);
        assertThat(review).hasFieldOrPropertyWithValue("filmId", 1L);
        assertThat(review).hasFieldOrPropertyWithValue("useful", 5L);
    }

    @Test
    void updateReview() {
        Review updated = new Review(
                1L,
                "Updated content",
                false,
                1L,
                1L,
                5L
        );

        Review result = storage.updateReview(updated);

        assertThat(result).isNotNull();
        assertThat(result).hasFieldOrPropertyWithValue("content", "Updated content");
        assertThat(result).hasFieldOrPropertyWithValue("isPositive", false);
    }

    @Test
    void getAllReviews() {

        assertThat(reviews).isNotEmpty();
        assertThat(reviews).hasSizeGreaterThanOrEqualTo(3);

        // Проверяем что все отзывы на месте
        boolean hasReview1 = reviews.stream().anyMatch(r -> r.getContent().equals("This is a great movie!"));
        boolean hasReview2 = reviews.stream().anyMatch(r -> r.getContent().equals("Not bad, but could be better"));
        boolean hasReview3 = reviews.stream().anyMatch(r -> r.getContent().equals("Amazing cinematography"));

        assertThat(hasReview1).isTrue();
        assertThat(hasReview2).isTrue();
        assertThat(hasReview3).isTrue();
    }

    @Test
    void getReviewsByFilm() {
        List<Review> reviews = storage.getReviewsByFilm(1L, 10);

        assertThat(reviews)
                .isNotEmpty()
                .hasSize(2);

        assertThat(reviews).allSatisfy(review -> {
            assertThat(review.getFilmId()).isEqualTo(1L);
        });

        assertThat(reviews.get(0).getUseful()).isEqualTo(5L);
        assertThat(reviews.get(1).getUseful()).isEqualTo(2L);

        assertThat(reviews.get(0).getUseful()).isGreaterThanOrEqualTo(reviews.get(1).getUseful());
    }

    @Test
    void removeReview() {
        storage.removeReview(1L);

        Review deletedReview = storage.getReview(1L);
        assertThat(deletedReview).isNull();
    }

    @Test
    void addLike() {
        Review reviewBefore = storage.getReview(1L);
        Long usefulBefore = reviewBefore.getUseful();

        storage.addLike(1L, 1L);

        Review reviewAfter = storage.getReview(1L);
        assertThat(reviewAfter.getUseful()).isEqualTo(usefulBefore + 1);
    }

    @Test
    void removeLike() {
        Review reviewBefore = storage.getReview(1L);
        Long usefulBefore = reviewBefore.getUseful();

        storage.removeLike(1L, 1L);

        Review reviewAfter = storage.getReview(1L);
        assertThat(reviewAfter.getUseful()).isEqualTo(usefulBefore - 1);
    }

    @Test
    void getReviewForNonExistentId() {
        Review review = storage.getReview(999L);
        assertThat(review).isNull();
    }

    @Test
    void getReviewsByNonExistentFilm() {
        List<Review> reviews = storage.getReviewsByFilm(999L, 10);
        assertThat(reviews).isEmpty();
    }
}