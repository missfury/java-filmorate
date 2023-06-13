package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validate.FilmValidate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

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

        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int limitSize) {
        return filmStorage.getMostPopularFilms(limitSize);
    }
}
