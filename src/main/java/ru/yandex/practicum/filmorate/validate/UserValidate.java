package ru.yandex.practicum.filmorate.validate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

@Slf4j
public class UserValidate {
    public static void validate(@Valid @RequestBody User user) {
        if (user.getLogin().contains(" ")) {
            log.warn("В логине содержатся пробелы, логин: {}", user.getLogin());
            throw new ValidationException("Логин содержит пробел");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            log.warn("Имя пользователя не заполнено. Вместо имени пользователя используется логин: {}", user.getLogin());
        }
    }
}

