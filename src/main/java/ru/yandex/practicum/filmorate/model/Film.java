package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.validation.BeforeDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@EqualsAndHashCode
@Builder(toBuilder = true)
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200)
    @NotBlank(message = "Описание фильма не может быть пустым")
    private String description;

    @BeforeDate
    private LocalDate releaseDate;

    @Positive
    private Integer duration;

    private List<Long> likes = new ArrayList<>();

    private List<Genre> genres = new ArrayList<>();

    @NonNull
    private Mpa mpa;

    private List<Director> directors = new ArrayList<>();

}
