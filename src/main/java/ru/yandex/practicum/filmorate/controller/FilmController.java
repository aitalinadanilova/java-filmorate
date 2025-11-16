package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        log.info("Попытка добавить фильм: {}", film.getName());
        validateFilm(film);

        film.setId(nextId++);
        films.put(film.getId(), film);

        log.info("Фильм добавлен: {}", film.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(film);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        log.info("Попытка обновить фильм: {}", film.getName());

        if (film.getId() == null || film.getId() <= 0) {
            log.warn("Некорректный ID при обновлении фильма: {}", film.getId());
            throw new ValidationException("Некорректный ID фильма");
        }

        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        validateFilm(film);

        films.put(film.getId(), film);
        log.info("Фильм обновлён: {}", film.getName());

        return ResponseEntity.ok(film);
    }

    @GetMapping
    public Map<Integer, Film> getAllFilms() {
        log.info("Запрошен список всех фильмов ({} шт.)", films.size());
        return films;
    }

    private void validateFilm(Film film) {
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может превышать 200 символов");
        }
        LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(firstFilmDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
