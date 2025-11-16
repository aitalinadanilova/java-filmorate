package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.info("Попытка создать пользователя: {}", user.getName());
        validateUser(user);

        user.setId(nextId++);
        users.put(user.getId(), user);

        log.info("Пользователь {} успешно создан с ID {}", user.getName(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        log.info("Попытка обновить данные пользователя: {}", user.getName());

        if (user.getId() == null || user.getId() <= 0) {
            throw new ValidationException("Некорректный ID пользователя");
        }

        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        validateUser(user);

        users.put(user.getId(), user);

        log.info("Пользователь обновлён: id={}, email={}", user.getId(), user.getEmail());
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public Map<Integer, User> getAllUsers() {
        log.info("Запрошен список пользователей ({} всего)", users.size());
        return users;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректная электронная почта");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный логин");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
