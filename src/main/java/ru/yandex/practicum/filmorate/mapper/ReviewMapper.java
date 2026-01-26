package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.Review;

public class ReviewMapper {
    private ReviewMapper() {
    }

    public static Review map(Review review) {
        System.out.println("Processing Review: " + review);
        return review;
    }

}