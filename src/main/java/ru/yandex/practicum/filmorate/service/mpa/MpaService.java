package ru.yandex.practicum.filmorate.service.mpa;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;

public interface MpaService {

    Collection<MpaRating> getAllMpa();

    MpaRating getMpaById(int id);

}