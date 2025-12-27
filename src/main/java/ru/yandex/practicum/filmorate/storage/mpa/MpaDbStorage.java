package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Component
@Qualifier("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<MpaRating> getAll() {
        String sql = "SELECT id FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> MpaRating.fromId(rs.getInt("id")));
    }

    public MpaRating getById(long id) {
        String sql = "SELECT id FROM mpa WHERE id = ?";
        List<MpaRating> list = jdbcTemplate.query(sql,
                (rs, rowNum) -> MpaRating.fromId(rs.getInt("id")),
                id);

        return list.isEmpty() ? null : list.get(0);
    }

}
