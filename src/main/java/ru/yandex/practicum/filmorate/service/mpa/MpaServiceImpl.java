package ru.yandex.practicum.filmorate.service.mpa;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Arrays;
import java.util.Collection;

@Service
public class MpaServiceImpl implements MpaService {

    @Override
    public Collection<MpaRating> getAllMpa() {
        return Arrays.asList(MpaRating.values());
    }

    @Override
    public MpaRating getMpaById(int id) {
        return MpaRating.fromId(id);
    }
}