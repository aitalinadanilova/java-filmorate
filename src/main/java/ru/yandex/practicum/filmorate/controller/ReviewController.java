package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;

    @GetMapping
    public Collection<Review> findAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос GET /reviews для filmId: {}, count: {}", filmId, count);
        return service.findAll(filmId, count);
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        log.info("Получен запрос POST /reviews: {}", review);
        return service.createReview(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        log.info("Получен запрос PUT /reviews: {}", review);
        return service.updateReview(review);
    }

    @GetMapping("/{reviewId}")
    public Review getReview(@PathVariable Long reviewId) {
        log.info("Получен запрос GET /reviews/{}", reviewId);
        return service.getReview(reviewId);
    }

    @DeleteMapping("/{reviewId}")
    public void removeReview(@PathVariable Long reviewId) {
        log.info("Получен запрос DELETE /reviews/{}", reviewId);
        service.removeReview(reviewId);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void addLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Пользователь {} ставит лайк отзыву {}", userId, reviewId);
        service.addLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void removeLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Пользователь {} удаляет лайк у отзыва {}", userId, reviewId);
        service.removeLike(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDislike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Пользователь {} ставит дизлайк отзыву {}", userId, reviewId);
        service.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void removeDislike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Пользователь {} удаляет дизлайк у отзыва {}", userId, reviewId);
        service.removeDislike(reviewId, userId);
    }
}