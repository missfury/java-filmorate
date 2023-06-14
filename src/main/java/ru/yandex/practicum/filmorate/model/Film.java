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

    public Film(int filmId, String nameFilm, String description, LocalDate releaseDate, int duration, int rate) {
        this.id = filmId;
        this.name = nameFilm;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.rating = rate;
    }

    public void addGenre(Genre genre) {
        if (genres == null) {
            genres = new LinkedHashSet<>();
        }
        genres.add(genre);
    }
}

