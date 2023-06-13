package ru.yandex.practicum.filmorate.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        log.info("Найден фильм с id {}", id);
        return filmService.getFilmById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Фильм добавлен {}", film);
        throwIfFilmReleaseIncorrect(film.getReleaseDate());
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Фильм обновлен {}", film);
        throwIfFilmReleaseIncorrect(film.getReleaseDate());
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Добавлен лайк фильму с id {} от пользователя с id {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Удален лайк фильму с id {} пользователем с id {}", id, userId);
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        log.info("Получено {} самых популярных фильмов", count);
        return filmService.getMostPopularFilms(count);
    }

    private void throwIfFilmReleaseIncorrect(LocalDate date) {
        if (date.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("В качестве даты релиза указана некорректная дата");
        }
    }
}
