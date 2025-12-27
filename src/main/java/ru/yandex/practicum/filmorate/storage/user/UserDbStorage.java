package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Component
@Qualifier("UserDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

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
        return jdbcTemplate.query(sqlSelectById, (resultSet, rowNumber) -> {
                    User user = new User();
                    user.setId(resultSet.getLong("id"));
                    user.setEmail(resultSet.getString("email"));
                    user.setLogin(resultSet.getString("login"));
                    user.setName(resultSet.getString("name"));
                    user.setBirthday(resultSet.getDate("birthday").toLocalDate());
                    return user;
                }, userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
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
}
