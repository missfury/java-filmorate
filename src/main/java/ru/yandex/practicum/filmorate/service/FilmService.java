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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final GenreStorage genreStorage;

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

    public List<Film> getPopularFilms(Integer limit, Integer genreId, Integer year) {
        StringBuilder condition = new StringBuilder();
        List<Film> films;

        if (genreId == null && year == null) {
            films = filmStorage.getPopular(limit, String.valueOf(condition));
        } else if (year == null) {
            condition.append("WHERE fg.genre_id = ").append(genreId);
            films = filmStorage.getPopular(limit, String.valueOf(condition));
        } else if (genreId == null) {
            condition.append("WHERE YEAR(f.release_date) = ").append(year);
            films = filmStorage.getPopular(limit, String.valueOf(condition));
        } else {
            condition.append("WHERE fg.genre_id = ").append(genreId)
                    .append("AND YEAR(f.release_date) = ").append(year);
            films = filmStorage.getPopular(limit, String.valueOf(condition));
        }
        loadInformationFilms(films);
        return films;
    }


    private void checkFilmExist(int filmId) {
        filmStorage.getFilmById(filmId);
    }

    private void loadInformationFilms(List<Film> films) {
        if (films.isEmpty()) { //Если фильмов нет, то не обращаемся за получением доп. информации
            log.info("filmStorage getRecommendation not exist");
            return;
        }
        log.info("filmStorage find all Films. films.size()  {}", films.size());
        genreStorage.loadFilmsGenres(films);
    }

}
