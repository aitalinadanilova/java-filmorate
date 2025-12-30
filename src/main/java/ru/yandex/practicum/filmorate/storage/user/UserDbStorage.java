package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;


@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (resultSet, rowNum) -> {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));
        user.setBirthday(resultSet.getDate("birthday").toLocalDate());
        return user;
    };

    @Override
    public User createUser(User user) {
        String sqlInsert = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getLogin());
            preparedStatement.setString(3, user.getName());
            preparedStatement.setDate(4, Date.valueOf(user.getBirthday()));
            return preparedStatement;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public void updateUser(User user) {
        String sqlUpdate = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sqlUpdate,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
    }

    @Override
    public void deleteUser(User user) {
        String sqlDelete = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sqlDelete, user.getId());
    }

    @Override
    public User getUserById(long userId) {
        String sqlSelectById = "SELECT * FROM users WHERE id = ?";

        List<User> users = jdbcTemplate.query(sqlSelectById, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId);

        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public List<User> getAllUsers() {
        String sqlSelectAll = "SELECT * FROM users";
        return jdbcTemplate.query(sqlSelectAll, (resultSet, rowNumber) -> {
            User user = new User();
            user.setId(resultSet.getLong("id"));
            user.setEmail(resultSet.getString("email"));
            user.setLogin(resultSet.getString("login"));
            user.setName(resultSet.getString("name"));
            user.setBirthday(resultSet.getDate("birthday").toLocalDate());
            return user;
        });
    }

    @Override
    public void addFriend(long userId, long friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'CONFIRMED')";
        jdbcTemplate.update(sql, userId, friendId);
    }



    @Override
    public List<User> getFriends(long userId) {
        String sql = """
        SELECT u.*
        FROM users u
        JOIN friendships f ON u.id = f.friend_id
        WHERE f.user_id = ?
    """;

        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        String sql = """
        SELECT u.*
        FROM users u
        JOIN friendships f1 ON u.id = f1.friend_id
        JOIN friendships f2 ON u.id = f2.friend_id
        WHERE f1.user_id = ?
          AND f2.user_id = ?
    """;

        return jdbcTemplate.query(sql, userRowMapper, userId, otherUserId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );
        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                friendId, userId
        );
    }
}
