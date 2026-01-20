package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Override
    public Review createReview(Review review) {
        log.info("Отзыв {} создан", review);
        if (review.getContent() == null || review.getContent().isEmpty()) {
            throw new ValidationException("Отзыв не может быть пустым");
        }

        if (review.getIsPositive() == null) {
            throw new ValidationException("Поле isPositive обязательно");
        }

        if (review.getUserId() == null) {
            throw new ValidationException("Поле userId обязательно");
        }

        if (review.getFilmId() == null) {
            throw new ValidationException("Поле filmId обязательно");
        }

        if (filmStorage.getFilm(review.getFilmId()) == null) {
            System.out.println("HERE not film " + review.getFilmId());
            throw new NotFoundException("Фильм с id=" + review.getFilmId() + " не найден");
        }

        if (userStorage.getUser(review.getUserId()) == null) {
            throw new NotFoundException("Пользователь с id=" + review.getUserId() + " не найден");
        }

        return reviewStorage.createReview(review);
    }

    @Override
    public Review updateReview(Review review) {
        if (review.getId() == null) {
            throw new NullPointerException("Id должен быть указан");
        }

        Review existingReview = reviewStorage.getReview(review.getId());
        if (existingReview == null) {
            throw new NotFoundException("Отзыв с id = " + review.getId() + " не найден");
        }

        return reviewStorage.updateReview(review);
    }

    @Override
    public Review getReview(Long reviewId) {
        Review review = reviewStorage.getReview(reviewId);
        if (review == null) {
            throw new NotFoundException("Отзыв с id = " + reviewId + " не найден");
        }
        return review;
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewStorage.getAllReviews();
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        Review review = reviewStorage.getReview(reviewId);
        if (review == null) {
            throw new NotFoundException("Отзыв отсутствует");
        }

        User user = userStorage.getUser(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь отсутствует");
        }

        reviewStorage.checkLikeOnReview(reviewId, userId);
        reviewStorage.addLike(reviewId, userId);
    }

    @Override
    public void removeReview(Long reviewId) {
        Review review = reviewStorage.getReview(reviewId);
        if (review == null) {
            throw new NotFoundException("Отзыв отсутствует");
        }

        reviewStorage.removeReview(reviewId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        Review review = reviewStorage.getReview(reviewId);
        if (review == null) {
            throw new NotFoundException("Отзыв с таким ID не найден!");
        }

        reviewStorage.removeLike(reviewId, userId);
    }

    @Override
    public List<Review> getReviewsByFilm(Long filmId, int count) {
        if (filmStorage.getFilm(filmId) == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        return reviewStorage.getReviewsByFilm(filmId, count);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        log.info("Ставим dislike отзыву с id: {}", reviewId);
        Review review = reviewStorage.getReview(reviewId);
        if (review == null) {
            throw new NotFoundException("Отзыв отсутствует");
        }

        User user = userStorage.getUser(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь отсутствует");
        }

        reviewStorage.addDislike(reviewId, userId);
    }

}

