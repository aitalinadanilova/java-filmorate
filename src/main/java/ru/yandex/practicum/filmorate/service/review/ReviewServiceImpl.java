package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Operation;
import ru.yandex.practicum.filmorate.service.feed.FeedService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedService feedService;

    @Override
    public Review createReview(Review review) {
        log.info("Создание отзыва: {}", review);

        validateReview(review);

        if (filmStorage.getFilm(review.getFilmId()) == null) {
            throw new NotFoundException("Фильм с id=" + review.getFilmId() + " не найден");
        }

        if (userStorage.getUser(review.getUserId()) == null) {
            throw new NotFoundException("Пользователь с id=" + review.getUserId() + " не найден");
        }

        Review newReview = reviewStorage.createReview(review);

        feedService.createFeed(newReview.getUserId(), EventType.REVIEW, Operation.ADD, newReview.getId());

        log.info("Отзыв создан с id={}", newReview.getId());
        return newReview;
    }

    @Override
    public Review updateReview(Review review) {
        getReview(review.getId());

        Review updatedReview = reviewStorage.updateReview(review);

        feedService.createFeed(updatedReview.getUserId(), EventType.REVIEW, Operation.UPDATE, updatedReview.getId());

        return updatedReview;
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
    public void addLike(Long reviewId, Long userId) {
        checkReviewAndUser(reviewId, userId);
        reviewStorage.checkLikeOnReview(reviewId, userId);
        reviewStorage.addLike(reviewId, userId);
    }

    @Override
    public void removeReview(Long reviewId) {
        Review review = getReview(reviewId);
        feedService.createFeed(review.getUserId(), EventType.REVIEW, Operation.REMOVE, reviewId);
        reviewStorage.removeReview(reviewId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        if (reviewStorage.getReview(reviewId) == null) {
            throw new NotFoundException("Отзыв не найден");
        }

        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (!reviewStorage.hasLike(reviewId, userId)) {
            throw new NotFoundException("Лайк от пользователя " + userId + " не найден");
        }

        reviewStorage.removeLike(reviewId, userId);
    }

    @Override
    @Transactional
    public void addDislike(Long reviewId, Long userId) {
        log.info("Ставим dislike отзыву с id: {}", reviewId);
        checkReviewAndUser(reviewId, userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        checkReviewAndUser(reviewId, userId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    @Override
    public Collection<Review> findAll(Long filmId, int count) {
        List<Review> reviews;

        if (filmId != null) {
            if (filmStorage.getFilm(filmId) == null) {
                throw new NotFoundException("Фильм с id=" + filmId + " не найден");
            }
            reviews = reviewStorage.getReviewsByFilm(filmId, count);
        } else {
            reviews = reviewStorage.getAllReviews();
        }

        return reviews.stream()
                .sorted(Comparator.comparingLong(Review::getUseful).reversed())
                .limit(count)
                .toList();
    }

    private void checkReviewAndUser(Long reviewId, Long userId) {
        if (reviewStorage.getReview(reviewId) == null) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Контент отзыва не может быть пустым");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("Поле isPositive должно быть заполнено");
        }
        if (review.getUserId() == null) {
            throw new ValidationException("Поле userId обязательно");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("Поле filmId обязательно");
        }
    }
}