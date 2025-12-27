package ru.yandex.practicum.filmorate.service.genre;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GenreServiceImpl implements GenreService {

    private final Map<Long, Genre> genres = Arrays.asList(
            new Genre(1L, "Комедия"),
            new Genre(2L, "Драма"),
            new Genre(3L, "Мультфильм"),
            new Genre(4L, "Триллер"),
            new Genre(5L, "Документальный")
    ).stream().collect(Collectors.toMap(Genre::getId, g -> g));

    @Override
    public Collection<Genre> getAllGenres() {
        return genres.values();
    }

    @Override
    public Genre getGenreById(long id) {
        if (!genres.containsKey(id)) {
            throw new RuntimeException("Genre not found with id: " + id);
        }
        return genres.get(id);
    }
}