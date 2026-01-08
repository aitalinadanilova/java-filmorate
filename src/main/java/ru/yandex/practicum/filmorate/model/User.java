package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Data
@Validated
@EqualsAndHashCode
@Builder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;

    @Email
    @NotBlank(message = "Почта не может быть пустая")
    private String email;

    @NotBlank(message = "Login не должен быть пустым")
    @Pattern(regexp = "^[^ ]+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private List<Long> friends;

}
