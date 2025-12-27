package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final @Qualifier("genreDbStorage") GenreStorage genreStorage;

    @GetMapping
    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable long id) {
        return genreStorage.getGenreById(id);
    }
}
