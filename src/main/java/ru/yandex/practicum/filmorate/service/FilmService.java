package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validate.FilmValidate;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("genreDbStorage") GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getFilms();
        filmStorage.loadFilmsGenres(films);
        return films;
    }

    public Film getFilmById(int filmId) {
        filmStorage.checkFilm(filmId);
        Film film = filmStorage.getFilmById(filmId);
        filmStorage.loadFilmsGenres(List.of(film));
        return film;
    }

    public Film addFilm(Film film) {
        FilmValidate.validate(film);
        filmStorage.addFilm(film);
        return film;
    }

    public Film updateFilm(Film film, int filmId) {
        filmStorage.checkFilm(film.getId());
        FilmValidate.validate(film);
        filmStorage.updateFilm(film, filmId);
        return film;
    }

    public void removeById(int filmId) {
        filmStorage.removeFilm(filmId);
    }

    public Film addLike(int filmId, int userId) {
        filmStorage.checkFilm(filmId);
        userStorage.checkUser(userId);

        return filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(int filmId, int userId) {
        filmStorage.checkFilm(filmId);
        userStorage.checkUser(userId);
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(Integer number) {
        return filmStorage.getMostPopularFilms(number);
    }

    public boolean validate(Film film) {
        if (film == null) {
            log.warn("Фильм не должен быть null");
            throw new ValidationException("Ошибка валидации. Тело запроса пустое.");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Дата релиза не должна быть раньше, чем Декабрь 28, 1895");
            throw new ValidationException("Ошибка валидации. Некорректная дата релиза.");
        }

        return true;
    }
}
