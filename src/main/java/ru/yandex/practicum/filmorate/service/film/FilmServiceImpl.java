package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Operation;
import ru.yandex.practicum.filmorate.service.director.DirectorService;
import ru.yandex.practicum.filmorate.service.feed.FeedService;
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
    private final FeedService feedService;
    private final DirectorService directorService;

    @Override
    public Film createFilm(Film film) {
        validateDependencies(film);
        log.info("Создание нового фильма: {}", film.getName());
        return filmStorage.createFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id фильма должен быть указан для обновления");
        }
        getFilm(film.getId());

        validateDependencies(film);
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
        feedService.createFeed(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    @Override
    public void removeLikeToFilm(Long filmId, Long userId) {
        checkFilmAndUserExist(filmId, userId);

        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
        feedService.createFeed(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    @Override
    public List<Film> getPopularFilms(Long count, Long genreId, Integer year) {
        return filmStorage.getPopularFilms(count, genreId, year);
    }

    @Override
    public List<Film> findSortFilmsByDirector(Long directorId, String sortBy) {
        directorService.findById(directorId);

        if (!"year".equalsIgnoreCase(sortBy) && !"likes".equalsIgnoreCase(sortBy)) {
            log.error("Неверный параметр сортировки: {}", sortBy);
            throw new ValidationException("Параметр sortBy может быть только year или likes");
        }

        log.info("Получение фильмов режиссёра {} с сортировкой по {}", directorId, sortBy);
        return filmStorage.findSortFilmsByDirector(directorId, sortBy);
    }

    private void validateDependencies(Film film) {
        if (film.getMpa() != null && mpaStorage.getById(film.getMpa().getId()) == null) {
            throw new NotFoundException("Указанный MPA не найден");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.checkGenresExists(film.getGenres());
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                directorService.findById(director.getId());
            }
        }
    }

    private void checkFilmAndUserExist(Long filmId, Long userId) {
        filmStorage.getFilm(filmId);
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    public List<Film> searchFilms(String query, String by) {
        String[] searchParams = by.split(",");
        boolean searchByTitle = false;
        boolean searchByDirector = false;

        for (String param : searchParams) {
            if (param.equalsIgnoreCase("title")) searchByTitle = true;
            if (param.equalsIgnoreCase("director")) searchByDirector = true;
        }

        return filmStorage.searchFilms(query, searchByTitle, searchByDirector);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        // Проверяем существование пользователей
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (userStorage.getUser(friendId) == null) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        log.info("Получение общих фильмов для пользователей {} и {}", userId, friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    @Override
    public void deleteFilm(Long filmId) {
        filmStorage.getFilm(filmId);

        log.info("Удаление фильма с id={}", filmId);
        filmStorage.deleteFilm(filmId);
    }
}