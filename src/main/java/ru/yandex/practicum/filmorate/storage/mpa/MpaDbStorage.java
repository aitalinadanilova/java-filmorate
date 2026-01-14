package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@AllArgsConstructor
@Component
@Primary
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getAll() {
        return jdbcTemplate.query("SELECT * FROM rating_mpa", new DataClassRowMapper<>(Mpa.class));
    }

    @Override
    public Mpa getById(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM rating_mpa WHERE id = ?", new DataClassRowMapper<>(Mpa.class), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Mpa getMpaOfFilm(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM rating_mpa WHERE id IN (SELECT rating_mpa_id FROM films WHERE id = ?);", new DataClassRowMapper<>(Mpa.class), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }

    }

}
