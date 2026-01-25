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

    void addLike(Long id, Long userId);

    void removeLike(Long id, Long userId);

    List<User> getLikes(Long filmId);

    List<Film> findSortFilmsByDirector(Long directorId, String sortBy);

    List<Film> getPopularFilms(Long count, Long genreId, Integer year);

    List<Film> getFilmsByIds(List<Long> filmIds);

    List<Film> searchFilms(String query, boolean byTitle, boolean byDirector);

    List<Film> getCommonFilms(Long userId, Long friendId);

    void deleteFilm(Long filmId);
}
