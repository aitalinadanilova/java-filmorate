package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
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

    public Film createFilm(Film film) {
        if (mpaStorage.getById(film.getMpa().getId()) == null) {
            throw new ValidationException("Указанный MPA  не найден");
        }
        if (film.getGenres() != null) {
            genreStorage.checkGenresExists(film.getGenres());
        }
        log.info("Фильм {} создан", film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            log.info("Id должен быть указан");
            throw new NullPointerException("Id должен быть указан");
        }
        if (filmStorage.getFilm(film.getId()) != null) {
            return filmStorage.updateFilm(film);
        }
        log.info("Не найден фильм");
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
    }

    public Film getFilm(Long filmId) {
        if (filmStorage.getFilm(filmId) != null) {
            return filmStorage.getFilm(filmId);
        }
        throw new NotFoundException("Фильм с id = " + filmId + " не найден");
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLikeToFilm(Long filmId, Long userId) {
        if (filmStorage.getFilm(filmId) == null) {
            throw new NotFoundException("Фильм отсуствует");
        } else if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь отсутсвует");
        }
        filmStorage.checkLikeOnFilm(filmId,userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLikeToFilm(Long filmId, Long userId) {
        if (filmStorage.getFilm(filmId) == null) {
            throw new NotFoundException("Фильм с таким ID не найден!");
        } else if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с таким ID не найден!");
        }
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Long count) {
        return filmStorage.getPopularFilms(count);
    }

}
