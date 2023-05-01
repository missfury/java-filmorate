package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class Film {
    private int id;
    @NotBlank(message = "Укажите название фильма.")
    private String name;
    @Size(max = 200, message = "Максимальная длина описания 200 символов.")
    private String description;
    @NotNull(message = "Укажите дату выхода фильма.")
    private LocalDate releaseDate;
    @NotNull(message = "Укажите продолжительность фильма.")
    @PositiveOrZero(message = "Продолжительность фильма не может быть отрицательной.")
    private Integer duration;
}
