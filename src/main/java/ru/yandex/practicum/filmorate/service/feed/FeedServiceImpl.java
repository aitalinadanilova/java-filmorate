package ru.yandex.practicum.filmorate.service.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.Operation;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    @Override
    public void createFeed(Long userId, EventType eventType, Operation operation, Long entityId) {
        log.info("Создание объекта feed: userId {}, eventType {}, operation {}, entityId {}", userId, eventType, operation, entityId);
        Feed feed = Feed.builder().timestamp(System.currentTimeMillis()).userId(userId).eventType(eventType).operation(operation).entityId(entityId).build();

        feedStorage.createFeed(feed);
    }

    @Override
    public List<Feed> getFeed(Long userId) {
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return feedStorage.getFeed(userId);
    }
}
