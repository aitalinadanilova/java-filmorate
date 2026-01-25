package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
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
    public ReviewDto createReview(ReviewDto dto) {
        log.info("Создание отзыва: {}", dto);

        validateReviewDto(dto);

        Review review = ReviewMapper.toEntity(dto);

        if (filmStorage.getFilm(review.getFilmId()) == null) {
            throw new NotFoundException("Фильм с id=" + review.getFilmId() + " не найден");
        }

        if (userStorage.getUser(review.getUserId()) == null) {
            throw new NotFoundException("Пользователь с id=" + review.getUserId() + " не найден");
        }

        Review newReview = reviewStorage.createReview(review);

        feedService.createFeed(newReview.getUserId(), EventType.REVIEW, Operation.ADD, newReview.getId());

        log.info("Отзыв создан с id={}", newReview.getId());
        return ReviewMapper.toDto(newReview);
    }

    @Override
    public Review updateReview(Review review) {
        Review oldReview = reviewStorage.getReview(review.getId());
        if (oldReview == null) throw new NotFoundException("Review not found");

        review.setUserId(oldReview.getUserId());
        review.setFilmId(oldReview.getFilmId());

        reviewStorage.updateReview(review);

        feedService.createFeed(oldReview.getUserId(), EventType.REVIEW, Operation.UPDATE, review.getId());

        return reviewStorage.getReview(review.getId());
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

        feedService.createFeed(review.getUserId(), EventType.REVIEW, Operation.REMOVE, reviewId);
        reviewStorage.removeReview(reviewId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {

        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь отсутствует");
        }

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
    @Transactional
    public void addDislike(Long reviewId, Long userId) {
        log.info("Ставим dislike отзыву с id: {}", reviewId);

        if (reviewStorage.getReview(reviewId) == null) {
            throw new NotFoundException("Отзыв отсутствует");
        }
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь отсутствует");
        }
        reviewStorage.addDislike(reviewId, userId);

    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        if (reviewStorage.getReview(reviewId) == null) {
            throw new NotFoundException("Отзыв не найден");
        }
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        reviewStorage.removeDislike(reviewId, userId);
    }

    @Override
    public Collection<ReviewDto> findAll(Long filmId, int count) {
        List<Review> reviews;

        if (filmId != null) {
            // Проверяем существование фильма
            if (filmStorage.getFilm(filmId) == null) {
                throw new NotFoundException("Фильм с id=" + filmId + " не найден");
            }
            reviews = reviewStorage.getReviewsByFilm(filmId, count);
        } else {
            reviews = reviewStorage.getAllReviews().stream()
                    .limit(count)
                    .toList();
        }

        return reviews.stream()
                .sorted(Comparator.comparingLong(Review::getUseful).reversed())
                .map(ReviewMapper::toDto)
                .toList();
    }

    private void validateReviewDto(ReviewDto dto) {
        if (dto.getContent() == null || dto.getContent().isEmpty()) {
            throw new ValidationException("Контент отзыва не может быть пустым");
        }

        if (dto.getUserId() == null) {
            throw new ValidationException("Поле userId обязательно");
        }

        if (dto.getFilmId() == null) {
            throw new ValidationException("Поле filmId обязательно");
        }
    }
}
