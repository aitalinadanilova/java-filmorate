package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final List<Film> films = new ArrayList<>();
    private int nextId = 1;

    @PostMapping
    public ResponseEntity<String> addFilm(@Valid @RequestBody Film film) {
        log.info("Попытка добавить фильм: {}", film.getName());
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание слишком длинное");
        }
        LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(firstFilmDate)) {
            throw new ValidationException("Дата релиза недопустима");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
        film.setId(nextId++);
        films.add(film);
        log.info("Фильм добавлен {}", film.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(film.getName());
    }

    @PutMapping
    public ResponseEntity<String> updateFilm(@Valid @RequestBody Film film) {
        log.info("Попытка обновить фильм: {}", film.getName());
        if (film.getId() == null || film.getId() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        for (int i = 0; i < films.size(); i++) {
            if (film.getName() == null || film.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
            if (film.getDescription() != null && film.getDescription().length() > 200) {
                throw new ValidationException("Описание слишком длинное");
            }
            LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
            if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(firstFilmDate)) {
                throw new ValidationException("Дата релиза недопустима");
            }
            if (film.getDuration() <= 0) {
                throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
            }
            films.set(i, film);
            return ResponseEntity.ok(film.getName());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Попытка вывести все фильмы ");
        return films;
    }
}
