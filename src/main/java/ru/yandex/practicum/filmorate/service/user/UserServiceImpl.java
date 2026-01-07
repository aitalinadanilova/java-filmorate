package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage storage;

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Не задано имя пользователя, будет использован логин {}", user.getLogin());
        }
        log.info("Пользователь создан с логином {}", user.getLogin());
        return storage.createUser(user);
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            log.info("Id пользователя должен быть указан");
            throw new NotFoundException("Id пользователя должен быть указан");
        }
        if (storage.getUser(user.getId()) != null) {
            log.info("Пользователь с id = {} обновлен", user.getId());
            return storage.updateUser(user);
        }
        throw new NotFoundException("Пользователь не найден с id = " + user.getId());
    }

    public User getUser(Long userId) {
        if (storage.getUser(userId) != null) {
            return storage.getUser(userId);
        }
        throw new NotFoundException("Пользователь не найден с id = " + userId);

    }

    public List<User> getAllUsers() {
        return storage.getAllUsers();
    }

    public void addToFriend(Long userId, Long friendId) {
        if (storage.getUser(userId) == null || storage.getUser(friendId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        storage.addFriends(userId,friendId);
        log.info("Пользователь {} стал другом пользователя {}",storage.getUser(userId),storage.getUser(friendId));
    }

    public void removeFromFriends(Long userId, Long friendId) {
        if (storage.getUser(userId) == null || storage.getUser(friendId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        storage.removeFriends(userId,friendId);
        log.info("Пользователи {} {} больше не друзья ",storage.getUser(userId),storage.getUser(friendId));
    }

    public List<User> getUsersFriends(Long userId) {
        if (storage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        log.info("Вот список друзей пользователя {} ", storage.getUser(userId));
        return storage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        if (storage.getUser(userId) == null || storage.getUser(friendId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return storage.getCommonFriends(userId,friendId);
    }
}
