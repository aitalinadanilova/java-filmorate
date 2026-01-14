package ru.yandex.practicum.filmorate.service.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaServiceImpl implements MpaService {
    private final MpaStorage storage;

    public List<Mpa> getAll() {
        return storage.getAll();
    }

    public Mpa getById(Long id) {
        if (storage.getById(id) == null) {
            throw new NotFoundException("Рейтинга с таким id = " + id + " нет");
        } else {
            return storage.getById(id);
        }

    }
}
