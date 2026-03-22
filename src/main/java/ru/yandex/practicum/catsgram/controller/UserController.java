package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.model.User;
import ru.yandex.practicum.catsgram.service.PostService;
import ru.yandex.practicum.catsgram.service.UserService;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ---------- GET /users ----------
    @GetMapping
    public Collection<User> getUsers() {
        return userService.findAll();
    }

    // ---------- POST /users ----------
    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    // ---------- PUT /users ----------
    @PutMapping
    public User update(@RequestBody User user) {
        return userService.update(user);
    }

}
