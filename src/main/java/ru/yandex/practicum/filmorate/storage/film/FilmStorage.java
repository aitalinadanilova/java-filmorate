package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FilmStorage {

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film getFilm(Long filmId);

    List<Film> getAllFilms();

    List<Film> getPopularFilms(Long count);

    void addLike(Long id, Long userId);

    void removeLike(Long id, Long userId);

    List<User> getLikes(Long filmId);

    List<Director> findDirectors();

    Director findDirectorById(Long directorId);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    boolean deleteDirectorById(Long directorId);

    List<Film> findSortFilmsByDirector(Long directorId, String sortBy);

}
