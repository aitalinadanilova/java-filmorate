package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;

public interface UserService {

    User createUser(User user);

    User updateUser(User user);

    User getUser(Long userId);

    List<User> getAllUsers();

    void addToFriend(Long userId, Long friendId);

    void removeFromFriends(Long userId, Long friendId);

    List<User> getUsersFriends(Long userId);

    List<User> getCommonFriends(Long userId, Long friendId);

}
