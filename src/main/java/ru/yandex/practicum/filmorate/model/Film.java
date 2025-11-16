package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    private Integer id;

    @NotNull(message = "Название фильма обязательно")
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @NotNull(message = "Описание фильма обязательно")
    @NotBlank(message = "Описание фильма не может быть пустым")
    @Size(max = 200, message = "Описание фильма не может превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность фильма обязательна")
    @Positive(message = "Продолжительность должна быть положительной")
    private Integer duration;
}
