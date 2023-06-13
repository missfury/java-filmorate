package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int filmId) {
        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotExistException(String.format("film id%s", filmId)));
    }

    public Film addFilm(Film film) {
        throwIfNoMpa(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        throwIfNoMpa(film);
        return filmStorage.updateFilm(film)
                .orElseThrow(() -> new NotExistException(String.format("film id%s", film.getId())));
    }

    private void throwIfNoMpa(Film film) {
        mpaStorage.getById(film.getMpa().getId())
                .orElseThrow(() -> new NotExistException(String.format("mpa id%s", film.getMpa().getId())));
    }

    public void addLike(int filmId, int userId) {
        getFilmById(filmId);
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotExistException(String.format("user id%s", userId)));
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        getFilmById(filmId);
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotExistException(String.format("user id%s", userId)));
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int limitSize) {
        return filmStorage.getMostPopularFilms(limitSize);
    }
}
