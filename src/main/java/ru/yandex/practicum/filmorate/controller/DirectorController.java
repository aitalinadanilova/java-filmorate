package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@Slf4j
@RequiredArgsConstructor
public class DirectorController {
    private final FilmService service;

    @GetMapping
    public List<Director> findAll() {
        log.info("Получен запрос GET /directors");
        return service.findDirectors();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable Long id) {
        log.info("Получен запрос GET /directors/{}", id);
        return service.findDirectorById(id);
    }

    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        log.info("Получен запрос POST /directors: {}", director);
        return service.createDirector(director);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        log.info("Получен запрос PUT /directors: {}", director);
        return service.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Получен запрос DELETE /directors/{}", id);
        service.deleteDirectorById(id);
    }

}
