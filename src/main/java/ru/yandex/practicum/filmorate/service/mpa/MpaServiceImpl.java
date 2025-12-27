package ru.yandex.practicum.filmorate.service.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaServiceImpl implements MpaService {

    private final MpaStorage mpaStorage;

    @Override
    public Collection<MpaRating> getAllMpa() {
        return mpaStorage.getAll();
    }

    @Override
    public MpaRating getMpaById(int id) {
        MpaRating mpa = mpaStorage.getById(id);
        if (mpa == null) {
            throw new NotFoundException("MPA not found");
        }
        return mpa;
    }
}