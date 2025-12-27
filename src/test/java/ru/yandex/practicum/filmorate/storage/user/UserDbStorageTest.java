package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        UserDto dto = new UserDto();
        dto.setEmail("test@example.com");
        dto.setLogin("testuser");
        dto.setName("Test User");
        dto.setBirthday(LocalDate.of(2000, 1, 1));

        User user = UserMapper.toModel(dto);
        User createdUser = userDbStorage.createUser(user);

        assertThat(createdUser.getId()).isNotNull();

        User fromDb = userDbStorage.getUserById(createdUser.getId());

        assertThat(fromDb)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", "test@example.com")
                .hasFieldOrPropertyWithValue("login", "testuser")
                .hasFieldOrPropertyWithValue("name", "Test User")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(2000, 1, 1));
    }

    @Test
    void testUpdateUser() {
        UserDto dto = new UserDto();
        dto.setEmail("initial@example.com");
        dto.setLogin("initialuser");
        dto.setName("Initial Name");
        dto.setBirthday(LocalDate.of(1995, 5, 5));

        User user = UserMapper.toModel(dto);
        User createdUser = userDbStorage.createUser(user);

        dto.setId(createdUser.getId());
        dto.setEmail("updated@example.com");
        dto.setLogin("updateduser");
        dto.setName("Updated Name");
        dto.setBirthday(LocalDate.of(1999, 9, 9));

        User updatedUserModel = UserMapper.toModel(dto);
        userDbStorage.updateUser(updatedUserModel);

        User fromDb = userDbStorage.getUserById(createdUser.getId());

        assertThat(fromDb)
                .hasFieldOrPropertyWithValue("email", "updated@example.com")
                .hasFieldOrPropertyWithValue("login", "updateduser")
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1999, 9, 9));
    }

    @Test
    void testDeleteUser() {
        UserDto dto = new UserDto();
        dto.setEmail("delete@example.com");
        dto.setLogin("deleteuser");
        dto.setName("Delete Me");
        dto.setBirthday(LocalDate.of(1990, 1, 1));

        User user = UserMapper.toModel(dto);
        User createdUser = userDbStorage.createUser(user);
        Long userId = createdUser.getId();

        userDbStorage.deleteUser(createdUser);
        User fromDb = userDbStorage.getUserById(userId);
        assertThat(fromDb).isNull();
    }

    @Test
    void testRetrieveAllUsers() {
        UserDto dto1 = new UserDto();
        dto1.setEmail("user1@example.com");
        dto1.setLogin("user1");
        dto1.setName("User One");
        dto1.setBirthday(LocalDate.of(1990, 1, 1));

        UserDto dto2 = new UserDto();
        dto2.setEmail("user2@example.com");
        dto2.setLogin("user2");
        dto2.setName("User Two");
        dto2.setBirthday(LocalDate.of(1992, 2, 2));

        User user1 = UserMapper.toModel(dto1);
        User user2 = UserMapper.toModel(dto2);

        userDbStorage.createUser(user1);
        userDbStorage.createUser(user2);

        List<User> allUsers = userDbStorage.getAllUsers();

        assertThat(allUsers)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting("email")
                .contains("user1@example.com", "user2@example.com");
    }
}
