package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> findAll() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, (rs, n) ->
                Director.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build());
    }

    @Override
    public Director findById(Long id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql,
                    (rs, n) -> Director.builder()
                            .id(rs.getLong("id"))
                            .name(rs.getString("name"))
                            .build(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссёр с id=" + id + " не найден");
        }
    }

    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, kh);
        director.setId(kh.getKey().longValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql, director.getName(), director.getId());
        if (rows == 0) {
            throw new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
        }
        return director;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
