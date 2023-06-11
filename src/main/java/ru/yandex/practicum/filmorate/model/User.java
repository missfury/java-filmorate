package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private Set<Integer> friends;

    public User(int id, String name, String login, String email, LocalDate birthday) {
        this.id = id;
        this.login = login;
        this.name = name;
        this.email = email;
        this.birthday = birthday;
    }
}
