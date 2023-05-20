package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int lastUserId;

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public Map<Integer, User> getUsersMap() {
        return users;
    }

    @Override
    public User getUserById(int userId) {
        if (users.containsKey(userId))
            return users.get(userId);
        else
            throw new NotExistException("Пользователя с id: " + userId + " не существует.");
    }

    @Override
    public User addUser(User user) {
        int userId = ++lastUserId;
        user.setId(userId);
        users.put(userId,user);
        log.info("Пользователь с логином: {} сохранен.", user.getLogin());
        return user;
    }

    @Override
    public User updateUser(User user) {
        int userId = user.getId();
        if (users.containsKey(userId)) {
            users.put(userId, user);
            log.info("Данные пользователя с логином: {} изменены.", user.getLogin());
            return user;
        } else
            throw new NotExistException("Пользователя с логином: " + user.getLogin() + " не существует.");
    }

    @Override
    public User deleteUserById(int userId) {
        if (users.containsKey(userId)) {
            User user = users.get(userId);
            users.remove(userId);
            log.info("Пользователь с id: {} удален.", userId);
            return user;
        } else
            throw new NotExistException("Пользователя с id: " + userId + " не существует.");
    }

}
