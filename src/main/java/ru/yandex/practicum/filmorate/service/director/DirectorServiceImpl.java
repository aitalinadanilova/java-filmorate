package ru.yandex.practicum.filmorate.service.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorServiceImpl implements DirectorService {

    private final DirectorStorage directorStorage;

    @Override
    public List<Director> findAll() {
        log.info("Получение списка всех режиссёров");
        return directorStorage.findAll();
    }

    @Override
    public Director findById(Long directorId) {
        log.info("Получение режиссёра с id = {}", directorId);
        Director director = directorStorage.findById(directorId);
        if (director == null) {
            throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");
        }
        return director;
    }

    @Override
    public Director create(Director director) {
        log.info("Создание нового режиссёра: {}", director.getName());
        return directorStorage.create(director);
    }

    @Override
    public Director update(Director director) {
        findById(director.getId());
        log.info("Обновление режиссёра с id = {}", director.getId());
        return directorStorage.update(director);
    }

    @Override
    public void delete(Long directorId) {
        log.info("Удаление режиссёра с id = {}", directorId);
        if (!directorStorage.deleteById(directorId)) {
            throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");
        }
    }
}