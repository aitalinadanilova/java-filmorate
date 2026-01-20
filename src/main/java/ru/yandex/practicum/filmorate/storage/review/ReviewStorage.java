package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface ReviewStorage {
    Review createReview(Review review);

    Review updateReview(Review review);

    Review getReview(Long reviewId);

    List<Review> getAllReviews();

    List<User> getLikes(Long reviewId);

    void addLike(Long id, Long userId);

    void removeReview(Long id);

    List<Review> getReviewsByFilm(Long filmId, int count);

    void removeLike(Long id, Long userId);

    boolean checkLikeOnReview(Long reviewId, Long userId);

    void addDislike(Long id, Long userId);

    boolean checkDislikeOnReview(Long reviewId, Long userId);
}
