package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validate.FilmValidate;

import java.util.*;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int filmIdCounter;

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        Film film = films.getOrDefault(id, null);
        return film;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        FilmValidate.validate(film);
        int filmId = ++filmIdCounter;
        film.setId(filmId);
        films.put(filmId,film);
        log.info("Фильм с названием: {} добавлен", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        FilmValidate.validate(film);
        int filmId = film.getId();
        if (films.containsKey(filmId)) {
            films.put(filmId, film);
            log.info("Данные фильма с названием: {} отредактированы", film.getName());
            return film;
        } else
            throw new NotExistException("Фильма с названием: " + film.getName() + " в каталоге нет");
    }
}
