package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final List<User> users = new ArrayList<>();
    private int nextId = 1;

    @PostMapping
    public ResponseEntity<String> createUser(@Valid  @RequestBody User user) {
        log.info("Попытка создать пользователя: {}", user.getName());
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректная электронная почта");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный логин");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ValidationException("Имя для отображения может быть пустым — в таком случае будет использован логин");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        user.setId(nextId++);
        users.add(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(user.getName());
    }

    @PutMapping
    public ResponseEntity<String> updateUser(@Valid @RequestBody User user) {
        log.info("Попытка обновить данные пользователя: {}", user.getName());

        if (user.getId() == null || user.getId() <= 0) {
            return ResponseEntity.badRequest().body("Некорректный ID пользователя");
        }

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректная электронная почта");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный логин");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                log.info("Пользователь с ID {} успешно обновлён", user.getId());
                return ResponseEntity.ok("Пользователь обновлён: " + user.getName());
            }
        }

        return ResponseEntity.notFound().build();
    }
    @GetMapping
    public List<User> getAllUsers() {
        log.info("Попытка вывести список пользователей");
        return users;
    }
}
