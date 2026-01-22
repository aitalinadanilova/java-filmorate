package ru.yandex.practicum.filmorate.model.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feed {
    Long timestamp;
    Long userId;
    EventType eventType;
    Operation operation;
    Long eventId;
    Long entityId;
}
