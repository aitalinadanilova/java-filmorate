package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film getFilm(Long filmId);

    List<Film> getAllFilms();

    void addLikeToFilm(Long filmId, Long userId);

    void removeLikeToFilm(Long filmId, Long userId);

    List<Film> findSortFilmsByDirector(Long directorId, String sortBy);

    List<Film> getPopularFilms(Long count, Long  genreId, Integer year);

    List<Film> searchFilms(String query, String by);

    List<Film> getCommonFilms(Long userId, Long friendId);

    void deleteFilm(Long filmId);
}
