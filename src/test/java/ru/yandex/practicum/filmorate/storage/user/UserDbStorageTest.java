package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    void contextLoads() {
        assertThat(userDbStorage).isNotNull();
    }

    @Test
    void testCreateAndGetUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userDbStorage.createUser(user);

        assertThat(created.getId()).isNotNull();

        User fromDb = userDbStorage.getUserById(created.getId());

        assertThat(fromDb)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", "test@example.com")
                .hasFieldOrPropertyWithValue("login", "testuser")
                .hasFieldOrPropertyWithValue("name", "Test User")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(2000, 1, 1));
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("initial@example.com");
        user.setLogin("initialuser");
        user.setName("Initial Name");
        user.setBirthday(LocalDate.of(1995, 5, 5));

        User created = userDbStorage.createUser(user);

        // Обновляем пользователя
        created.setEmail("updated@example.com");
        created.setLogin("updateduser");
        created.setName("Updated Name");
        created.setBirthday(LocalDate.of(1999, 9, 9));

        userDbStorage.updateUser(created);

        User updated = userDbStorage.getUserById(created.getId());

        assertThat(updated)
                .hasFieldOrPropertyWithValue("email", "updated@example.com")
                .hasFieldOrPropertyWithValue("login", "updateduser")
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1999, 9, 9));
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("delete@example.com");
        user.setLogin("deleteuser");
        user.setName("Delete Me");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userDbStorage.createUser(user);
        Long id = created.getId();

        userDbStorage.deleteUser(created);

        // Проверяем, что пользователь удален
        assertThatThrownBy(() -> userDbStorage.getUserById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id");
    }

    @Test
    void testGetAllUsers() {
        // Создаем двух пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userDbStorage.createUser(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        userDbStorage.createUser(user2);

        List<User> users = userDbStorage.getAllUsers();

        assertThat(users)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting("email")
                .contains("user1@example.com", "user2@example.com");
    }
}
