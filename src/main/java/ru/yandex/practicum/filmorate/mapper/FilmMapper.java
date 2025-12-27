package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.dto.FilmDto;

import java.util.stream.Collectors;

public class FilmMapper {

    public static Film toModel(FilmDto dto) {
        if (dto == null) return null;

        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getGenreIds() != null) {
            film.setGenres(dto.getGenreIds().stream()
                    .map(id -> new Genre(id, null))
                    .collect(Collectors.toSet()));
        }

        if (dto.getMpaId() != null) {
            film.setMpa(MpaRating.fromId(dto.getMpaId()));
        }

        return film;
    }

    public static FilmDto toDto(Film film) {
        if (film == null) return null;

        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getGenres() != null) {
            dto.setGenreIds(film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet()));
        }

        if (film.getMpa() != null) {
            dto.setMpaId(film.getMpa().getId());
        }

        if (film.getLikes() != null) {
            dto.setLikes(film.getLikes());
        }

        return dto;
    }

}
