package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public FilmDto create(@Valid @RequestBody FilmDto filmDto) {
        Film saved = filmService.createFilm(filmDto);
        return FilmMapper.toDto(saved);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody FilmDto filmDto) {
        Film updated = filmService.updateFilm(filmDto);
        return FilmMapper.toDto(updated);
    }

    @GetMapping("/{id}")
    public FilmDto getFilm(@PathVariable Long id) {
        Film film = filmService.getFilmById(id);
        return FilmMapper.toDto(film);
    }

    @GetMapping
    public Collection<FilmDto> getAllFilms() {
        return filmService.getAllFilms().stream()
                .map(FilmMapper::toDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getTopFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getTopFilms(count).stream()
                .map(FilmMapper::toDto)
                .collect(Collectors.toList());
    }

}
