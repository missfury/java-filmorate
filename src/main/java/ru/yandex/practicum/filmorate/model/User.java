package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;
    @Email(message = "Адрес электронной почты не соответствует формату")
    @NotBlank(message = "Укажите адрес электронной почты")
    private String email;
    @NotBlank(message = "Укажите логин")
    private String login;
    private String name;
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @NotNull(message = "Укажите дату рождения")
    private LocalDate birthday;
    private Set<Integer> friends = new HashSet<>();
}
