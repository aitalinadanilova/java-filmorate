package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
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

    @Override
    public Film createFilm(Film film) {
        validateMpaAndGenres(film);
        log.info("Создание нового фильма: {}", film.getName());
        return filmStorage.createFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id фильма должен быть указан для обновления");
        }
        filmStorage.getFilm(film.getId());
        validateMpaAndGenres(film);

        log.info("Обновление фильма с id = {}", film.getId());
        return filmStorage.updateFilm(film);
    }

    @Override
    public Film getFilm(Long filmId) {
        return filmStorage.getFilm(filmId);
    }

    @Override
    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    @Override
    public void addLikeToFilm(Long filmId, Long userId) {
        checkFilmAndUserExist(filmId, userId);

        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLikeToFilm(Long filmId, Long userId) {
        checkFilmAndUserExist(filmId, userId);

        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    @Override
    public List<Film> getPopularFilms(Long count) {
        return filmStorage.getPopularFilms(count);
    }

    public List<Director> findDirectors() {
        return filmStorage.findDirectors();
    }

    public Director findDirectorById(Long directorId) {
        return filmStorage.findDirectorById(directorId);
    }

    public Director createDirector(Director director) {
        return filmStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        return filmStorage.updateDirector(director);
    }

    public void deleteDirectorById(Long directorId) {
        if (!filmStorage.deleteDirectorById(directorId)) {
            throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");
        }
    }

    public List<Film> findSortFilmsByDirector(Long directorId, String sortBy) {
        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new ValidationException("Параметр sortBy может быть только year или likes");
        }
        return filmStorage.findSortFilmsByDirector(directorId, sortBy);
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null && mpaStorage.getById(film.getMpa().getId()) == null) {
            throw new NotFoundException("Указанный MPA не найден");
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.checkGenresExists(film.getGenres());
        }
    }

    private void checkFilmAndUserExist(Long filmId, Long userId) {
        filmStorage.getFilm(filmId);
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}