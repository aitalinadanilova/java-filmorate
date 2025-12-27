package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmService {

    Film createFilm(FilmDto filmDto);

    Film updateFilm(FilmDto filmDto);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    List<Film> getTopFilms(int count);

    Film getFilmById(long id);

    Collection<Film> getAllFilms();

}
