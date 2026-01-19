package ru.yandex.practicum.filmorate.service.recommendation;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;

public interface RecommendationService {
    List<Film> getRecommendations(Long userId);
}