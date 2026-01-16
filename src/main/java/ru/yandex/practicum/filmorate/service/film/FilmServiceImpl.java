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

    @Override
    public List<Director> findDirectors() {
        log.info("Получение списка всех режиссёров");
        return filmStorage.findDirectors();
    }

    @Override
    public Director findDirectorById(Long directorId) {
        log.info("Получение режиссёра с id = {}", directorId);
        Director director = filmStorage.findDirectorById(directorId);
        if (director == null) {
            throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");
        }
        return director;
    }

    @Override
    public Director createDirector(Director director) {
        log.info("Создание нового режиссёра: {}", director.getName());
        return filmStorage.createDirector(director);
    }

    @Override
    public Director updateDirector(Director director) {
        findDirectorById(director.getId());
        log.info("Обновление режиссёра с id = {}", director.getId());
        return filmStorage.updateDirector(director);
    }

    @Override
    public void deleteDirectorById(Long directorId) {
        log.info("Удаление режиссёра с id = {}", directorId);
        if (!filmStorage.deleteDirectorById(directorId)) {
            throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");
        }
    }

    @Override
    public List<Film> findSortFilmsByDirector(Long directorId, String sortBy) {
        findDirectorById(directorId);

        if (!"year".equalsIgnoreCase(sortBy) && !"likes".equalsIgnoreCase(sortBy)) {
            log.error("Неверный параметр сортировки: {}", sortBy);
            throw new ValidationException("Параметр sortBy может быть только year или likes");
        }

        log.info("Получение фильмов режиссёра {} с сортировкой по {}", directorId, sortBy);
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