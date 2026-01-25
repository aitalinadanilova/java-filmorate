package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import java.util.List;

public interface DirectorStorage {

    List<Director> findAll();

    Director findById(Long directorId);

    Director create(Director director);

    Director update(Director director);

    boolean deleteById(Long directorId);

}
