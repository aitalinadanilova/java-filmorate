package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Integer id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неправильный формат Email")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^[^ ]+$", message = "Логин не может содержать пробелы")
    private String login;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
