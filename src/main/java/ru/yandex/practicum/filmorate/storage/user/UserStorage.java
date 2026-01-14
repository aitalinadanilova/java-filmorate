package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User user);

    User getUser(Long id);

    List<User> getAllUsers();

    void addFriends(Long userId, Long friendId);

    void removeFriends(Long userId, Long friendId);

    List<User> getFriends(Long id);

    List<User> getCommonFriends(Long id, Long friendId);

}
