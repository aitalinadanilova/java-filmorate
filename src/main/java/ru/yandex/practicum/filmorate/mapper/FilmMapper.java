package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dto.FilmDto;

import java.util.HashSet;
import java.util.Set;
public class FilmMapper {

    public static Film toModel(FilmDto dto) {
        if (dto == null) {
            return null;
        }

        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        film.setMpa(dto.getMpa());

        film.setGenres(
                dto.getGenres() != null
                        ? new HashSet<>(dto.getGenres())
                        : new HashSet<>()
        );

        return film;
    }

    public static FilmDto toDto(Film film) {
        if (film == null) {
            return null;
        }

        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        dto.setMpa(film.getMpa());
        dto.setGenres(
                dto.getGenres() != null
                        ? new HashSet<>(dto.getGenres())
                        : new HashSet<>()
        );

        return dto;
    }
}
