package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    // ---------- GET /users ----------
    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    // ---------- POST /users ----------
    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }

       if (emailExists(user.getEmail())) {
           throw new DuplicatedDataException("Этот имейл уже используется");
       }
       user.setId(getNextId());
       user.setRegistrationDate(Instant.now());
       users.put(user.getId(), user);
       return user;
    }

    // ---------- PUT /users ----------
    @PutMapping
    public User update(@RequestBody User user) {
        if (user.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        User storedUser = users.get(user.getId());
        if (storedUser == null) {
            return null;
        }

        if (user.getEmail() != null) {
            if (emailExists(user.getEmail(), user.getId())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            storedUser.setEmail(user.getEmail());
        }

        if (user.getUsername() != null) {
            storedUser.setUsername(user.getUsername());
        }

        if (user.getPassword() != null) {
            storedUser.setPassword(user.getPassword());
        }
        return storedUser;
    }

    // ---------- Вспомогательные методы ----------
    private boolean emailExists(String email) {
        for (User user : users.values()) {
            if (email.equals(user.getEmail())) {
                return true;
            }
        }
        return false;
    }

    private boolean emailExists(String email, long userId) {
        for (User user : users.values()) {
            if (email.equals(user.getEmail())
                    && user.getId() != userId) {
                return true;
            }
        }
        return false;
    }

    // генерация id — как в PostController
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
