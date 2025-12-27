package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

public class MpaMapper {

    public static MpaRating toModel(MpaDto dto) {
        if (dto == null) return null;
        return MpaRating.fromId(dto.getId());
    }

    public static MpaDto toDto(MpaRating mpaRating) {
        if (mpaRating == null) return null;
        return new MpaDto(mpaRating.getId(), mpaRating.getName());
    }
}
