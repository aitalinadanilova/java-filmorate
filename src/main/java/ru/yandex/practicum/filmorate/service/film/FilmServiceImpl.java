package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmServiceImpl(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                           UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    @Override
    public Film createFilm(FilmDto filmDto) {
        Film film = FilmMapper.toModel(filmDto);
        filmStorage.createFilm(film);
        return film;
    }

    @Override
    public Film updateFilm(FilmDto filmDto) {
        Film film = FilmMapper.toModel(filmDto);
        filmStorage.updateFilm(film);
        return film;
    }

    @Override
    public void addLike(long filmId, long userId) {
        Film film = getFilmModelById(filmId);
        userService.getUserById(userId);
        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        Film film = getFilmModelById(filmId);
        userService.getUserById(userId);
        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Film getFilmById(long id) {
        return getFilmModelById(id);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    private Film getFilmModelById(long id) {
        return filmStorage.getFilmById(id);
    }

}
