package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @JsonProperty("reviewId")
    private Long id;

    private String content;

    @JsonProperty("isPositive")
    private Boolean isPositive;

    private Long userId;

    private Long filmId;

    private Long useful;

}
