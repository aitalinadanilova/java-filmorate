package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film getFilm(Long filmId);

    List<Film> getAllFilms();

    void addLikeToFilm(Long filmId, Long userId);

    void removeLikeToFilm(Long filmId, Long userId);

    List<Film> getPopularFilms(Long count);
}
