package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.validate.FilmValidate;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public Film addFilm(Film film) {
        FilmValidate.validate(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        FilmValidate.validate(film);
        return filmStorage.updateFilm(film);
    }

    public Film deleteFilmById(int filmId) {
        checkFilmExist(filmId);
        return filmStorage.deleteFilmById(filmId);
    }

    boolean containsLike(int filmId, int userId) {
        if (filmStorage.getFilmById(filmId).getUsersLikes().contains(userId)) {
            return true;
        }
        return false;
    }

    public Film addLike(int filmId, int userId) {
        checkFilmExist(filmId);
        if (containsLike(filmId,userId))
            throw new ValidationException("Лайк от пользователя с id: " + userId +
            " уже существует для фильма с id: " + filmId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(int filmId, int userId) {
        checkFilmExist(filmId);
        if (!containsLike(filmId,userId))
            throw new NotExistException("Лайк от пользователя с id: " + userId + " не найден у фильма с id: " + filmId);
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getFilms().stream()
                .sorted((o1, o2) -> Integer.compare(o2.getUsersLikes().size(), o1.getUsersLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkFilmExist(int filmId) {
        filmStorage.getFilmById(filmId);
    }
}
