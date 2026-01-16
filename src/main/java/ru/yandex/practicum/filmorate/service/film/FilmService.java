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

    List<Film> getPopularFilms(Long count);

    List<Director> findDirectors();

    Director findDirectorById(Long id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirectorById(Long id);

    List<Film> findSortFilmsByDirector(Long directorId, String sortBy);

}
