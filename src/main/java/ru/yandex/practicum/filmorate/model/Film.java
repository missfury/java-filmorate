package ru.yandex.practicum.filmorate.model;
import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    private int id;
    @NotBlank(message = "Укажите название фильма")
    private String name;
    @Size(max = 200, message = "Максимальная длина описания 200 символов")
    private String description;
    @NotNull(message = "Укажите дату выхода фильма")
    private LocalDate releaseDate;
    @NotNull(message = "Укажите продолжительность фильма")
    @PositiveOrZero(message = "Продолжительность фильма не может быть отрицательной")
    private Integer duration;
    private List<Integer> usersLikes;
    private Mpa mpa;
    private int rating;
    private LinkedHashSet<Genre> genres;

    public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration,
                Mpa mpa, LinkedHashSet<Genre> genres) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.genres = genres;
    }


    public void addGenre(Genre genre) {
        if (genres == null) {
            genres = new LinkedHashSet<>();
        }
        genres.add(genre);
    }
}

