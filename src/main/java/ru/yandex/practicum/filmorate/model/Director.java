package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import jakarta.validation.constraints.NotBlank;

@Builder
@Data
public class Director {
    @JsonProperty("id")
    @NonNull
    private Long id;

    @JsonProperty("name")
    @NotBlank(message = "Ошибка!Имя режиссера не может быть пустым.")
    private final String name;

}
