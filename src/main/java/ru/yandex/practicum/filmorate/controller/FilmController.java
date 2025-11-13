package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final List<Film> films = new ArrayList<>();
    private int nextId = 1;

    @PostMapping
    public ResponseEntity<String> addFilm(@Valid @RequestBody Film film) {
        log.info("Попытка добавить фильм: {}", film.getName());
        validateFilm(film);

        film.setId(nextId++);
        films.add(film);

        log.info("Фильм добавлен: {}", film.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(film.getName());
    }

    @PutMapping
    public ResponseEntity<String> updateFilm(@Valid @RequestBody Film film) {
        log.info("Попытка обновить фильм: {}", film.getName());

        if (film.getId() == null || film.getId() <= 0) {
            log.warn("Некорректный ID при обновлении фильма: {}", film.getId());
            return ResponseEntity.badRequest().body("Некорректный ID фильма");
        }

        validateFilm(film);

        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getId().equals(film.getId())) {
                films.set(i, film);
                log.info("Фильм с id={} обновлён: {}", film.getId(), film.getName());
                return ResponseEntity.ok(film.getName());
            }
        }

        log.warn("Фильм с id={} не найден", film.getId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Фильм не найден");
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Запрошен список всех фильмов ({} шт.)", films.size());
        return films;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может превышать 200 символов");
        }
        LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(firstFilmDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
