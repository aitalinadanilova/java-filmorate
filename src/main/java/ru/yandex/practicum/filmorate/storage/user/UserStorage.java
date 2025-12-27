package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User createUser(User user);

    void updateUser(User user);

    void deleteUser(User user);

    User getUserById(long id);

    List<User> getAllUsers();

}
