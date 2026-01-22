package ru.yandex.practicum.filmorate.service.feed;

import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.Operation;

import java.util.List;

public interface FeedService {
    void createFeed(Long userId, EventType eventType, Operation operation, Long entityId);

    List<Feed> getFeed(Long userId);

    void deleteFeed(Long eventId);
}
