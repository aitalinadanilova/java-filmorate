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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен: {}", film.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(film);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Фильм обновлён: {}", film.getName());
        return ResponseEntity.ok(film);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    private void validateFilm(Film film) {
        LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(firstFilmDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}

