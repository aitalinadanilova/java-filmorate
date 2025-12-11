package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {
    User createUser(User user);

    User updateUser(User user);

    User getUserById(long id);

    List<User> getAllUsers();

    void addFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    List<User> getCommonFriends(long userId, long otherUserId);

}
