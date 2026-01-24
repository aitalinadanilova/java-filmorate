package ru.yandex.practicum.filmorate.storage.review;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.util.List;

@AllArgsConstructor
@Component
@Primary
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper mapper;

    @Override
    public Review createReview(Review review) {

        String sql = """
                INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
                VALUES (?, ?, ?, ?, 0)
                """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();

        return Review.builder()
                .id(generatedId)
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(0L)
                .build();
    }

    @Override
    public Review updateReview(Review review) {
        jdbcTemplate.update(
                "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?",
                review.getContent(),
                review.getIsPositive(),
                review.getId()
        );
        return getReview(review.getId());
    }

    @Override
    public Review getReview(Long reviewId) {
        List<Review> reviews = jdbcTemplate.query(
                "SELECT * FROM reviews WHERE id = ?",
                mapper,
                reviewId
        );
        return reviews.isEmpty() ? null : reviews.get(0);
    }

    @Override
    public List<Review> getAllReviews() {
        return jdbcTemplate.query(
                "SELECT * FROM reviews ORDER BY useful DESC",
                mapper
        );
    }

    @Override
    public List<Review> getReviewsByFilm(Long filmId, int count) {
        return jdbcTemplate.query(
                "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?",
                mapper,
                filmId,
                count
        );
    }

    @Override
    public void removeReview(Long reviewId) {
        jdbcTemplate.update("DELETE FROM review_likes WHERE review_id = ?", reviewId);
        int rows = jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", reviewId);

        if (rows == 0) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
    }

    @Override
    public List<User> getLikes(Long reviewId) {
        try {
            return jdbcTemplate.query("SELECT * FROM users WHERE id IN (SELECT user_id FROM review_likes WHERE review_id = ?)", new DataClassRowMapper<>(User.class), reviewId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        List<Boolean> existing = jdbcTemplate.query(
                "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?",
                (rs, rowNum) -> rs.getBoolean("is_like"),
                reviewId,
                userId
        );

        if (!existing.isEmpty()) {
            boolean currentIsLike = existing.get(0);

            if (currentIsLike) {
                // Уже есть лайк
                throw new ValidationException("Пользователь уже поставил лайк этому отзыву");
            }

            jdbcTemplate.update(
                    "UPDATE reviews SET useful = useful + 2 WHERE id = ?",
                    reviewId
            );

            jdbcTemplate.update(
                    "UPDATE review_likes SET is_like = true WHERE review_id = ? AND user_id = ?",
                    reviewId,
                    userId
            );
        } else {
            jdbcTemplate.update(
                    "UPDATE reviews SET useful = useful + 1 WHERE id = ?",
                    reviewId
            );

            jdbcTemplate.update(
                    "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)",
                    reviewId,
                    userId
            );
        }
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        // 1. Уменьшаем счётчик useful
        jdbcTemplate.update(
                "UPDATE reviews SET useful = useful - 1 WHERE id = ?",
                reviewId
        );

        // 2. Удаляем лайк
        jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?",
                reviewId,
                userId
        );
    }

    @Override
    public boolean checkLikeOnReview(Long reviewId, Long userId) {
        List<Long> result = jdbcTemplate.query(
                "SELECT review_id FROM review_likes WHERE review_id = ? AND user_id = ?",
                (rs, rowNum) -> rs.getLong("review_id"),
                reviewId,
                userId
        );

        if (!result.isEmpty()) {
            throw new ValidationException("Пользователь с id = " + userId + " уже поставил лайк");
        }
        return true;
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        List<Boolean> existing = jdbcTemplate.query(
                "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?",
                (rs, rowNum) -> rs.getBoolean("is_like"),
                reviewId,
                userId
        );

        if (!existing.isEmpty()) {
            boolean currentIsLike = existing.get(0);

            if (!currentIsLike) {
                throw new ValidationException("Пользователь уже поставил дизлайк этому отзыву");
            }

            jdbcTemplate.update(
                    "UPDATE reviews SET useful = useful - 2 WHERE id = ?",
                    reviewId
            );

            jdbcTemplate.update(
                    "UPDATE review_likes SET is_like = false WHERE review_id = ? AND user_id = ?",
                    reviewId,
                    userId
            );
        } else {

            jdbcTemplate.update(
                    "UPDATE reviews SET useful = useful - 1 WHERE id = ?",
                    reviewId
            );

            jdbcTemplate.update(
                    "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)",
                    reviewId,
                    userId
            );
        }
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        jdbcTemplate.update("UPDATE reviews SET useful = useful + 1 WHERE id = ?", reviewId);

        jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false",
                reviewId, userId
        );
    }

}
