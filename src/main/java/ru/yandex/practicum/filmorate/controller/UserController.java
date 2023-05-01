package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validate.UserValidate;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int userIdCounter;

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        UserValidate.validate(user);
        int userId = ++userIdCounter;
        user.setId(userId);
        users.put(userId,user);
        log.info("Пользователь с логином: {} добавлен", user.getLogin());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        UserValidate.validate(user);
        int userId = user.getId();
        if (users.containsKey(userId)) {
            users.put(userId, user);
            log.info("Данные пользователя с логином: {} изменены.", user.getLogin());
            return user;
        } else
            throw new NotExistException("Пользователя с логином: " + user.getLogin() + " не существует.");
    }
}
