package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Override
    public List<Film> getRecommendations(Long userId) {
        // Проверяем существование пользователя
        User user = userStorage.getUser(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        // Получаем всех пользователей
        List<User> allUsers = userStorage.getAllUsers();

        // Находим пользователя с максимальным пересечением лайков
        Map<Long, Set<Long>> userLikesMap = new HashMap<>();

        // Заполняем мапу лайков пользователей
        for (User u : allUsers) {
            List<Film> likedFilms = filmStorage.getAllFilms().stream()
                    .filter(f -> f.getLikes() != null && f.getLikes().contains(u.getId()))
                    .collect(Collectors.toList());
            Set<Long> filmIds = likedFilms.stream()
                    .map(Film::getId)
                    .collect(Collectors.toSet());
            userLikesMap.put(u.getId(), filmIds);
        }

        // Лайки текущего пользователя
        Set<Long> currentUserLikes = userLikesMap.getOrDefault(userId, Collections.emptySet());

        // Находим пользователя с максимальным пересечением лайков
        Long mostSimilarUserId = findMostSimilarUser(userId, userLikesMap, currentUserLikes);

        if (mostSimilarUserId == null) {
            log.info("Для пользователя {} не найдено пользователей с пересекающимися лайками", userId);
            return Collections.emptyList();
        }

        // Фильмы, которые лайкнул похожий пользователь, но не лайкнул текущий
        Set<Long> similarUserLikes = userLikesMap.get(mostSimilarUserId);
        Set<Long> recommendedFilmIds = new HashSet<>(similarUserLikes);
        recommendedFilmIds.removeAll(currentUserLikes);

        // Получаем объекты фильмов
        List<Film> allFilms = filmStorage.getAllFilms();
        List<Film> recommendedFilms = allFilms.stream()
                .filter(f -> recommendedFilmIds.contains(f.getId()))
                .collect(Collectors.toList());

        log.info("Для пользователя {} найдено {} рекомендаций", userId, recommendedFilms.size());
        return recommendedFilms;
    }

    private Long findMostSimilarUser(Long currentUserId, Map<Long, Set<Long>> userLikesMap,
                                     Set<Long> currentUserLikes) {
        Long mostSimilarUserId = null;
        int maxIntersection = 0;

        for (Map.Entry<Long, Set<Long>> entry : userLikesMap.entrySet()) {
            Long otherUserId = entry.getKey();

            // Пропускаем текущего пользователя
            if (otherUserId.equals(currentUserId)) {
                continue;
            }

            Set<Long> otherUserLikes = entry.getValue();
            Set<Long> intersection = new HashSet<>(currentUserLikes);
            intersection.retainAll(otherUserLikes);

            if (intersection.size() > maxIntersection) {
                maxIntersection = intersection.size();
                mostSimilarUserId = otherUserId;
            }
        }

        return mostSimilarUserId;
    }
}