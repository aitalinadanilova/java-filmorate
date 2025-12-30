package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;
    private final Set<Friendship> friendships = new HashSet<>();


    @Override
    public User createUser(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void updateUser(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        users.put(user.getId(), user);
    }

    @Override
    public void deleteUser(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        users.remove(user.getId());
    }

    @Override
    public User getUserById(long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriend(long userId, long friendId) {
        getUserById(userId);
        getUserById(friendId);

        boolean exists1 = friendships.stream()
                .anyMatch(f -> f.getUserId().equals(userId) && f.getFriendId().equals(friendId));
        if (!exists1) {
            friendships.add(new Friendship(userId, friendId, FriendshipStatus.CONFIRMED));
        }

        boolean exists2 = friendships.stream()
                .anyMatch(f -> f.getUserId().equals(friendId) && f.getFriendId().equals(userId));
        if (!exists2) {
            friendships.add(new Friendship(friendId, userId, FriendshipStatus.CONFIRMED));
        }
    }


    @Override
    public void removeFriend(long userId, long friendId) {
        friendships.removeIf(f ->
                (f.getUserId().equals(userId) && f.getFriendId().equals(friendId)) ||
                        (f.getUserId().equals(friendId) && f.getFriendId().equals(userId))
        );
    }


    @Override
    public List<User> getFriends(long userId) {
        return friendships.stream()
                .filter(f -> f.getUserId().equals(userId))
                .map(f -> getUserById(f.getFriendId()))
                .toList();
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        Set<Long> first = friendships.stream()
                .filter(f -> f.getUserId().equals(userId))
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());

        return friendships.stream()
                .filter(f -> f.getUserId().equals(otherUserId))
                .map(Friendship::getFriendId)
                .filter(first::contains)
                .map(this::getUserById)
                .toList();
    }

}
