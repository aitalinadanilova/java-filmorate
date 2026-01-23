package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;

    @GetMapping
    public Collection<ReviewDto> findAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
            return service.findAll(filmId, count);
    }


    @PostMapping
    public ReviewDto create(@Valid @RequestBody ReviewDto dto) {
        return service.createReview(dto);
    }

    @PutMapping
    public ReviewDto update(@Valid @RequestBody ReviewDto dto) {
        Review review = ReviewMapper.toEntity(dto);
        Review updated = service.updateReview(review);
        return ReviewMapper.toDto(updated);
    }

    @GetMapping("/{reviewId}")
    public ReviewDto getReview(@PathVariable Long reviewId) {
        Review review = service.getReview(reviewId);
        return ReviewMapper.toDto(review);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void addLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        service.addLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void removeLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        service.removeLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}")
    public void removeReview(@PathVariable Long reviewId) {
        service.removeReview(reviewId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDislike(@PathVariable Long reviewId, @PathVariable Long userId) {
        service.addDislike(reviewId, userId);
    }
}
