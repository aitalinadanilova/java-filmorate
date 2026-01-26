package ru.yandex.practicum.filmorate.service.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.List;

public interface ReviewService {

    Collection<Review> findAll(Long filmId, int count);

    Review createReview(Review review);

    Review updateReview(Review review);

    Review getReview(Long reviewId);

    void removeReview(Long reviewId);

    void addLike(Long reviewId, Long userId);

    void removeLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void removeDislike(Long reviewId, Long userId);
}
