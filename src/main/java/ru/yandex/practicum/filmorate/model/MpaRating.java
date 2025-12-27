package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MpaRating {
    G(1, "G"),
    PG(2, "PG"),
    PG_13(3, "PG-13"),
    R(4, "R"),
    NC_17(5, "NC-17");

    private final int id;
    private final String name;

    public static MpaRating fromId(int id) {
        return Arrays.stream(MpaRating.values())
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("MPA rating not found with id: " + id));
    }
}
