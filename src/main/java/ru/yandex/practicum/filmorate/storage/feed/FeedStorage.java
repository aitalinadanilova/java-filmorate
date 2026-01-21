package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.feed.Feed;

import java.util.List;

public interface FeedStorage {
    Feed createFeed (Feed feed);

    List<Feed> getFeed(Long userId);

    void deleteFeed(Long eventId);
}
