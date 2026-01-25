package ru.yandex.practicum.filmorate.service.director;

import ru.yandex.practicum.filmorate.model.Director;
import java.util.List;

public interface DirectorService {

    List<Director> findAll();

    Director findById(Long id);

    Director create(Director director);

    Director update(Director director);

    void delete(Long id);

}
