package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validate.FilmValidate;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service

public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int filmId) {
        filmStorage.checkFilm(filmId);
        return filmStorage.getFilmById(filmId);
    }

    public Film addFilm(Film film) {
        FilmValidate.validate(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film, int filmId) {
        filmStorage.checkFilm(filmId);
        FilmValidate.validate(film);
        return filmStorage.updateFilm(film, filmId);
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
        if (!filmStorage.getFilmById(filmId).getUsersLikes().contains(userId)) {
            throw new NotExistException("Пользователь не найден");
        }
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int limitSize) {
        return filmStorage.getMostPopularFilms(limitSize);
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
