package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

public class ReviewMapper {
    private ReviewMapper() {
    }

    public static ReviewDto toDto(Review review) {

        ReviewDto reviewDto = ru.yandex.practicum.filmorate.dto.ReviewDto.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .isPositive(review.isPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();
        System.out.println("Response DTO: " + reviewDto);
        return reviewDto;
    }

    public static Review toEntity(ReviewDto dto) {
        return Review.builder()
                .id(dto.getReviewId())
                .content(dto.getContent())
                .isPositive(dto.isPositive())
                .userId(dto.getUserId())
                .filmId(dto.getFilmId())
                .useful(dto.getUseful())
                .build();
    }
}
