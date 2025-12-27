package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.FilmDto;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidFilm() {
        FilmDto film = new FilmDto();
        film.setName("Interstellar");
        film.setDescription("Космическая драма");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);

        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Корректный фильм не должен вызывать ошибок");
    }

    @Test
    void testEmptyName() {
        FilmDto film = new FilmDto();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testEmptyDescription() {
        FilmDto film = new FilmDto();
        film.setName("Фильм");
        film.setDescription(" ");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void testReleaseDateInFuture() {
        FilmDto film = new FilmDto();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.now().plusDays(1)); // будущее
        film.setDuration(100);

        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate")));
    }

    @Test
    void testNegativeDuration() {
        FilmDto film = new FilmDto();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(-10);

        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("duration")));
    }

    @Test
    void testZeroDuration() {
        FilmDto film = new FilmDto();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(0);

        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("duration")));
    }
}

