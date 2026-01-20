package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewDto {

    @JsonProperty("reviewId")
    private Long reviewId;

    private String content;

    @JsonProperty("isPositive")
    private boolean isPositive;

    private long userId;
    private long filmId;
    private long useful;
}
