package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
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
    private final GenreStorage genreStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final MpaService mpaService;

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getFilms();
        return films;
    }

    public Film getFilmById(int filmId) {
        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotExistException("Фильма с id: " + filmId + " не существует"));
    }

    public Film addFilm(Film film) {
        FilmValidate.validate(film);
        if (film.getMpa() != null)
            mpaService.throwIfMpaNotExist(film.getMpa().getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreService.throwIfGenreNotExist(genre.getId());
            }
        }
        int newId = filmStorage.addFilm(film);
        return filmStorage.getFilmById(newId)
                .orElseThrow(() -> new NotExistException("Фильма с id: " + newId + " не существует"));
    }

    public Film updateFilm(Film film) {
        FilmValidate.validate(film);
        filmStorage.updateFilm(film);
        return filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new NotExistException("Фильма с id: " + film.getId() + " не существует"));
    }

    public void deleteFilmById(int filmId) {
        checkFilmExist(filmId);
        filmStorage.deleteFilmById(filmId);
    }

    boolean containsLike(int filmId, int userId) {
        if (filmStorage.getLikesByFilmId(filmId).contains(userId)) {
            return true;
        }
        return false;
    }

    public void addLike(int filmId, int userId) {
        checkFilmExist(filmId);
        if (containsLike(filmId,userId))
            throw new ValidationException("Лайк от пользователя с id: " + userId +
            " уже существует для фильма с id: " + filmId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        checkFilmExist(filmId);
        if (!containsLike(filmId,userId))
            throw new NotExistException("Лайк от пользователя с id: " + userId + " не найден у фильма с id: " + filmId);
        filmStorage.removeLike(filmId, userId);
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
