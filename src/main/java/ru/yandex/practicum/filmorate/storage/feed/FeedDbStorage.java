package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.feed.Feed;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Component
@Slf4j
@Primary
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FeedRowMapper rowMapper;

    @Override
    public void createFeed(Feed feed) {
        log.info("Добавление нового события пользователя {}: {} - {}", feed.getUserId(), feed.getEventType(), feed.getOperation());
        String sql = "INSERT INTO feed (timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, feed.getTimestamp());
            ps.setLong(2, feed.getUserId());
            ps.setString(3, feed.getEventType().toString());
            ps.setString(4, feed.getOperation().toString());
            ps.setLong(5, feed.getEntityId());
            return ps;
        }, keyHolder);
    }

    @Override
    public List<Feed> getFeed(Long userId) {
        String sql = "SELECT * FROM feed WHERE user_id = ?";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    @Override
    public void deleteFeed(Long eventId) {
        log.info("Удаление объекта feed с eventId: {}", eventId);
        jdbcTemplate.update("DELETE FROM feed WHERE event_id = ?", eventId);
    }
}
