package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final FilmStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getRecommendations(Long userId) {
        // Проверяем существование пользователя
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        if (userCount == null || userCount == 0) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        String sql = """
            WITH
            current_user_likes AS (
                SELECT film_id
                FROM likes
                WHERE user_id = ?
            ),
            similar_users AS (
                SELECT
                    l2.user_id AS similar_user_id,
                    COUNT(DISTINCT l2.film_id) AS common_likes_count
                FROM likes l1
                JOIN likes l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = ?
                    AND l2.user_id != ?
                GROUP BY l2.user_id
                HAVING COUNT(DISTINCT l2.film_id) > 0
                ORDER BY common_likes_count DESC
                LIMIT 1
            )
            SELECT DISTINCT l.film_id
            FROM likes l
            LEFT JOIN similar_users su ON l.user_id = su.similar_user_id
            WHERE su.similar_user_id IS NOT NULL
                AND l.film_id NOT IN (SELECT film_id FROM current_user_likes)
            ORDER BY l.film_id
           """;

        List<Long> recommendedFilmIds = jdbcTemplate.queryForList(sql, Long.class,
                userId, userId, userId);

        if (recommendedFilmIds.isEmpty()) {
            log.info("Для пользователя {} не найдено рекомендаций", userId);
            return List.of();
        }

        List<Film> recommendedFilms = filmStorage.getFilmsByIds(recommendedFilmIds);
        log.info("Для пользователя {} найдено {} рекомендаций", userId, recommendedFilms.size());

        return recommendedFilms;
    }
}