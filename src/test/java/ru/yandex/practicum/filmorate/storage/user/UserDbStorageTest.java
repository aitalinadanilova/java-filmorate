package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testCreateAndFindUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        userStorage.createUser(user);

        User retrieved = userStorage.getUserById(user.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testUpdateUser() {
        User user = userStorage.getUserById(1);
        user.setName("Updated Name");
        userStorage.updateUser(user);

        User updated = userStorage.getUserById(1);
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("delete@example.com");
        user.setLogin("deleteuser");
        user.setName("Delete User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        userStorage.createUser(user);
        long id = user.getId();

        userStorage.deleteUser(user);

        Optional<User> deleted = Optional.ofNullable(userStorage.getUserById(id));
        assertThat(deleted).isEmpty();
    }

    @Test
    void testGetAllUsers() {
        List<User> users = userStorage.getAllUsers();
        assertThat(users).isNotEmpty();
    }
}