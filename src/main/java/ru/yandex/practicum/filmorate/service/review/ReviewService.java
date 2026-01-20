package ru.yandex.practicum.filmorate.service.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewService {

    Review createReview(Review review);

    Review updateReview(Review review);

    Review getReview(Long reviewId);

    List<Review> getAllReviews();

    void removeReview(Long reviewId);

    void addLike(Long reviewId, Long userId);

    void removeLike(Long reviewId, Long userId);

    List<Review> getReviewsByFilm(Long filmId, int count);
}
