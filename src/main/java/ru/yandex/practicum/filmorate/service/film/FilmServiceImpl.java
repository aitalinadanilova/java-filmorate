package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final UserStorage userStorage;


    @Override
    public Film createFilm(Film film) {
        if (mpaStorage.getById(film.getMpa().getId()) == null) {
            throw new NotFoundException("Указанный MPA не найден");
        }

        if (film.getGenres() != null) {
            genreStorage.checkGenresExists(film.getGenres());
        }

        log.info("Фильм {} создан", film);
        return filmStorage.createFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new NullPointerException("Id должен быть указан");
        }

        Film existingFilm = filmStorage.getFilm(film.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

        return filmStorage.updateFilm(film);
    }

    @Override
    public Film getFilm(Long filmId) {
        Film film = filmStorage.getFilm(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    @Override
    public void addLikeToFilm(Long filmId, Long userId) {
        Film film = filmStorage.getFilm(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм отсутствует");
        }

        User user = userStorage.getUser(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь отсутствует");
        }

        filmStorage.checkLikeOnFilm(filmId, userId);
        filmStorage.addLike(filmId, userId);
    }

    @Override
    public void removeLikeToFilm(Long filmId, Long userId) {
        Film film = filmStorage.getFilm(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с таким ID не найден!");
        }

        User user = userStorage.getUser(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с таким ID не найден!");
        }

        filmStorage.removeLike(filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(Long count, Long genreId, Integer year) {
        return filmStorage.getPopularFilms(count, genreId, year);
    }
}

