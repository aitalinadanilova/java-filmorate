package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import jakarta.validation.constraints.NotBlank;

@Builder
@Data
public class Director {

    @NonNull
    private Long id;

    @NotBlank(message = "Ошибка!Имя режиссера не может быть пустым.")
    private final String name;

}
