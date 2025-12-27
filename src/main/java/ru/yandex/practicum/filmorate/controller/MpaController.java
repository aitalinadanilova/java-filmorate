package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final @Qualifier("mpaDbStorage") MpaDbStorage mpaStorage;

    @GetMapping
    public List<MpaRating> getAllMpa() {
        return mpaStorage.getAll();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaById(@PathVariable long id) {
        MpaRating mpa = mpaStorage.getById(id);
        if (mpa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MPA not found");
        }
        return mpa;
    }
}
