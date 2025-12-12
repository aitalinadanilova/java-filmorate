package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    @Override
    public Film createFilm(Film film) {
        filmStorage.createFilm(film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        filmStorage.updateFilm(film);
        return film;
    }

    @Override
    public void addLike(long filmId, long userId) {
        Film film = getFilmById(filmId);

        userService.getUserById(userId);

        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        Film film = getFilmById(filmId);

        userService.getUserById(userId);
        if (!film.getLikes().contains(userId)) {
            throw new ValidationException(
                    "Пользователь " + userId + " не ставил лайк фильму " + filmId
            );
        }

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
        return filmStorage.getFilmById(id);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }
}
