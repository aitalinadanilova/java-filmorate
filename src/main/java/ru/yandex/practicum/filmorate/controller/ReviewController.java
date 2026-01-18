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
    public Collection<Review> findAll() {
        return service.getAllReviews();
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        return service.createReview(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        return service.updateReview(review);
    }

    @GetMapping("/{reviewId}")
    public Review getReview(@PathVariable Long reviewId) {
        return service.getReview(reviewId);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void addLike(@PathVariable Long reviewId, Long userId) {
        service.addLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void removeLike(@PathVariable Long reviewId, Long userId) {
        service.removeLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/{userId}")
    public void removeReview(@PathVariable Long reviewId, @PathVariable Long userId) {
        service.removeReview(reviewId, userId);
    }
}
