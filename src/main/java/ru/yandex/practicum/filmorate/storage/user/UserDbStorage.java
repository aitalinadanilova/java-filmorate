package ru.yandex.practicum.filmorate.storage.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@AllArgsConstructor
@Component
@Slf4j
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper mapper;

    @Override
    public User createUser(User user) {
        String sqlQuery =
                "INSERT INTO users (email, login, name, birthday)values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sqlQuery =
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(
                sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public User getUser(Long id) {
        List<User> users = jdbcTemplate.query("SELECT " +
                "u.ID, " +
                "u.EMAIL, " +
                "u.LOGIN, " +
                "u.NAME, " +
                "u.BIRTHDAY, " +
                "f.USER2_ID " +
                "FROM USERS AS u " +
                "LEFT JOIN FRIENDS AS f ON (f.USER1_ID  = u.ID)" +
                "WHERE u.id = ?", mapper, id);
        if (users.size() == 0) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = jdbcTemplate.query("SELECT " +
                "u.ID, " +
                "u.EMAIL, " +
                "u.LOGIN, " +
                "u.NAME, " +
                "u.BIRTHDAY, " +
                "f.USER2_ID " +
                "FROM USERS u " +
                "LEFT JOIN FRIENDS f ON (f.USER1_ID  = u.ID)", mapper);
        Set<User> uniqueUser = new TreeSet<>(Comparator.comparing(User::getId));
        uniqueUser.addAll(users);
        return new ArrayList<>(uniqueUser);
    }

    @Override
    public void addFriends(Long userId, Long friendId) {
        jdbcTemplate.update("INSERT INTO friends (user1_id, user2_id, status)values (?, ?, ?)", userId, friendId, true);
    }

    @Override
    public void removeFriends(Long  userId, Long friendId) {
        jdbcTemplate.update("DELETE FROM friends WHERE user1_id = ? AND user2_id = ?", userId, friendId);
    }

    @Override
    public List<User> getFriends(Long id) {
        return jdbcTemplate.query("SELECT * FROM users WHERE id IN (SELECT user2_id FROM friends WHERE user1_id = ? AND status = true)", new DataClassRowMapper<>(User.class), id);
    }

    @Override
    public List<User> getCommonFriends(Long id, Long friendId) {
        return jdbcTemplate.query("SELECT * FROM users WHERE id IN (SELECT user2_id FROM friends WHERE user1_id = ? AND status = true AND user2_id IN ( SELECT user2_id FROM friends WHERE user1_id = ? AND status = true))", new DataClassRowMapper<>(User.class), id, friendId);

    }

}
