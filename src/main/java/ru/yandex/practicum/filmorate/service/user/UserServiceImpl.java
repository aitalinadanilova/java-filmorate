package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    public UserServiceImpl(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User createUser(User user) {
        boolean emailExists = userStorage.getAllUsers().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        boolean loginExists = userStorage.getAllUsers().stream()
                .anyMatch(u -> u.getLogin().equalsIgnoreCase(user.getLogin()));
        if (loginExists) {
            throw new ValidationException("Пользователь с таким login уже существует");
        }

        return userStorage.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        getUserById(user.getId());
        userStorage.updateUser(user);
        return user;
    }

    @Override
    public User getUserById(long id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    @Override
    public void addFriend(long userId, long friendId) {
        userStorage.addFriend(userId, friendId);
    }


    @Override
    public void removeFriend(long userId, long friendId) {
        getUserById(userId);
        getUserById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    @Override
    public List<User> getFriends(long userId) {
        getUserById(userId);
        return userStorage.getFriends(userId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        getUserById(userId);
        getUserById(otherUserId);
        return userStorage.getCommonFriends(userId, otherUserId);
    }
}
