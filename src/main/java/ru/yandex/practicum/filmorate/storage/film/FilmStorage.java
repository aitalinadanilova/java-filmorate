package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    void createFilm(Film film);

    void deleteFilm(Film film);

    Collection<Film> getAllFilms();

    void updateFilm(Film film);

    Film getFilmById(long id);
}
