package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//import jakarta.validation.constraints.NotBlank;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Director {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    //@NotBlank(message = "Ошибка! Имя режиссера не может быть пустым.")
    private String name;

}