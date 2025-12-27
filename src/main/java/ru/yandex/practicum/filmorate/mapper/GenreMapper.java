package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

public class GenreMapper {

    public static Genre toModel(GenreDto dto) {
        if (dto == null) return null;
        return new Genre(dto.getId(), dto.getName());
    }

    public static GenreDto toDto(Genre genre) {
        if (genre == null) return null;
        return new GenreDto(genre.getId(), genre.getName());
    }
}