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
    void testCreateAndRetrieveUser() {
        User userToCreate = new User();
        userToCreate.setEmail("test@example.com");
        userToCreate.setLogin("testuser");
        userToCreate.setName("Test User");
        userToCreate.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userDbStorage.createUser(userToCreate);

        assertThat(createdUser.getId()).isNotNull();

        User userFromDb = userDbStorage.getUserById(createdUser.getId());

        assertThat(userFromDb)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", "test@example.com")
                .hasFieldOrPropertyWithValue("login", "testuser")
                .hasFieldOrPropertyWithValue("name", "Test User")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(2000, 1, 1));
    }

    @Test
    void testUpdateUser() {
        User userToCreate = new User();
        userToCreate.setEmail("initial@example.com");
        userToCreate.setLogin("initialuser");
        userToCreate.setName("Initial Name");
        userToCreate.setBirthday(LocalDate.of(1995, 5, 5));

        User createdUser = userDbStorage.createUser(userToCreate);

        // Обновляем данные пользователя
        createdUser.setEmail("updated@example.com");
        createdUser.setLogin("updateduser");
        createdUser.setName("Updated Name");
        createdUser.setBirthday(LocalDate.of(1999, 9, 9));

        userDbStorage.updateUser(createdUser);

        User updatedUser = userDbStorage.getUserById(createdUser.getId());

        assertThat(updatedUser)
                .hasFieldOrPropertyWithValue("email", "updated@example.com")
                .hasFieldOrPropertyWithValue("login", "updateduser")
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1999, 9, 9));
    }

    @Test
    void testDeleteUser() {
        User userToCreate = new User();
        userToCreate.setEmail("delete@example.com");
        userToCreate.setLogin("deleteuser");
        userToCreate.setName("Delete Me");
        userToCreate.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userDbStorage.createUser(userToCreate);
        Long userId = createdUser.getId();

        userDbStorage.deleteUser(createdUser);

        // Проверяем, что пользователь удалён
        assertThatThrownBy(() -> userDbStorage.getUserById(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id");
    }

    @Test
    void testRetrieveAllUsers() {
        // Создаём двух пользователей
        User firstUser = new User();
        firstUser.setEmail("user1@example.com");
        firstUser.setLogin("user1");
        firstUser.setName("User One");
        firstUser.setBirthday(LocalDate.of(1990, 1, 1));
        userDbStorage.createUser(firstUser);

        User secondUser = new User();
        secondUser.setEmail("user2@example.com");
        secondUser.setLogin("user2");
        secondUser.setName("User Two");
        secondUser.setBirthday(LocalDate.of(1992, 2, 2));
        userDbStorage.createUser(secondUser);

        List<User> allUsers = userDbStorage.getAllUsers();

        assertThat(allUsers)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting("email")
                .contains("user1@example.com", "user2@example.com");
    }
}
