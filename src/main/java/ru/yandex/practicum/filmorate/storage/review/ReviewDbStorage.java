package ru.yandex.practicum.filmorate.storage.review;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
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

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt =
                    connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.isPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setLong(4, review.getFilmId());
            return stmt;
        }, keyHolder);

        review.setId(keyHolder.getKey().longValue());
        review.setUseful(0L);

        return review;
    }

    @Override
    public Review updateReview(Review review) {
        jdbcTemplate.update(
                "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?",
                review.getContent(),
                review.isPositive(),
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
                """
                        SELECT * FROM reviews
                        ORDER BY useful DESC
                        """,
                mapper
        );
    }

    @Override
    public List<Review> getReviewsByFilm(Long filmId, int count) {
        return jdbcTemplate.query(
                """
                        SELECT * FROM reviews
                        WHERE film_id = ?
                        ORDER BY useful DESC
                        LIMIT ?
                        """,
                mapper,
                filmId,
                count
        );
    }

    @Override
    public void removeReview(Long reviewId, Long userId) {
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", reviewId);
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
        jdbcTemplate.update(
                "UPDATE reviews SET useful = useful + 1 WHERE id = ?",
                reviewId
        );

        jdbcTemplate.update("INSERT INTO review_likes (review_id, user_id, is_like) values (?, ?, true);", reviewId, userId);
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
        if ((jdbcTemplate.query("SELECT review_id FROM likes WHERE user_id = ? AND user_id = ?", new ColumnMapRowMapper(), reviewId, userId)).contains(userId)) {
            throw new ValidationException("Пользователь с id = " + userId + " уже поставил лайк");
        }
        return true;
    }
}
